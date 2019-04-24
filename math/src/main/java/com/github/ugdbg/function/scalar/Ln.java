package com.github.ugdbg.function.scalar;

/**
 * u:x → ln(x)
 */
public class Ln implements Derivable {
	@Override
	public Derivable derive() {
		return new Ratio(new Constant(1), new Linear(1, 0));
	}

	@Override
	public float apply(float input) {
		return (float) Math.log(input);
	}
}
