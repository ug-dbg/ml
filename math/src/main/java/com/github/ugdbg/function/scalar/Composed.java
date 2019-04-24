package com.github.ugdbg.function.scalar;

public interface Composed extends Function {
	
	Function f();
	
	Function g();

	@Override
	default float apply(float input) {
		return this.f().apply(this.g().apply(input));
	}
	
	static Composed of(Function f, Function g) {
		return new Composed() {
			@Override
			public Function f() {
				return f;
			}

			@Override
			public Function g() {
				return g;
			}
		};
	}
}
