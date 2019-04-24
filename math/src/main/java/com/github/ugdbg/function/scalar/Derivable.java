package com.github.ugdbg.function.scalar;

public interface Derivable extends Function {
	Function derive();
	
	default Derivable compose(Derivable with) {
		return ComposedDerivable.of(this, with);
	}
}
