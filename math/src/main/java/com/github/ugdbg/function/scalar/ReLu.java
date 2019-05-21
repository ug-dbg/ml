package com.github.ugdbg.function.scalar;

/**
 * Rectified Linear Units function.
 * <br>
 * y:x → x if x > 0, 0 else.
 */
public class ReLu extends DomainCheckedFunction<ReLu> implements Derivable {
	
	@Override
	public Function derive() {
		return new Function() {
			@Override
			public float doApply(float input) {
				return input > 0 ? 1 : 0;
			}

			@Override
			public String label() {
				return "1 if x > 0, 0 else";
			}
		};
	}

	@Override
	public float doApply(float input) {
		return input > 0 ? input : 0;
	}

	@Override
	public String label() {
		return "max(O, x)";
	}
}
