package com.github.ugdbg.function.vector;

import com.github.ugdbg.vector.Vector;

import java.math.BigDecimal;

public interface ErrorFunction extends VDerivable {
	
	Vector expected();
	
	default Number normalizedError(Vector output) {
		Vector error = this.apply(output);
		float length = this.length(output);
		Number sum = error.sum();
		if (sum instanceof BigDecimal) {
			return ((BigDecimal) sum).multiply(BigDecimal.valueOf((-1 / length)));
		} else if (sum instanceof Float) {
			return (-1 / length) * sum.floatValue();
		} else {
			return (-1 / length) * sum.doubleValue();
		}
	}
	
	default int length(Vector output) {
		if (this.expected().dimension() != output.dimension()) {
			throw new IllegalArgumentException(
				"Bad vector size [" + this.expected().dimension() + "] VS [" + output.dimension() + "]"
			);
		}
		return this.expected().dimension();
	}
}
