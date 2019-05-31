package com.github.ugdbg.function.scalar.domain;

import com.github.ugdbg.function.domain.Domain;

import static java.lang.Float.*;

/**
 * Some common domains.
 * <ul>
 *     <li>ℝ : {@link #R}</li>
 *     <li>ℝ ∪ {−∞, +∞} (Extended real number line) : {@link #R_CLOSED}</li>
 *     <li>ℝ* : {@link #R_STAR}</li>
 *     <li>ℝ+ ({x ∈ ℝ | x ≥ 0}) : {@link #R_PLUS}</li>
 *     <li>ℝ- ({x ∈ ℝ | x ≤ 0}): {@link #R_MINUS}</li>
 *     <li>ℝ+* ({x ∈ ℝ | x > 0}) : {@link #R_PLUS_STAR}</li>
 *     <li>ℝ- ({x ∈ ℝ | x < 0}) : {@link #R_MINUS_STAR}</li>
 * </ul>
 */
public class Domains {
	
	private Domains(){}
	
	public static final Domain<Float> R = new Segment(NEGATIVE_INFINITY, POSITIVE_INFINITY) {
		@Override public String toString() { return "ℝ";}
	};
	
	public static final Domain<Float> R_CLOSED = new Segment(NEGATIVE_INFINITY, POSITIVE_INFINITY).open(false, false);
	
	public static final Domain<Float> R_PLUS = new Segment(0F, POSITIVE_INFINITY){
		@Override public String toString() { return "ℝ+";}
	}.open(false, true);
	
	public static final Domain<Float> R_PLUS_CLOSED = new Segment(0F, POSITIVE_INFINITY).open(false, false);
	
	public static final Domain<Float> R_MINUS = new Segment(NEGATIVE_INFINITY, 0F){
		@Override public String toString() { return "ℝ-";}
	}.open(true, false);
	
	public static final Domain<Float> R_PLUS_STAR = new Segment(0F, POSITIVE_INFINITY){
		@Override public String toString() { return "ℝ+*";}
	}.open(true, true);
	
	public static final Domain<Float> R_MINUS_STAR = new Segment(NEGATIVE_INFINITY, 0F){
		@Override public String toString() { return "ℝ-*";}
	}.open(true, true);
	
	public static final Domain<Float> R_STAR = new Union(R_MINUS_STAR, R_PLUS_STAR){
		@Override public String toString() { return "ℝ*";}
	};
}
