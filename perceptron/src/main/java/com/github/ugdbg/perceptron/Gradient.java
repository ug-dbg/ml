package com.github.ugdbg.perceptron;

import com.github.ugdbg.datatypes.TYPE;
import com.github.ugdbg.function.vector.Matrix;
import com.github.ugdbg.vector.Vector;

/**
 * A gradient that can be applied to a {@link NeuronLayer} when back-propagating.
 * <br>
 * A gradient can be summed with an other one and averaged.
 */
class Gradient {
	Matrix weightGradient;
	Vector biasGradient;

	Gradient(int inputSize, int outputSize, TYPE type) {
		this(new Matrix(outputSize, inputSize, type), Vector.of(type, outputSize));
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
		float coef = 1f / size;
		this.weightGradient.mult(coef);
		this.biasGradient.mult(coef);
		return this;
	}
}