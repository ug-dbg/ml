package com.github.ugdbg.function.scalar;

import com.github.ugdbg.function.vector.VFunction;
import com.github.ugdbg.function.vector.Vector;

import java.io.Serializable;
import java.util.Objects;

/**
 * An interface for ℝ functions.
 */
public interface Function extends Serializable {

	/**
	 * Apply the function on an input ℝ number.
	 * @param input the input float value
	 * @return the output float value of the function
	 */
	float apply(float input);

	/**
	 * Compose the current function with another one  : f ∘ g.
	 * <br>
	 * (this function) ∘ (with)
	 * <br>
	 * @param with the other function to compose this with.
	 * @return a new Function, composition of this function and the parameter.
	 */
	default Function compose(Function with) {
		return input -> Function.this.apply(with.apply(input));
	}

	/**
	 * A label for this function. Default to {@link Objects#toString(Object)}.
	 * <br>
	 * Override to give this function an explicit, human readable, label
	 * @return a label for this function.
	 */
	default String label() {
		return Objects.toString(this);
	}

	/**
	 * Get the vectorial function for this : f:x(x₁,x₂,x₃...xₘ) → y(y₁,y₂,y₃...yₙ) where yᵢ = this(xᵢ)   
	 * @return a new vectorial function instance for this function 
	 */
	default VFunction vectorial() {
		return (VFunction) this::apply;
	}

	/**
	 * Apply the current function to a vector.
	 * @param input the input vector
	 * @return an output vector y(y₁,y₂,y₃...yₙ) where yᵢ = this(xᵢ)
	 */
	default float[] apply(float[] input) {
		float[] out = new float[input.length];
		for (int i = 0; i < input.length; i++) {
			out[i] = this.apply(input[i]);
		}
		return out;
	}
	
	/**
	 * Apply the current function to a vector.
	 * @param input the input vector
	 * @return an output vector y(y₁,y₂,y₃...yₙ) where yᵢ = this(xᵢ)
	 */
	default Vector apply(Vector input) {
		return new Vector(this.apply(input.getValue()));
	}
}
