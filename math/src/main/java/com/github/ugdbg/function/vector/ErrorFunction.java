package com.github.ugdbg.function.vector;

public interface ErrorFunction {
	
	float apply(float[] expected, float[] output);
	
	default int length(float[] expected, float[] output) {
		if (expected.length != output.length) {
			throw new IllegalArgumentException("Bad vector size [" + expected.length + "] VS [" + output.length + "]");
		}
		return expected.length;
	}
}
