package com.github.ugdbg.function.vector;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * sm:x(x₁,x₂,x₃...xₙ) → y(y₁,y₂,y₃...yₙ)
 * <br>
 * where yₖ = exp(xₖ) / ∑₁→ₙ (x₁,x₂,x₃...xₙ) .
 */
public class SoftMax implements VDerivable {
	@Override
	public float[] apply(float[] input) {
		Float[] inputObjects = ArrayUtils.toObject(input);
		float sum = this.expSum(inputObjects);
		return ArrayUtils.toPrimitive(
			Arrays.stream(inputObjects).map(f -> (float) Math.exp(f) / sum).toArray(Float[]::new)
		);
	}

	@Override
	public VFunction derive() {
		return (VFunction) input -> this.jacobian(input).apply(input);
	}

	private Matrix jacobian(float[] input) {
		int dimension = input.length;
		float[] softmaxOut = SoftMax.this.apply(input);
		return new Matrix(dimension, dimension).operation(
			(matrix, i, j) -> matrix.at(i, j, softmaxOut[j] * (Matrix.kroneckerDelta(i, j) - softmaxOut[i]))
		);
	}
	
	private float expSum(Float[] input) {
		return (float) Arrays.stream(input).map(Math::exp).mapToDouble(d -> d).sum();
	}
}
