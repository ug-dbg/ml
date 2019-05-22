package com.github.ugdbg.function.vector.domain;

import com.github.ugdbg.function.scalar.domain.Domains;

/**
 * Some common vector domains.
 * <ul>
 *     <li>ℝⁿ : {@link #R(int)}</li>
 *     <li>(ℝ ∪ {−∞, +∞})ⁿ (Extended real number line) : {@link #R_closed(int)}</li>
 *     <li>ℝ+ⁿ ({x ∈ ℝ | x ≥ 0})ⁿ : {@link #R_plus(int)}</li>
 *     <li>ℝⁿ, ∀n : {@link #R_ANY}</li>
 * </ul>
 */
public class VDomains {

	public static final VDomain R_ANY = VDomain.anyDimension(Domains.R);
	
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
