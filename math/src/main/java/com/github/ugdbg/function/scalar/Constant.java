package com.github.ugdbg.function.scalar;

import com.github.ugdbg.function.scalar.domain.Domains;

/**
 * u:x → k
 */
public class Constant extends DomainCheckedFunction<Constant> implements Derivable {
	
	private final float k;

	public Constant(float k) {
		this.k = k;
		this.domain = Domains.R_CLOSED;
	}

	@Override
	public Derivable derive() {
		return new Zero();
	}

	@Override
	public float doApply(float input) {
		return this.k;
	}

	@Override
	public String label() {
		return String.valueOf(this.k);
	}

	@Override
	public String toString() {
		return "u:x → " + this.label();
	}
}
