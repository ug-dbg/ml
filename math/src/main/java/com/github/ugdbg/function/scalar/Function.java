package com.github.ugdbg.function.scalar;

import com.github.ugdbg.NumberUtils;
import com.github.ugdbg.function.domain.Domain;
import com.github.ugdbg.function.domain.DomainCheckException;
import com.github.ugdbg.function.scalar.domain.Domains;
import com.github.ugdbg.function.vector.VFunction;
import com.github.ugdbg.vector.Vector;

import java.io.Serializable;
import java.math.MathContext;
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
	float doApply(float input);

	/**
	 * Apply the function on an input ℝ number.
	 * <br>
	 * Defaults to {@link #doApply(float)} using {@link Number#floatValue()}.
	 * <br>
	 * Override to provide a specific implementation
	 * @param input the input value
	 * @return the output value of the function
	 */
	default Number doApply(Number input) {
		return this.doApply(input.floatValue());
	}
	
	/**
	 * Check the domain and apply the function on an input ℝ number.
	 * @param input the input float value
	 * @return the output float value of the function
	 * @throws DomainCheckException if domain check is enable, domain is not null and input is outside of domain.
	 */
	default float apply(float input) {
		if (this.domainCheck() && this.domain() != null && ! this.domain().isIn(input)) {
			throw new DomainCheckException(this, input);
		}
		return this.doApply(input);
	}

	/**
	 * What is the domain of this function ?
	 * <br>
	 * Default to {@link Domains#R}
	 * @return the function domain
	 */
	default Domain<Float> domain() {
		return Domains.R;
	}

	/**
	 * Should the domain be checked ? 
	 * <br>
	 * Default to false;
	 * @return true if the domain must be checked before every {@link #doApply(float)} call.
	 */
	default boolean domainCheck() {
		return false;
	}
	
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
		return new VFunction() {
			@Override
			public Vector doApply(Vector input) {
				return Function.this.apply(input);
			}

			@Override
			public String label() {
				return Function.this.label();
			}
		};
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
		return Vector.of(input.getValue().getType(), this.apply(input.floats()));
	}
	
	default MathContext mathContext() {
		return NumberUtils.MATH_CONTEXT;
	}
}
