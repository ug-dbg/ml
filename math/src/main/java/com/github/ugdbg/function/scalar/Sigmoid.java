package com.github.ugdbg.function.scalar;

import java.io.Serializable;

/**
 * u:x → 1 / (1 + exp(-λ * x))
 */
public class Sigmoid implements Derivable, Serializable {

	private int lambda;

	public Sigmoid(int lambda) {
		this.lambda = lambda;
	}

	@Override
	public float apply(float input) {
		return (float) (1 / (1 + Math.exp(this.lambda * -1 * input)));
	}

	@Override
	public Function derive() {
		return input -> {
			double expMinusLambda = Math.exp(Sigmoid.this.lambda * -1 * input);
			double numerator   = Sigmoid.this.lambda * expMinusLambda;
			double denominator = (1 + expMinusLambda) * (1 + expMinusLambda);
			return (float) (numerator / denominator);
		};
	}
}
