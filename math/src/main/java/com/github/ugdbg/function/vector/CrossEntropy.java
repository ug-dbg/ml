package com.github.ugdbg.function.vector;

/**
 * ce:x(x₁,x₂,x₃...xₙ) → y(y₁,y₂,y₃...yₙ)
 * <br>
 * where :
 * <ul>
 *     <li>yₖ = tₖ * log(xₖ) + (1 - tₖ * log(1 - xₖ))</li>
 *     <li>tₖ = (t₁,t₂,t₃...tₙ) is the expected output</li>
 * </ul>
 */
public class CrossEntropy extends DomainCheckedFunction<CrossEntropy> implements ErrorFunction {
	
	private final float[] expected;

	public CrossEntropy(float[] expected) {
		this.expected = expected;
	}

	@Override
	public float[] expected() {
		return this.expected;
	}

	@Override
	public float[] doApply(float[] output) {
		float[] error = new float[output.length];
		for (int i = 0; i < output.length; i++) {
			error[i] = this.calculate(i, output[i]);
		}
		return error;
	}
	
	protected float calculate(int i, float value) {
		return (float) (-1f * (this.expected[i] * Math.log(value) + (1 - this.expected[i]) * Math.log(1 - value)));
	}

	@Override
	public ErrorFunction derive() {
		return new CrossEntropy(CrossEntropy.this.expected()) {
			@Override
			protected float calculate(int i, float value) {
				float expected = this.expected()[i];
				return -1 * (expected / value) + (1 - expected) / (1 - value);
			}
		};
	}
}
