package com.github.ugdbg.function.scalar;

import com.github.ugdbg.function.scalar.domain.Domains;

import java.io.Serializable;

/**
 * u:x → 1 / (1 + exp(-λ * x))
 */
public class Sigmoid extends DomainCheckedFunction<Sigmoid> implements Derivable, Serializable {

	private int lambda;
	
	public Sigmoid(int lambda) {
		this.lambda = lambda;
	}

	@Override
	public float doApply(float input) {
		return (float) (1f / (1f + Math.exp(this.lambda * -1 * input)));
	}

	@Override
	public Function derive() {
		return input -> {
			float out = Sigmoid.this.apply(input);
			return out * (1f - out);
		};
	}

	@Override
	public String label() {
		return "1 / (1 + e(-" + this.lambda + " * x))";
	}

	@Override
	public String toString() {
		return "u:x → " + this.label();
	}
}
