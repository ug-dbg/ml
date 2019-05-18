package com.github.ugdbg.perceptron;

import com.github.ugdbg.function.vector.Matrix;
import com.github.ugdbg.function.vector.Vector;

/**
 * A gradient that can be applied to a {@link NeuronLayer} when back-propagating.
 * <br>
 * A gradient can be summed with an other one and averaged.
 */
class Gradient {
	Matrix weightGradient;
	Vector biasGradient;

	Gradient(int inputSize, int outputSize) {
		this(new Matrix(outputSize, inputSize), Vector.of(outputSize));
	}
	
	Gradient(Matrix weightGradient, Vector biasGradient) {
		this.weightGradient = weightGradient;
		this.biasGradient = biasGradient;
	}
	
	void sum(Gradient gradient) {
		this.weightGradient.sum(gradient.weightGradient);
		this.biasGradient.sum(gradient.biasGradient);
	}
	
	Gradient average(int size) {
		this.weightGradient.mult(1f / size);
		this.biasGradient.mult(1f / size);
		return this;
	}
}