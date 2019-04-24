package com.github.ugdbg.function.vector;

public class CrossEntropy implements ErrorFunction {
	@Override
	public float apply(float[] expected, float[] output) {
		int length = this.length(expected, output);
		
		float sum = 0;
		for (int i = 0; i < length; i++) {
			sum += expected[i] * Math.log(output[i]) + (1 - expected[i] * Math.log(1 - output[i]));
		}
		
		return (-1 / (float) length) * sum;
	}
}
