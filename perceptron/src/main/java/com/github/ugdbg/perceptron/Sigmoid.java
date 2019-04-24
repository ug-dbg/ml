package com.github.ugdbg.perceptron;

import java.io.Serializable;

/**
 * x → 1 / (1 + exp(-λ * x))
 */
public class Sigmoid implements Function, Serializable {

	private int lambda;

	public Sigmoid(int lambda) {
		this.lambda = lambda;
	}

	@Override
	public float apply(float input) {
		return (float) (1 / (1 + Math.exp(this.lambda * -1 * input)));
	}

	@Override
	public float derive(float input) {
		double expMinusLambda = Math.exp(this.lambda * -1 * input);
		double numerator   = this.lambda * expMinusLambda;
		double denominator = (1 + expMinusLambda) * (1 + expMinusLambda);
		return (float) (numerator / denominator);
	}
}
