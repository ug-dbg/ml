package com.github.ugdbg.perceptron;

import com.google.common.collect.Lists;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A neuron network is :
 * <ul>
 *     <li>an input neuron layer</li>
 *     <li>'hidden' layers of neurons</li>
 *     <li>an output neuron layer</li>
 * </ul>
 * Computing then back propagation (see {@link #train(float[], float[])} can be achieved on a neuron network.
 */
public class NeuronNetwork implements Serializable {

	private NeuronLayer inputLayer;

	private List<NeuronLayer> hiddenLayers = new ArrayList<>();

	private NeuronLayer outputLayer;

	public NeuronNetwork(
		int inputDataLength,
		int inputLayerSize,
		int outputLayerSize,
		float learningFactor,
		int... hiddenLayersSizes){

		this.inputLayer  = new NeuronLayer(inputLayerSize);

		int previousSize = inputLayerSize;
		for (int size : hiddenLayersSizes) {
			NeuronLayer neuronLayer = new NeuronLayer(size);
			neuronLayer.init(previousSize, learningFactor, new Sigmoid(1));
			this.hiddenLayers.add(neuronLayer);
			previousSize = size;
		}

		this.outputLayer = new NeuronLayer(outputLayerSize);
		this.inputLayer.init(inputDataLength, learningFactor, new Sigmoid(1));
		this.outputLayer.init(previousSize,   learningFactor, new Sigmoid(1));
	}

	public float[] compute(float[] input){
		float[] lastOutput = this.inputLayer.compute(input);

		for (NeuronLayer hiddenLayer : this.hiddenLayers) {
			lastOutput = hiddenLayer.compute(lastOutput);
		}

		return this.outputLayer.compute(lastOutput);
	}

	public void train(float[] input, float[] wanted){
		Map<NeuronLayer, float[]> inputs  = new HashMap<>();
		Map<NeuronLayer, float[]> outputs = new HashMap<>();

		inputs.put(this.inputLayer, input);
		float[] inputLayerOutput   = this.inputLayer.compute(input);
		outputs.put(this.inputLayer, inputLayerOutput);

		float[] lastOutput = inputLayerOutput;
		for (NeuronLayer hiddenLayer : this.hiddenLayers) {
			inputs.put(hiddenLayer, lastOutput);
			lastOutput = hiddenLayer.compute(lastOutput);
			outputs.put(hiddenLayer, lastOutput);
		}

		inputs.put(this.outputLayer, lastOutput);
		float[] output = this.outputLayer.compute(lastOutput);
		outputs.put(this.outputLayer, output);

		NeuronLayer.NeuronLayerError outputErrorSignal = this.outputLayer.train(
			inputs.get(this.outputLayer),
			output,
			new NeuronLayer.NeuronLayerError(wanted, output, this.outputLayer)
		);

		List<NeuronLayer> reversed = Lists.reverse(this.hiddenLayers);
		NeuronLayer.NeuronLayerError hiddenLayerErrorSignal = outputErrorSignal;
		for (NeuronLayer hiddenLayer : reversed) {
			hiddenLayerErrorSignal = hiddenLayer.train(
				inputs.get(hiddenLayer),
				outputs.get(hiddenLayer),
				hiddenLayerErrorSignal
			);
		}

		this.inputLayer.train(
			inputs.get(this.inputLayer),
			outputs.get(this.inputLayer),
			hiddenLayerErrorSignal
		);
	}

	@Override
	public String toString() {
		String out = "Input layer : " + this.inputLayer.toString() + "\n";
		int count = 0;
		for (NeuronLayer hiddenLayer : this.hiddenLayers) {
			out += "   - Hidden Layer [" +  String.format("%02d", count) + "] : " + hiddenLayer.toString() + "\n";
			count++;
		}
		out += "Output layer : " + this.outputLayer.toString();
		return out;
	}

	public static float[] singleValueVector(float value, int length){
		float[] out = new float[length];
		for(int i = 0; i < length; i++){
			out[i] = value;
		}
		return out;
	}

	public static float[] singleValueVector(float value, int length, int onlyAt){
		float[] out = new float[length];
		for(int i = 0; i < length; i++){
			out[i] = i == onlyAt ? value : 0;
		}
		return out;
	}

	// Compute, train, serialize network and print to stdout some dummy/random data.
	// This is bullshit.
	// TODO : Find something to use this perceptron for :-/
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		float[] input = {0.5f, 4, 2, 4.3f, 0.9f, 0};
		NeuronNetwork network = new NeuronNetwork(input.length, 10, 3, 0.1f, 10, 4, 7);

		System.out.println(network);

		float[] out = network.compute(input);

		Random random = new Random();
		for(int i = 0; i < 1000; i++) {
			network.train(input, new float[]{
				random.nextFloat() + random.nextInt(10),
				random.nextFloat() + random.nextInt(1),
				random.nextFloat() + random.nextInt(4)
			});
		}

		Path serializedNetwork = Files.createTempFile("network", null);
		ObjectOutputStream serializer = new ObjectOutputStream(new FileOutputStream(serializedNetwork.toFile()));
		serializer.writeObject(network);
		serializer.flush();
		serializer.close();

		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(serializedNetwork.toFile()));
		network = (NeuronNetwork) ois.readObject();

		System.out.println(Arrays.toString(network.compute(input)) + " VS " + Arrays.toString(out));
		System.out.println(network);
	}

}
