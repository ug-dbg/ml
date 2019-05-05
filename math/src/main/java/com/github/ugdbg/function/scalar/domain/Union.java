package com.github.ugdbg.function.scalar.domain;

import com.github.ugdbg.function.domain.Domain;
import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * A union (denoted by ⋃) of a collection of domains is the domain of all elements in the collection.
 */
public class Union extends TreeSet<Domain<Float>> implements Domain<Float> {

	/**
	 * Default constructor : empty domain
	 */
	public Union() {}

	/**
	 * Create a new Union domain from this collection of domains 
	 * @param domains the collection of domains
	 */
	public Union(Collection<Domain<Float>> domains) {
		super(domains.stream().filter(d -> ! d.isEmpty()).collect(Collectors.toList()));
	}

	/**
	 * Create a new Union domain from this collection of domains 
	 * @param domains the collection of domains
	 */
	@SafeVarargs
	public Union(Domain<Float>... domains) {
		this(Arrays.asList(domains));
	}

	@Override
	public boolean isIn(Float x) {
		return this.stream().map(s -> s.isIn(x)).filter(Boolean::booleanValue).findFirst().orElse(false);
	}

	@Override
	public String toString() {
		return this.isEmpty() ? "∅" : Joiner.on(" ⋃ ").join(this);
	}

	@Override
	public boolean isEmpty() {
		return this.stream().allMatch(Domain::isEmpty);
	}

	
	@Override
	@SafeVarargs
	public final Union union(Domain<Float>... other) {
		Union domains = new Union(other);
		domains.add(this);
		return domains;
	}

	@Override
	public Union inter(Domain<Float> other) {
		return this.stream().map(d -> d.inter(other)).collect(Collectors.toCollection(Union::new));
	}

	// FIXME : using String comparison is not really a good idea. It should compare segments.
	@Override
	public int compareTo(Domain o) {
		return this.toString().compareTo(o.toString());
	}

	/**
	 * Get the lowest segment from this union of domains.
	 * <br>
	 * Comparison is done using the {@link #toString()} method : this is a convenience method only !
	 * @return the lowest segment. null if this union is empty
	 */
	public Segment lowest() {
		Domain lowest = this.pollFirst();
		if (lowest == null) {
			return null;
		}
		if (lowest instanceof Segment) {
			return (Segment) lowest;
		}
		return ((Union) lowest).lowest();
	}
}
