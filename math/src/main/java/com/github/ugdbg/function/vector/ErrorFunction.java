package com.github.ugdbg.function.vector;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public interface ErrorFunction extends VDerivable {
	
	float[] expected();
	
	default float normalizedError(float[] output) {
		float[] error = this.apply(output);
		float length = this.length(output);
		return (-1 / length) * (float) Arrays.stream(ArrayUtils.toObject(error)).mapToDouble(f -> (double) f).sum();
	}
	
	default int length(float[] output) {
		if (this.expected().length != output.length) {
			throw new IllegalArgumentException(
				"Bad vector size [" + this.expected().length + "] VS [" + output.length + "]"
			);
		}
		return this.expected().length;
	}
}
