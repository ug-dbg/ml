package com.github.ugdbg.function.scalar;

/**
 * u:x → e(x)
 */
public class Exp extends DomainCheckedFunction<Exp> implements Derivable {

	@Override
	public Derivable derive() {
		return this;
	}

	@Override
	public float doApply(float input) {
		return (float) Math.exp(input);
	}

	@Override
	public String label() {
		return "e(x)";
	}

	@Override
	public String toString() {
		return "u:x → " + this.label();  
	}
}
