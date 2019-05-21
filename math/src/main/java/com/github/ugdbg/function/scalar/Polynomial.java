package com.github.ugdbg.function.scalar;

import org.apache.commons.lang3.StringUtils;

/**
 * u:x → α*x^n + β*x^(n-1) + ... + λ*x + μ
 */
public class Polynomial extends DomainCheckedFunction<Polynomial> implements Derivable {
	
	/** x^0 to x^n : makes iteration easier */
	private final float[] factors;

	/**
	 * Constructor with explicit polynomial factors declaration.
	 * @param factors from x^0 to x^n
	 */
	public Polynomial(float... factors) {
		this.factors = factors;
	}

	/**
	 * u:x → α*x^n
	 * @param factor α
	 * @param pow    n
	 * @return a new Polynomial for single power
	 */
	public static Polynomial pow(float factor, int pow) {
		float[] factors = new float[pow + 1];
		for (int i = 0; i < pow; i++) {
			factors[i] = 0;
		}
		factors[pow] = factor;
		return new Polynomial(factors);
	} 
	
	@Override
	public Derivable derive() {
		float[] deriveFactors = new float[this.factors.length - 1];
		
		for (int i = 0; i < deriveFactors.length; i++) {
			deriveFactors[i] = this.factors[i + 1] * (i + 1);
		}
		
		return new Polynomial(deriveFactors);
	}

	@Override
	public float doApply(float input) {
		float sum = 0;
		for (int i = 0; i < this.factors.length; i++) {
			sum += this.factors[i] * Math.pow(input, i);
		}
		return sum;
	}

	@Override
	public String toString() {
		return "u:x → " + this.label();
	}

	@Override
	public String label() {
		StringBuilder builder = new StringBuilder();
		for (int i = this.factors.length - 1; i >=0; i--) {
			if (this.factors[i] == 0) {
				continue;
			}
			builder.append(this.factors[i]);
			if (i > 0) {
				builder.append("*x^").append(i);
			}
			builder.append(" + ");
		}
		return StringUtils.removeEnd(builder.toString(), " + ");
	}
}
