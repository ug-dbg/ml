package com.github.ugdbg.function.vector;

import java.io.Serializable;

/**
 * f:x(x₁,x₂,x₃...xₘ) → y(y₁,y₂,y₃...yₙ)
 * <br>
 * An interface for functions that apply on a vector and return a vector.
 */
public interface VFunction extends Serializable {

	/**
	 * Apply the function to an input vector.
	 * @param input the input vector
	 * @return the output
	 */
	float[] apply(float[] input);
	
	/**
	 * Apply the function to an input vector.
	 * <br>
	 * Convenience method to {@link #apply(float[])}.
	 * @param input the input vector
	 * @return the output
	 */
	default Vector apply(Vector input) {
		return new Vector(this.apply(input.getValue()));
	}
	
}
