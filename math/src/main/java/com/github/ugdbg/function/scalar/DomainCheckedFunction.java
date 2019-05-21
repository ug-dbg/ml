package com.github.ugdbg.function.scalar;

import com.github.ugdbg.function.domain.Domain;
import com.github.ugdbg.function.scalar.domain.Domains;

abstract class DomainCheckedFunction<F extends DomainCheckedFunction> implements Function {
	
	private boolean domainCheck = false;
	protected Domain<Float> domain = Domains.R_CLOSED;
	
	@SuppressWarnings("unchecked")
	public F domainCheck(boolean domainCheck) {
		this.domainCheck = domainCheck;
		return (F) this;
	}

	@SuppressWarnings("unchecked")
	public F onDomain(Domain<Float> domain) {
		this.domain = domain;
		return (F) this;
	}
	
	@Override
	public boolean domainCheck() {
		return this.domainCheck;
	}
	
	@Override
	public Domain<Float> domain() {
		return this.domain;
	}
}
