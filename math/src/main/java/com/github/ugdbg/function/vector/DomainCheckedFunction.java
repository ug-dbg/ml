package com.github.ugdbg.function.vector;

import com.github.ugdbg.function.vector.domain.VDomain;
import com.github.ugdbg.function.vector.domain.VDomains;

abstract class DomainCheckedFunction<F extends DomainCheckedFunction> implements VFunction {
	
	private boolean domainCheck = true;
	protected VDomain domain = VDomains.R_ANY;
	
	@SuppressWarnings("unchecked")
	public F domainCheck(boolean domainCheck) {
		this.domainCheck = domainCheck;
		return (F) this;
	}

	@SuppressWarnings("unchecked")
	public F onDomain(VDomain domain) {
		this.domain = domain;
		return (F) this;
	}
	
	@Override
	public boolean domainCheck() {
		return this.domainCheck;
	}
	
	@Override
	public VDomain domain() {
		return this.domain;
	}
}

