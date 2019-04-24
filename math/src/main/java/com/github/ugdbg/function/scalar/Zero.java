package com.github.ugdbg.function.scalar;

/**
 * u:x → 0
 */
public class Zero implements Derivable {
	@Override
	public Derivable derive() {
		return this;
	}

	@Override
	public float apply(float input) {
		return 0;
	}
}
