package com.github.ugdbg.perceptron;


import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * A neuron computes an input vector into an output value :  <br>
 * It makes a linear combination of the input with a 'weight' vector and then applies an activation function. <br>
 * <br>
 * Then it can compute an error signal from an input and the next neuron layer error signal. <br>
 * It is mostly stateless - apart from its weights. <br>
 */
public class Neuron implements Serializable {

	/**
	 * Added to the linear combination of the input.
	 */
	private float bias = 0;

	/**
	 * Weight vector for linear combination
	 */
	private float[] weights;

	/**
	 * Activation function : used after linear combination. It should have some activation threshold properties.
	 */
	private Function activation;

	/**
	 * Scalar between 0 and 1. <br>
	 * Too low a learning rate makes the network learn very slowly. <br>
	 * Too high a learning rate makes the weights and objective function diverge, so there is no learning at all.
	 */
	private float learningFactor = 0.5f;

	/**
	 * Adding momentum to the weight changes is a simple way to avoid local minimum,
	 * a problem that impedes perceptron learning. <br>
	 * <br>
	 * This is done by saving a percentage of the last weight change
	 * and applying it to the current weight change operation. <br>
	 */
	private float momentum = 0.3f;

	public Neuron(float bias, float[] weights, float learningFactor, Function activation) {
		this.bias = bias;
		this.weights = weights;
		this.activation = activation;
		this.learningFactor = learningFactor;
	}

	/**
	 * Factory method : create a neuron and randomly initializes its weigth.
	 * @param weightSize     the weight vector size. Must equals the input data vectors length
	 * @param learningFactor the {@link #learningFactor}
	 * @param activation     the {@link #activation} function to use.
	 * @return the created and initialized Neuron.
	 */
	public static Neuron init(int weightSize, float learningFactor, Function activation){
		float[] weights = new float[weightSize];
		Random random = new Random();
		for(int i = 0; i < weightSize; i++){
			weights[i] = random.nextFloat();
		}
		return new Neuron(0, weights, learningFactor, activation);
	}

	/**
	 * Computes output from an input vector : <br>
	 * <ol>
	 * 	<li>Linear combination with {@link #weights} vector</li>
	 * 	<li>Activation function</li>
	 * </ol>
	 * @param input the input data
	 * @return the computed output
	 */
	public float compute(float[] input){
		return this.activationFunction(this.linearCombination(input));
	}

	/**
	 * Error signal for the neuron, given an input and the next layer error signal. <br>
	 * <ol>
	 *     <li>linear combination of the input vector with the neuron {@link #weights}</li>
	 *     <li>multiply with the sum of the next layer error signal</li>
	 *     <li>apply the {@link #activation} derive {@link Function#derive(float)} on the result</li>
	 * </ol>
	 * @param input                the neuron input
	 * @param nextLayerErrorSignal the next layer error signal
	 * @return the {@link Neuron.NeuronErrorSignal}
	 */
	public NeuronErrorSignal errorSignal(float[] input, List<Float> nextLayerErrorSignal){
		float error = this.activation.derive(this.linearCombination(input)) * this.sum(nextLayerErrorSignal);
		return new NeuronErrorSignal(error, this.weights);
	}

	/**
	 * Update the neuron's weights. <br>
	 * <b>Formula : </b> weight(i) = weight(i) + weight change + delta. <br>
	 * <br>
	 * With :
	 * <ul>
	 *     <li>weight change = (learning factor * error signal * output)</li>
	 *     <li>delta = momentum * weight change</li>
	 * </ul>
	 *
	 * @param errorSignal the error signal to apply on weights
	 * @param output the output associated with the error signal
	 */
	public void updateWeights(float errorSignal, float output){
		for(int i = 0; i < this.weights.length; i++){
			float weightChange = this.learningFactor * errorSignal * output;
			float delta = this.momentum * weightChange;
			this.weights[i] = this.weights[i] + weightChange + delta;
		}
	}

	/**
	 * Simple linear combination between an input vector and the neuron's weights. <br>
	 * Sizes must be the same !
	 * @param input the input vector
	 * @return the linear combination with the neuron's weights.
	 */
	private float linearCombination(float[] input){
		if(input.length != this.weights.length){
			throw new IllegalArgumentException(
				"Input size [" + input.length + "] does not match weigths size [" + this.weights.length + "]"
			);
		}
		float out = this.bias;
		for (int i = 0; i < input.length; i++) {
			out += input[i] * this.weights[i];
		}
		return out;
	}

	/**
	 * Use the activation function on an input float.
	 * @param input the input
	 * @return the output value
	 */
	private float activationFunction(float input){
		return this.activation.apply(input);
	}

	/**
	 * Sum a list of float.
	 * @param floats the float vector
	 * @return the sum of every float in the list
	 */
	private float sum(List<Float> floats){
		float out = 0;
		for(float element : floats) out += element;
		return out;
	}

	@Override
	public String toString() {
		String out = "[";
		for (float weight : this.weights) {
			out += String.format("%.1f", weight) + ", ";
		}
		out = StringUtils.removeEnd(out, ", ");
		out += "]";
		return out;
	}

	/**
	 * Error signal of a neuron. <br>
	 * Aggregate a global neuron error and an error signal for every weight,
	 * i.e. an error signal for every previous neuron.
	 */
	public static class NeuronErrorSignal {

		/**
		 * Total error signal
		 */
		private float error;

		/**
		 * {@link #error} multiplied with the neuron's weight vector.
		 */
		private float[] errorSignal;

		NeuronErrorSignal(float error, float[] weight){
			this.error = error;
			this.errorSignal = new float[weight.length];
			for(int i = 0; i < weight.length; i++){
				this.errorSignal[i] = error * weight[i];
			}
		}

		/**
		 * Get the neuron's global error
		 * @return {@link #error}
		 */
		public float getError() {
			return this.error;
		}

		/**
		 * Get the error signal of this neuron, for a given previous layer neuron. <br>
		 * @param neuronLayerIndex the previous neuron index in its layer i.e. the weight index of this neuron
		 * @return the error signal for previous neuron of the given index
		 */
		public float getErrorFor(int neuronLayerIndex){
			return this.errorSignal[neuronLayerIndex];
		}
	}
}
