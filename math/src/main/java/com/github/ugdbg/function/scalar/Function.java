package com.github.ugdbg.function.scalar;

import java.util.Objects;

public interface Function {
	float apply(float input);
	
	default Function compose(Function with) {
		return input -> Function.this.apply(with.apply(input));
	}
	
	default String label() {
		return Objects.toString(this);
	}
}
