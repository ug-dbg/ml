package com.github.ugdbg.function.scalar;

import com.github.ugdbg.function.vector.VDerivable;
import com.github.ugdbg.function.vector.VFunction;

/**
 * An interface for ℝ functions that can be derived.
 */
public interface Derivable extends Function {
	Function derive();
	
	default Derivable compose(Derivable with) {
		return ComposedDerivable.of(this, with);
	}
	
	/**
	 * Get the vectorial function for this : f:x(x₁,x₂,x₃...xₘ) → y(y₁,y₂,y₃...yₙ) where yᵢ = this(xᵢ)   
	 * @return a new vectorial function instance for this function 
	 */
	default VDerivable vectorial() {
		return new VDerivable() {
			@Override
			public VFunction derive() {
				return Derivable.this.derive().vectorial();
			}

			@Override
			public String label() {
				return Derivable.this.label();
			}

			@Override
			public float[] apply(float[] input) {
				return Derivable.this.apply(input);
			}
		};
	}
}
