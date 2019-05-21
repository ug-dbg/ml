package com.github.ugdbg.function.domain;

import com.github.ugdbg.function.scalar.domain.Segment;
import com.github.ugdbg.function.scalar.domain.Union;

import java.io.Serializable;

/**
 * The domain of a function is the set of argument values for which the function is defined.
 * <br>
 * That is, the function provides an "output" or value for each member of the domain.
 * <br>
 * It can either be a {@link Segment} or a union of domains : {@link Union}.
 */
public interface Domain<T> extends Comparable<Domain>, Serializable {

	/**
	 * Is the given input inside the domain ?
	 * @param x the input
	 * @return true if the current domain contains the input value
	 */
	boolean isIn(T x);

	/**
	 * Is this domain empty (i.e. ∅) ? 
	 * @return true if the current domain instance is empty
	 */
	boolean isEmpty();

	/**
	 * Create a new domain instance that is the intersection of the current domain and the given parameter.
	 * <br><br>
	 * The intersection A ∩ B of two domains A and B is the domain that contains all elements of A that also belong to B 
	 * (or equivalently, all elements of B that also belong to A), but no other elements.
	 * @param other the other domain
	 * @return a new Domain instance, intersection of 'this' and 'other'
	 */
	Domain<T> inter(Domain<T> other);

	/**
	 * Create a new domain instance this is the union of the current domain and the given parameter.
	 * @param other the other domain
	 * @return a {@link Union} for 'this' and 'other'
	 */
	Domain<T> union(Domain<T>[] other);
}
