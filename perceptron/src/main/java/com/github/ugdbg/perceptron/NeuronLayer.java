package com.github.ugdbg.perceptron;

import com.github.ugdbg.function.scalar.Derivable;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A neuron layer is a set of neurons. <br>
 * They all receive the same input data. <br>
 * They apply their weights and activation function on it. <br>
 * A new vector of the {@link #neurons} length is the output of the layer. <br>
 * <br>
 * Backward propagation (error signal processing and weights update)
 * can be done in a single method : {@link #train(float[], float[], NeuronLayerError)}. <br>
 * An error signal for the whole layer is then available : {@link NeuronLayer}.
 */
public class NeuronLayer implements Serializable {

	/**
	 * The neurons of this layer
	 */
	private final Neuron[] neurons;

	/**
	 * Standard constructor. given the number of neurons.
	 * @param nbNeurons the number of neurons.
	 */
	public NeuronLayer(int nbNeurons) {
		this.neurons = new Neuron[nbNeurons];
	}

	/**
	 * Factory method : build the layer and randomly init all of its neurons weights.
	 * @param weightSize     the weight size of every neuron (the previous layer neurons size)
	 * @param learningFactor the learning factor of the layer neurons
	 * @param activation     the activation function of the neurons
	 */
	public void init(int weightSize, float learningFactor, Derivable activation){
		for(int i = 0; i < this.neurons.length; i++){
			this.neurons[i] = Neuron.init(weightSize, learningFactor, activation);
		}
	}

	/**
	 * Process an input vector. <br>
	 * Call {@link Neuron#compute(float[])} on every Neuron in {@link #neurons} and return an output vector.
	 * @param input the input data. Its size must match the {@link #neurons} size.
	 * @return the processed output vector
	 */
	public float[] compute(float[] input){
		float[] out = new float[this.neurons.length];
		for (int i = 0; i < this.neurons.length; i++) {
			out[i] = this.neurons[i].compute(input);
		}
		return out;
	}

	/**
	 * Backward propagation.
	 * <ol>
	 *     <li>
	 *         process the error signal for every neuron of the layer,
	 *         given the neuron input and the next layer error for this neuron
	 *     </li>
	 *     <li>update every neuron weights with this error signal and the computed neuron output</li>
	 * </ol>
	 * @param input  the neuron layer input vector
	 * @param output the neuron layer output vector
	 * @param nextLayerErrorSignal the next layer error signal
	 * @return the neuron layer error (a list of every neuron {@link Neuron.NeuronErrorSignal}
	 */
	public NeuronLayerError train(float[] input, float[] output, NeuronLayerError nextLayerErrorSignal){
		NeuronLayerError layerError = new NeuronLayerError();
		for (int i = 0; i < this.neurons.length; i++) {
			Neuron.NeuronErrorSignal errorSignal = this.neurons[i].errorSignal(input, nextLayerErrorSignal.getErrorFor(i));
			this.neurons[i].updateWeights(errorSignal.getError(), output[i]);
			layerError.add(errorSignal);
		}
		return layerError;
	}

	@Override
	public String toString() {
		String out = "NeuronLayer(" + String.format("%02d", this.neurons.length) + ") {";
		for(int i = 0; i < this.neurons.length; i++){
			out += "[" + i + "]" + " " + this.neurons[i].toString() + " | ";
		}
		StringUtils.removeEnd(out, " | ");
		out += '}';
		return out;
	}

	/**
	 * A neuron layer error signal. <br>
	 * This is a list of every layer neuron {@link Neuron.NeuronErrorSignal}.
	 */
	static class NeuronLayerError extends ArrayList<Neuron.NeuronErrorSignal>{
		public List<Float> getErrorFor(int neuronIndex){
			return this.stream().map(errorSignal -> errorSignal.getErrorFor(neuronIndex)).collect(Collectors.toList());
		}

		public NeuronLayerError() {}

		/**
		 * Bootstrap : last layer error signal. <br>
		 * We don't have a 'next layer error signal' for the last neuron layer. <br>
		 * Build the error signal from the desired output.
 		 */
		public NeuronLayerError(float[] wanted, float[] actual, NeuronLayer layer){
			float[] error = new float[wanted.length];
			for(int i = 0; i < wanted.length; i++){
				error[i] = wanted[i] - actual[i];
			}
			for(int i = 0; i < layer.neurons.length; i++){
				this.add(new Neuron.NeuronErrorSignal(error[i], NeuronNetwork.singleValueVector(1, wanted.length, i)));
			}
		}
	}
}
