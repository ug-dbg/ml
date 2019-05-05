package com.github.ugdbg.function.scalar.domain;

import com.github.ugdbg.function.domain.Domain;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A line segment is a part of a line that is bounded by two distinct end points.
 * It contains every point on the line between its endpoints. 
 * <br><br>
 * A closed line segment includes both endpoints. <br> 
 * An open line segment excludes both endpoints. <br>
 * A half-open line segment includes exactly one of the endpoints. <br>
 */
public class Segment implements Domain<Float> {

	private final LeftBorder  from;
	private final RightBorder to;

	/**
	 * A segment border : a number and open status.
	 * <br>
	 * Open status is not final : it can be updated using {@link #open(boolean, boolean)}.
	 */
	private static abstract class Border {
		final Float at;
		
		/** Border open status : true is open. false is closed. */
		boolean open;

		private Border(Float at, boolean open) {
			this.at = at;
			this.open = open;
		}

		String label() {
			return this.at == Float.NEGATIVE_INFINITY 
				? "-∞" 
				: (this.at == Float.POSITIVE_INFINITY ? "+∞" : String.valueOf(this.at)); 
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || this.getClass() != o.getClass()) return false;
			Border border = (Border) o;
			return this.open == border.open && this.at.equals(border.at);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.at, this.open);
		}
		
		boolean bigger(Float than) {
			return this.open ? this.at.compareTo(than) > 0 : this.at.compareTo(than) >= 0;
		}

		boolean smaller(Float than) {
			return this.open ? this.at.compareTo(than) < 0 : this.at.compareTo(than) <= 0;
		}
	}
	
	/**
	 * A segment left border : a number and open status.
	 * <br>
	 * This is mostly to handle labeling and comparison.
	 */
	private static class LeftBorder extends Border implements Comparable<LeftBorder> {
		private LeftBorder(Float at, boolean open) {
			super(at, open);
		}

		@Override
		String label() {
			return (this.open ? "]" : "[") + super.label();
		}

		@Override
		public int compareTo(LeftBorder o) {
			return new CompareToBuilder().append(this.at, o.at).append(this.open, o.open).toComparison();
		}
	}
	
	/**
	 * A segment right border : a number and open status.
	 * <br>
	 * This is mostly to handle labeling and comparison.
	 */
	private static class RightBorder extends Border implements Comparable<RightBorder> {
		private RightBorder(Float at, boolean open) {
			super(at, open);
		}
		
		@Override
		String label() {
			return super.label() + (this.open ? "[" : "]");
		}
		
		@Override
		public int compareTo(RightBorder o) {
			return new CompareToBuilder().append(this.at, o.at).append(!this.open, !o.open).toComparison();
		}
	}

	/**
	 * Private constructor, using explicit borders.
	 * @param from the left border
	 * @param to   the right border
	 */
	private Segment(LeftBorder from, RightBorder to) {
		this.from = from;
		this.to = to;
	}

	/**
	 * Constructor using left and right border values.
	 * Use {@link #open(boolean, boolean)} to set borders open statuses.
	 * @param from the left border value
	 * @param to   the right border value
	 */
	public Segment(Float from, Float to) {
		if (from == null || to == null) {
			throw new IllegalArgumentException("Segment cannot have a null bound !");
		}
		this.from = new LeftBorder(from, true);
		this.to   = new RightBorder(to,  true);
	}

	/**
	 * Set this segment borders open statuses.
	 * @param from  the left border open status. true is open. false is closed.
	 * @param right the right border open status. true is open. false is closed.
	 * @return the current Segment instance
	 */
	public Segment open(boolean from, boolean right) {
		this.from.open = from;
		this.to.open   = right;
		return this;
	}

	@Override
	public String toString() {
		if (this.isEmpty()) {
			return "∅";
		}
		return this.from.label() + ", " + this.to.label();
	}

	@Override
	public boolean isIn(Float x) {
		if (x == null) {
			throw new IllegalArgumentException("null value cannot be checked in segment [" + this + "]");
		}
		return this.from.smaller(x) && this.to.bigger(x);
	}
	
	@Override
	public boolean isEmpty() {
		return this.from.bigger(this.to.at) && this.to.smaller(this.from.at);
	}

	@Override
	public int compareTo(Domain o) {
		Segment lowest = o instanceof Segment ? (Segment) o : ((Union) o).lowest(); 
		return new CompareToBuilder().append(this.from, lowest.from).append(this.to, lowest.to).toComparison();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.from).append(this.to).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof Segment)) {
			return false;
		}
		return new EqualsBuilder().append(this.from, ((Segment) obj).from).append(this.to, ((Segment) obj).to).build();
	}

	@Override
	@SafeVarargs
	public final Domain<Float> union(Domain<Float>... other) {
		Union domain = new Union(other);
		domain.add(this);
		return domain;
	}

	@Override
	public Domain<Float> inter(Domain other) {
		if (other instanceof Segment) {
			return this.inter((Segment) other);
		}
		if (other instanceof Union) {
			return new Union(((Union) other).stream().map(d -> d.inter(this)).collect(Collectors.toList()));
		}
		
		throw new IllegalArgumentException("Unsupported domain type [" + other.getClass() + "]");
	}

	/**
	 * Create a new segment instance that is the intersection of the current segment and the given parameter
	 * @param other the other segment
	 * @return a new segment instance, intersection of 'this' and 'other'
	 */
	public Segment inter(Segment other) {
		return new Segment(ObjectUtils.max(this.from, other.from), ObjectUtils.min(this.to, other.to));
	}
	
	/**
	 * Create a new domain instance that is the intersection of the current segment and all of the given domains.
	 * @param others the other domains
	 * @return a new domain instance, intersection of 'this' and all the 'other' domains
	 */
	public Domain inter(Collection<Domain> others) {
		Domain current = this;
		for (Domain other : others) {
			current = current.inter(other);
		}
		return current;
	}
	
	/**
	 * Create a new domain instance that is the intersection of the current segment and all of the given domains.
	 * @param others the other domains
	 * @return a new domain instance, intersection of 'this' and all the 'other' domains
	 */
	public Domain inter(Domain... others) {
		return this.inter(Arrays.asList(others));
	}
}
