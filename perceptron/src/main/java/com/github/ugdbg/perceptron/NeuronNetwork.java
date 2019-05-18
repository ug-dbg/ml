package com.github.ugdbg.perceptron;

import com.github.ugdbg.function.scalar.Derivable;
import com.github.ugdbg.function.vector.Vector;
import org.apache.commons.collections4.ListUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A neuron network as a set of neuron layers {@link NeuronLayer}.
 * <br>
 * It supports :
 * <ul>
 *     <li>prediction : {@link #predict(Vector)}</li>
 *     <li>back propagation : {@link #backProp(Input)}</li>
 *     <li>training using batching of inputs : {@link #train(List, int, float, int, Executor)}</li>
 *     <li>parallel/sequential back-propagation for a batch</li>
 * </ul>
 * This code vastly derives from 
 * <a href ="https://www.miximum.fr/blog/introduction-au-deep-learning-2/">Thibault Jouannic's blog</a>.
 */
public class NeuronNetwork implements Serializable {
	
	private final int inputDim;
	private List<NeuronLayer> layers = new ArrayList<>();

	/**
	 * New neuron network. No layer.
	 * @param inputDim the input vector dimension
	 */
	public NeuronNetwork(int inputDim) {
		this.inputDim = inputDim;
	}

	/**
	 * Is this network coherent ?
	 * @return true if the output dimension of every layer matches the input dimension of the next layer. 
	 */
	public boolean isCoherent() {
		int lastOutputSize = this.inputDim;
		for (NeuronLayer layer : this.layers) {
			if (layer.inputSize() != lastOutputSize) {
				return false;
			}
			lastOutputSize = layer.outputSize();
		}
		return true;
	}

	@Override
	public String toString() {
		return "NeuronNetwork{" + String.join(" | ", this.shortLabel()) + "}";
	}

	/**
	 * A short label for this network : every layer {@link NeuronLayer#shortLabel()} with its position in the network.
	 * @return a short label for this network
	 */
	public List<String> shortLabel() {
		return IntStream.range(0, this.layers.size())
			.mapToObj(i -> "(" + i + ") | " + this.layers.get(i).shortLabel())
			.collect(Collectors.toList());
	}

	/**
	 * Get the network output size
	 * @return the output size of the last layer. -1 if no layer configured yet.
	 */
	private int outputSize() {
		return this.layers.isEmpty() ? -1 : this.layers.get(this.layers.size() - 1).outputSize();
	}

	/**
	 * Add a layer as the last layer in this network.
	 * @param layerSize  the layer size
	 * @param activation the layer activation function
	 */
	public void addLayer(int layerSize, Derivable activation) {
		int outputSize = this.outputSize();
		int layerInputSize = outputSize == -1 ? this.inputDim : outputSize;
		this.layers.add(new NeuronLayer(layerSize, layerInputSize, activation));
	}

	/**
	 * Feed the given vector throught the whole network and return the output vector top index (i.e. prediction class)
	 * @param data the input vector
	 * @return the network prediction for the input
	 */
	public int predict(Vector data) {
		return this.feedForward(data).topIndex();
	}
	
	/**
	 * Feed the given vector throught the whole network and return the output vector.
	 * @param data the input vector
	 * @return the network prediction for the input
	 */
	private Vector feedForward(Vector data) {
		Vector activation = data;
		for (NeuronLayer layer : this.layers) {
			activation = layer.forward(activation);
		}
		return activation;
	}

	/**
	 * Train the network : feed forward all the inputs as batches and back-propagate the error gradient.
	 * <br>
	 * Gradient back-propagation can be paralleled. Weight and bias update cannot though.
	 * @param inputs         the input vector / expected class
	 * @param steps          how many times should the inputs be played
	 * @param learningRate   the network learning rate
	 * @param batchSize      the input batches size
	 * @param executor       a custom executor (back-propagation can be paralleled)
	 */
	public void train(List<Input> inputs, int steps, float learningRate, int batchSize, Executor executor) {
		for (int i = 0; i < steps; i++) {
			Collections.shuffle(inputs);
			ListUtils.partition(inputs, batchSize).forEach(batch -> this.trainBatch(batch, learningRate, executor));
		}
	}

	/**
	 * Parallel computation of training the network using a batch of inputs.
	 * <ul>
	 *     <li>Create a batch of {@link Task} that will get gradients from {@link #backProp(Input)}.</li>
	 *     <li>Execute the batch using the executor</li>
	 *     <li>Average the gradients and update the layers</li>
	 * </ul>
	 * @param inputs       the input batch
	 * @param learningRate the learning rate (updating the weights and bias in the layers)
	 * @param executor     an executor for parralelism
	 */
	private void trainBatch(List<Input> inputs, float learningRate, Executor executor) {
		Gradients gradients = Gradients.init(this.layers);

		List<Task> tasks = inputs
			.stream()
			.map(i -> Task.of(() -> gradients.sum(this.backProp(i))))
			.collect(Collectors.toList());
		
		try {
			executor.invokeAll(tasks);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted exception running batch", e);
		}

		for (int i = 0; i < this.layers.size(); i++) {
			this.layers.get(i).update(gradients.get(i).average(inputs.size()), learningRate);
		}
	}

	/**
	 * Back-propagate an input vector through the network.
	 * @param input the input vector
	 * @return the back-propagation output, as a collection of error gradient (weights and bias) (one per layer)
	 */
	private Outputs backProp(Input input) {
		NeuronLayer.LayerOutput layerOutput = NeuronLayer.LayerOutput.activation(input.input);
		List<NeuronLayer.LayerOutput> layerOutputs = new ArrayList<>();
		layerOutputs.add(layerOutput);
		
		for (NeuronLayer layer : this.layers) {
			layerOutput = layer.verboseForward(layerOutput);
			layerOutputs.add(layerOutput);
		}
		
		Vector target = Vector.oneHot(input.expected, this.outputSize());
		Vector delta = this.getOutputDelta(layerOutput, target);
		List<Vector> deltas = new ArrayList<>();
		deltas.add(delta);

		for (int i = this.layers.size() - 1; i >= 1; i--) {
			NeuronLayer layer  = this.layers.get(i);
			NeuronLayer prev   = this.layers.get(i - 1);
			Vector activationPrime = prev.activationPrime(layerOutputs.get(i).aggregations);
			delta = activationPrime.mult(layer.getWeights().transpose().apply(delta));
			deltas.add(delta);
		}
		
		Collections.reverse(deltas);
		Outputs outputs = new Outputs();

		for (int i = 0; i < this.layers.size(); i++) {
			outputs.add(new Gradient(deltas.get(i).outer(layerOutputs.get(i).activation), deltas.get(i)));
		}
		
		return outputs;
	}

	/**
	 * Get the network output delta (δ), i.e. the delta for the last layer output.
	 * <br>
	 * For now we consider this δ as the difference between the layer output and the expected target.
	 * <br>
	 * See <a href="https://www.ics.uci.edu/~pjsadows/notes.pdf">Peter Sadowski notes on backpropagation</a>.
	 * <br>
	 * I understand this works if you either :
	 * <ul>
	 *     <li> consider cross-entropy loss function and use a Sigmoid activation function in the layer.</li>
	 *     <li> consider cross-entropy loss function and use a Softmax activation function in the layer.</li>
	 *     <li> consider Mean Squared Error loss function and a use Linear Output activation function in the layer.</li>
	 * </ul>
	 * <br>
	 * FIXME : is this correct ? 
	 * TODO : make this work for any loss/activation function couples.
	 * @param output the last layer output
	 * @param target the expected network output
	 * @return the delta (δ) of the network for the given last layer output
	 */
	private Vector getOutputDelta(NeuronLayer.LayerOutput output, Vector target) {
		return Vector.sub(output.activation, target);
	}

	/**
	 * A network input is a vector and an expected class (which should match the output vector top index)
	 */
	public static class Input {
		Vector input;
		int expected;
	
		public Input(Vector input, int expected) {
			this.input = input;
			this.expected = expected;
		}
	}

	/**
	 * The output of back-propagation.
	 */
	private static class Outputs extends Gradients {
		Outputs() {
			super();
		}
	}

	/**
	 * {@link Task} execution interface.
	 * <br>
	 * Default implementations for {@link #sequential()} and {@link #parallel(int)}.
	 */
	interface Executor {
		void invokeAll(List<Task> tasks) throws InterruptedException;
		
		/** Sequential execution in the current thread of all the tasks. */
		static Executor sequential() {
			return tasks -> tasks.forEach(Task::call);
		}
		
		/** Parallel execution in [parallelism] threads of all the tasks. */
		static Executor parallel(int parallelism) {
			return new Executor() {
				private ExecutorService executor = Executors.newFixedThreadPool(parallelism);
				@Override
				public void invokeAll(List<Task> tasks) throws InterruptedException {
					this.executor.invokeAll(tasks);
				}
			};
		}
	}

	/**
	 * A task that can be executed in a separated thread.
	 */
	public static class Task implements Callable<Void> {
		private Runnable function;
		
		static Task of(Runnable function){
			Task task = new Task();
			task.function = function;
			return task;
		}
		
		@Override
		public Void call() {
			this.function.run();
			return null;
		}
	}
}
