package com.github.ugdbg.function.vector;

import java.util.Arrays;

/**
 * u:x(x₁,x₂,x₃...xₘ) → y(x₁,x₂,x₃...xₙ)
 */
public class Identity extends DomainCheckedFunction<Identity> {
	@Override
	public float[] doApply(float[] input) {
		return Arrays.copyOf(input, input.length);
	}
}
