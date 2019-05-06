package com.github.ugdbg.function.vector;

/**
 * d:x(x₁,x₂,x₃...xₙ) → y(y₁,y₂,y₃...yₙ) where yₖ = (xₖ - tₖ)² and tₖ = (t₁,t₂,t₃...tₙ) is the expected output.
 */
public class Distance implements ErrorFunction {
	
	private final float[] expected;

	public Distance(float[] expected) {
		this.expected = expected;
	}

	@Override
	public float[] expected() {
		return this.expected;
	}

	@Override
	public float[] apply(float[] output) {
		float[] distance = new float[output.length];
		for (int i = 0; i < output.length; i++) {
			distance[i] = this.calculate(i, output[i]);
		}
		return distance;
	}

	@Override
	public VFunction derive() {
		return (VFunction) input -> {
			float[] out = new float[input.length];
			for (int i = 0; i < input.length; i++) {
				out[i] = 2 * (input[i] - Distance.this.expected()[i]);
			}
			return out;
		};
	}

	private float calculate(int i, float value) {
		return (float) Math.pow(value - this.expected[i], 2);
	}
}
