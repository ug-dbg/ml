package com.github.ugdbg.function.vector.domain;

import com.github.ugdbg.function.scalar.domain.Domains;

public class VDomains {
	
	public static VDomain R(int n) {
		return VDomain.of(Domains.R, n);
	}
	
	public static VDomain R_closed(int n) {
		return VDomain.of(Domains.R_CLOSED, n);
	}
	
	public static VDomain R_plus(int n) {
		return VDomain.of(Domains.R_PLUS, n);
	}
}
