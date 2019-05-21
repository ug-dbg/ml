package com.github.ugdbg.function.scalar;

import com.github.ugdbg.function.scalar.domain.Domains;

/**
 * u:x → ln(x)
 */
public class Ln extends DomainCheckedFunction<Ln> implements Derivable {

	public Ln() {
		this.domain = Domains.R_PLUS_CLOSED;
	}

	@Override
	public Derivable derive() {
		return new Ratio(new Constant(1), new Linear(1, 0));
	}

	@Override
	public float doApply(float input) {
		return (float) Math.log(input);
	}

	@Override
	public String label() {
		return "ln(x)";
	}

	@Override
	public String toString() {
		return "u:x → " + this.label();
	}
}
