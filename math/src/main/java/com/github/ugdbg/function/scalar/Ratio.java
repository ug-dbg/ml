package com.github.ugdbg.function.scalar;

/**
 * u:x → f(x) / g(x)
 */
public class Ratio implements Derivable {
	
	private final Derivable f;
	
	private final Derivable g;

	public Ratio(Derivable f, Derivable g) {
		this.f = f;
		this.g = g;
	}

	@Override
	public Function derive() {
		return input -> {
			float g = Ratio.this.g.apply(input);
			float gPrime = Ratio.this.g.derive().apply(input);
			float f = Ratio.this.f.apply(input);
			float fPrime = Ratio.this.f.derive().apply(input);
			return (fPrime * g - f * gPrime) / (g * g);  
		};
	}

	@Override
	public float apply(float input) {
		return this.f.apply(input) / this.g.apply(input);
	}

	@Override
	public String label() {
		return this.f.label() + " / " + this.g.label();
	}

	@Override
	public String toString() {
		return "u:x → " + this.label();
	}
}
