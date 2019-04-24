package com.github.ugdbg.function.scalar;

public interface Function {
	float apply(float input);
	
	default Function compose(Function with) {
		return input -> Function.this.apply(with.apply(input));
	}
}
