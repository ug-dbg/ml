package com.github.ugdbg.function.vector;

public class Distance implements ErrorFunction {
	@Override
	public float apply(float[] expected, float[] output) {
		int length = this.length(expected, output);
		
		float sum = 0;
		for (int i = 0; i < length; i++) {
			sum += Math.pow(output[i] - expected[i], 2);
		}
		return sum;
	}
}
