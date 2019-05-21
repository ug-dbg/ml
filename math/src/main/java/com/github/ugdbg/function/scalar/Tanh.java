package com.github.ugdbg.function.scalar;

/**
 * u:x → tanh(x)
 */
public class Tanh extends DomainCheckedFunction<Tanh> implements Derivable {
	@Override
	public float doApply(float input) {
		return (float) Math.tanh(input);
	}

	/**
	 * u':x → 1 - tanh²(x)
	 * @return a new Function instance that is the tanh derivative
	 */
	@Override
	public Function derive() {
		return (Function) input -> (float) (1 - Math.pow(Tanh.this.apply(input), 2));
	}

	@Override
	public String label() {
		return "tanh(x)";
	}

	@Override
	public String toString() {
		return "u:x → " + this.label();  
	}
}
