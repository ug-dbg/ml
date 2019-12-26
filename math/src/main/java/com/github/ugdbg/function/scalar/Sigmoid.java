package com.github.ugdbg.function.scalar;

import com.github.ugdbg.NumberUtils;
import ch.obermuhlner.math.big.BigDecimalMath;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * u:x → 1 / (1 + exp(-λ * x))
 */
public class Sigmoid extends DomainCheckedFunction<Sigmoid> implements Derivable, Serializable {

	private int lambda;
	
	public Sigmoid(int lambda) {
		this.lambda = lambda;
	}

	@Override
	public float doApply(float input) {
		return (float) (1f / (1f + Math.exp(this.lambda * -1 * input)));
	}

	@Override
	public Number doApply(Number input) {
		if (input instanceof BigDecimal) {
			BigDecimal exponent = ((BigDecimal) input).multiply(BigDecimal.valueOf(-1 * this.lambda));
			
			return BigDecimal.ONE.divide(
				BigDecimalMath.pow(NumberUtils.E, exponent, this.mathContext()).add(BigDecimal.ONE), 
				RoundingMode.HALF_DOWN
			);
		}
		return super.doApply(input);
	}

	@Override
	public Function derive() {
		return new Function() {
			@Override
			public float doApply(float input) {
				float out = Sigmoid.this.apply(input);
				return out * (1f - out);
			}

			@Override
			public Number doApply(Number input) {
				Number out = Sigmoid.this.doApply(input);
				if (out instanceof BigDecimal) {
					return ((BigDecimal) out).multiply(BigDecimal.ONE.subtract((BigDecimal) out));
				}
				return out.doubleValue() * (1f - out.doubleValue());
			}
		};
	}

	@Override
	public String label() {
		return "1 / (1 + e(-" + this.lambda + " * x))";
	}

	@Override
	public String toString() {
		return "u:x → " + this.label();
	}
}
