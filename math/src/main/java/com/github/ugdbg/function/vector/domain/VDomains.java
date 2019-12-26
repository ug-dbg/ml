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

	/** ℝⁿ, ∀n : dimension of the input vector does not matter. */
	public static final VDomain R_ANY = VDomain.anyDimension(Domains.R);
	
	/** 
	 * ℝⁿ.
	 * V(x₁,x₂,x₃...xₙ) ∈ ℝⁿ if and only if : 
	 * <ul>
	 *     <li>{@link com.github.ugdbg.vector.Vector#dimension()} = n</li>
	 *     <li>∀ k ∈ [1, n], xₖ ∈ ℝ</li>
	 * </ul>
	 */
	public static VDomain R(int n) {
		return VDomain.of(Domains.R, n);
	}
	
	/** 
	 * (ℝ ∪ {−∞, +∞})ⁿ.
	 * V(x₁,x₂,x₃...xₙ) ∈ ℝⁿ if and only if : 
	 * <ul>
	 *     <li>{@link com.github.ugdbg.vector.Vector#dimension()} = n</li>
	 *     <li>∀ k ∈ [1, n], xₖ ∈ (ℝ ∪ {−∞, +∞})</li>
	 * </ul>
	 */
	public static VDomain R_closed(int n) {
		return VDomain.of(Domains.R_CLOSED, n);
	}

	/** 
	 * ℝ+ⁿ.
	 * V(x₁,x₂,x₃...xₙ) ∈ ℝⁿ if and only if : 
	 * <ul>
	 *     <li>{@link com.github.ugdbg.vector.Vector#dimension()} = n</li>
	 *     <li>∀ k ∈ [1, n], xₖ ∈ ℝ+</li>
	 * </ul>
	 */
	public static VDomain R_plus(int n) {
		return VDomain.of(Domains.R_PLUS, n);
	}
}
