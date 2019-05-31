package com.github.ugdbg.perceptron;

import com.github.ugdbg.function.vector.Matrix;
import com.github.ugdbg.function.vector.VDerivable;
import com.github.ugdbg.function.vector.domain.VDomains;
import com.github.ugdbg.vector.primitive.FloatVector;

import java.io.Serializable;
import java.util.Random;

/**
 * A neuron layer is a collection of neurons that links the input vector to the next layer.
 * <br>
 * {@link #forward(FloatVector)} method operates an {@link #aggregation(FloatVector)} and an {@link #activation}
 * to compute an output vector that will be processed by the next layer.
 * <br>
 * We chose to model a neuron layer using : 
 * <ul>
 *     <li>a Matrix of {@link #weights} that will be used for the {@link #aggregation(FloatVector)} operation</li>
 *     <li>a {@link #bias} vector that will also be used for the {@link #aggregation(FloatVector)} operation</li>
 *     <li>a derivable {@link #activation} function that will be used for the {@link #activation(FloatVector)} operation</li>
 * </ul>
 * The {@link #verboseForward(LayerOutput)} method does a {@link #forward(FloatVector)} and stores the output from
 * both aggregation and activation : this is the method that should be used to train the network.
 * <br>
 * Both weights and bias can then be updated from the next layer error gradient using {@link #update(Gradient, float)}.
 */
class NeuronLayer implements Serializable {
	private Matrix weights;
	private FloatVector bias;
	private VDerivable activation;

	/**
	 * A new neuron layer for the given I/O sizes.
	 * {@link #weights} matrix is initialized as gaussian (Gaussian ("normally") distributed values).
	 * @param outputSize the layer output size (the 'height' of the weight matrix and the dimension of the bias vector) 
	 * @param inputSize  the layer input size (the 'width' of the weight matrix
	 * @param activation the activation function of the layer
	 */
	NeuronLayer(int outputSize, int inputSize, VDerivable activation) {
		this.weights = Matrix.randomGaussian(outputSize, inputSize, new Random());
		this.bias = FloatVector.of(outputSize);
		this.activation = activation;
	}
	
	/**
	 * The layer input size
	 * @return the width of the {@link #weights} matrix
	 */
	int inputSize() {
		return this.weights.getN();
	}

	/**
	 * The layer output size
	 * @return the height of the {@link #weights} matrix 
	 */
	int outputSize() {
		return this.weights.getM();
	}

	/**
	 * Get the weights matrix
	 * @return {@link #weights}
	 */
	Matrix getWeights() {
		return this.weights;
	}

	/**
	 * A short label for the layer : input dimension, weight matrix/bias vector, activation function, output dimension. 
	 * @return e.g. 'Dimension:200 ⇒ M(10, 200)+V(10) ⇒ 1 / (1 + e(-1 * x)) ⇒ Dimension:10'
	 */
	public String shortLabel() {
		return this.weights.domain() 
			+ " ⇒ " + this.weights.shortLabel() + "+" + this.bias.shortLabel() 
			+ " ⇒ " + this.activation.label()
			+ " ⇒ " + VDomains.R(this.outputSize()); 
	}

	/**
	 * Do a forward : {@link #aggregation(FloatVector)} then {@link #activation(FloatVector)}.
	 * @param data the input vector
	 * @return the output from the forward on the current layer
	 */
	FloatVector forward(FloatVector data) {
		return this.activation(this.aggregation(data));
	}

	/**
	 * Do a {@link #forward(FloatVector)} but keep a reference to both aggregation and activation outputs.
	 * @param previous the previous layer output {@link LayerOutput#activation} is used as forward input)
	 * @return the output from the forward on the current layer
	 */
	LayerOutput verboseForward(LayerOutput previous) {
		LayerOutput output = new LayerOutput();
		output.aggregations = this.aggregation(previous.activation);
		output.activation = this.activation(output.aggregations);
		return output;
	}

	/**
	 * Do the aggregation on the given input : apply the vector to the {@link #weights} matrix.
	 * @param data the input vector
	 * @return the output vector
	 */
	private FloatVector aggregation(FloatVector data) {
		return this.weights.apply(data).sum(this.bias);
	}

	/**
	 * Do the activation on the given input : apply the vector to the {@link #activation} function.
	 * @param data the input vector
	 * @return the output vector
	 */
	private FloatVector activation(FloatVector data) {
		return this.activation.apply(data);
	}

	/**
	 * Back-propagation : apply the vector to the derivative of the {@link #activation} function.
	 * @param data the input vector
	 * @return the output vector
	 */
	FloatVector activationPrime(FloatVector data) {
		return this.activation.derive().apply(data);
	}

	/**
	 * Update the weights and bias of the current layer.
	 * <ul>
	 *     <li>(weights = weights * (gradient * learningRate * -1)</li>
	 *     <li>(bias = bias * (gradient * learningRate * -1)</li>
	 * </ul>
	 * @param gradient     the gradient to apply
	 * @param learningRate the learning rate
	 */
	void update(Gradient gradient, float learningRate) {
		this.updateWeights(gradient.weightGradient, learningRate);
		this.updateBiases(gradient.biasGradient, learningRate);
	}

	/**
	 * Update the weights matrix using the error gradient matrix and the learning rate. 
	 * <br>
	 * weights = weights * (gradient * learningRate * -1)
	 * @param gradient     the error gradient matrix
	 * @param learningRate the learning rate
	 */
	private void updateWeights(Matrix gradient, float learningRate) {
		this.weights.sum(gradient.mult(learningRate * -1f));
	}

	/**
	 * Update the bias using the error gradient vector.
	 * <br>
	 * (bias = bias * (gradient * learningRate * -1)
	 * @param gradient     the error gradient vector
	 * @param learningRate the learning rate
	 */
	private void updateBiases(FloatVector gradient, float learningRate) {
		this.bias.sum(gradient.mult(learningRate * -1f));
	}

	/**
	 * Stores the aggregation and activation output values after a forward.
	 */
	static class LayerOutput {
		FloatVector aggregations;
		FloatVector activation;

		private LayerOutput() {}
		
		static LayerOutput activation(FloatVector activation) {
			LayerOutput output = new LayerOutput();
			output.activation = activation;
			return output;
		}
	}
}