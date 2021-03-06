package com.github.ugdbg.function.scalar;

/**
 * u:x → f(x) * g(x)
 */
public class Product extends DomainCheckedFunction<Product> implements Derivable {
	
	private final Derivable f;
	
	private final Derivable g;

	public Product(Derivable f, Derivable g) {
		this.f = f;
		this.g = g;
		this.domainCheck(f.domainCheck() && g.domainCheck());
		if (f.domain() != null && g.domain() != null) {
			this.domain = f.domain().inter(g.domain());
		}
	}
	
	@Override
	public Function derive() {
		return input -> {
			float f = Product.this.f.apply(input);
			float g = Product.this.g.apply(input);
			float fPrime = Product.this.f.derive().apply(input);
			float gPrime = Product.this.g.derive().apply(input);
			return fPrime * g + f * gPrime;
		};
	}

	@Override
	public float doApply(float input) {
		return this.f.apply(input) * this.g.apply(input);
	}

	@Override
	public String label() {
		return this.f.toString() + " + " + this.g.toString();
	}
	
	@Override
	public String toString() {
		return "u:x → " + this.label();
	}
}
