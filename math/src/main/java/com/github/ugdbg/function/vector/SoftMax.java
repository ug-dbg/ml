package com.github.ugdbg.function.vector;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * sm:x(x₁,x₂,x₃...xₙ) → y(y₁,y₂,y₃...yₙ)
 * <br>
 * where yₖ = exp(xₖ) / ∑₁→ₙ (x₁,x₂,x₃...xₙ) .
 */
public class SoftMax implements VFunction {
	@Override
	public float[] apply(float[] input) {
		Float[] inputObjects = ArrayUtils.toObject(input);
		float sum = this.expSum(inputObjects);
		return ArrayUtils.toPrimitive(
			Arrays.stream(inputObjects).map(f -> (float) Math.exp(f) / sum).toArray(Float[]::new)
		);
	}
	
	private float expSum(Float[] input) {
		return (float) Arrays.stream(input).map(Math::exp).mapToDouble(d -> d).sum();
	}
}
