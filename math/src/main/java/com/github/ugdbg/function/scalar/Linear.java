package com.github.ugdbg.function.scalar;

/**
 * u:x → a * x + b
 */
public class Linear implements Derivable {
	
	private final float a;
	
	private final float b;

	public Linear(float a, float b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public Derivable derive() {
		return new Constant(this.a);
	}

	@Override
	public float apply(float input) {
		return this.a * input + this.b;
	}
}
