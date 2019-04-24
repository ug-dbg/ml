package com.github.ugdbg.function.scalar;

/**
 * u:x → e(x)
 */
public class Exp implements Derivable {
	@Override
	public Derivable derive() {
		return this;
	}

	@Override
	public float apply(float input) {
		return (float) Math.exp(input);
	}
}
