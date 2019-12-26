package com.github.ugdbg.function.vector;

import com.github.ugdbg.vector.Vector;

import java.util.Arrays;

/**
 * u:x(x₁,x₂,x₃...xₘ) → y(x₁,x₂,x₃...xₙ)
 */
public class Identity extends DomainCheckedFunction<Identity> {
	@Override
	public Vector doApply(Vector input) {
		return input.copy();
	}
}
