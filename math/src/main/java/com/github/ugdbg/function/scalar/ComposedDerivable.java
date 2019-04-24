package com.github.ugdbg.function.scalar;

/**
 * u:x → (f ∘ g) (x)
 */
public interface ComposedDerivable extends Composed, Derivable {
	
	Derivable f();
	
	Derivable g();
	
	static ComposedDerivable of(Derivable f, Derivable g) {
		return new ComposedDerivable() {
			@Override
			public Derivable f() {
				return f;
			}

			@Override
			public Derivable g() {
				return g;
			}

			@Override
			public Function derive() {
				// Chain rule !
				return input -> this.f().derive().apply(this.g().apply(input)) * this.g().derive().apply(input);
			}
		};
	}
	
}
