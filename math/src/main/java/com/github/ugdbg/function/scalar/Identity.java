package com.github.ugdbg.function.scalar;

/**
 * u:x → x
 */
public class Identity implements Derivable {
	@Override
	public Derivable derive() {
		return new Constant(1);
	}

	@Override
	public float apply(float input) {
		return input;
	}

	@Override
	public String label() {
		return "x";
	}

	@Override
	public String toString() {
		return "u:x → " + this.label();
	}
}
