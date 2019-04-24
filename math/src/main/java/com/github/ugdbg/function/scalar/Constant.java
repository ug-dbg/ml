package com.github.ugdbg.function.scalar;

/**
 * u:x → k
 */
public class Constant implements Derivable {
	
	private final float k;

	public Constant(float k) {
		this.k = k;
	}

	@Override
	public Derivable derive() {
		return new Zero();
	}

	@Override
	public float apply(float input) {
		return this.k;
	}
}
