package com.github.ugdbg.function.vector;

/**
 * f:x(x₁,x₂,x₃...xₘ) → y(y₁,y₂,y₃...yₙ)
 * <br>
 * where f is a function that apply on a vector and return a vector and can be derived.
 */
public interface VDerivable extends VFunction {
	/**
	 * Get the derivative of the current function
	 * @return a new vectorial function that is the derivative of the current function.
	 */
	VFunction derive();
}
