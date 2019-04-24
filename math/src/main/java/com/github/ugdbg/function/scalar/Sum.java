package com.github.ugdbg.function.scalar;

/**
 * u:x → f(x) + g(x) 
 */
public class Sum implements Derivable {
	
	private final Derivable f;
	
	private final Derivable g;

	public Sum(Derivable f, Derivable g) {
		this.f = f;
		this.g = g;
	}

	@Override
	public float apply(float input) {
		return this.f.apply(input) + this.g.apply(input);
	}
	
	@Override
	public Function derive() {
		return input -> this.f.derive().apply(input) + this.g.derive().apply(input);
	}

	@Override
	public String label() {
		return this.f.label() + this.g.label();
	}

	@Override
	public String toString() {
		return "u:x → " + this.label();
	}
}
