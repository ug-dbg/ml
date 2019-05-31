package com.github.ugdbg.function.vector;

import com.github.ugdbg.function.domain.DomainCheckException;
import com.github.ugdbg.function.vector.domain.VDomain;
import com.github.ugdbg.function.vector.domain.VDomains;
import com.github.ugdbg.vector.primitive.FloatVector;

import java.io.Serializable;
import java.util.Objects;

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
	float[] doApply(float[] input);
	
	default float[] apply(float[] input) {
		FloatVector inputVector = FloatVector.of(input);
		if (this.domainCheck() && this.domain() != null && ! this.domain().isIn(inputVector)) {
			throw new DomainCheckException(this, inputVector);
		}
		return this.doApply(input);
	}

	/**
	 * What is the domain of this function ? 
	 * Default to {@link VDomains#R_ANY}.
	 * @return the domain for the function 
	 */
	default VDomain domain() {
		return VDomains.R_ANY;
	}

	/**
	 * Should the domain be checked ? 
	 * <br>
	 * Default to false;
	 * @return true if the domain must be checked before every {@link #doApply(float[])} call.
	 */
	default boolean domainCheck() {
		return false;
	}
	
	/**
	 * Apply the function to an input vector.
	 * <br>
	 * Convenience method to {@link #apply(float[])}.
	 * @param input the input vector
	 * @return the output
	 */
	default FloatVector apply(FloatVector input) {
		return FloatVector.of(this.apply(input.getPrimitiveValue()));
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
}
