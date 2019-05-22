package com.github.ugdbg.function.vector.domain;

import com.github.ugdbg.function.domain.Domain;
import com.github.ugdbg.function.scalar.domain.Domains;
import com.github.ugdbg.function.vector.Vector;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * A domain for vectors.
 * <br>
 * e.g. ℝⁿ
 */
public class VDomain implements Domain<Vector> {
	
	private Domain<Float>[] domains;

	private VDomain(Domain<Float>[] domains) {
		this.domains = domains;
	}

	@SuppressWarnings("unchecked")
	public VDomain(int dimension) {
		this.domains = new Domain[dimension];
		IntStream.range(0, dimension).forEach(value -> this.domains[value] = Domains.R_CLOSED);
	}
	
	public static VDomain of(Domain<Float> domain, int dimension) {
		VDomain vDomain = new VDomain(dimension);
		IntStream.range(0, dimension).forEach(value -> vDomain.domains[value] = domain);
		return vDomain;
	}
	
	@SuppressWarnings("unchecked")
	public static VDomain anyDimension(Domain<Float> domain) {
		return new VDomain(new Domain[] {domain}) {
			@Override
			public int dimension() {
				return Integer.MAX_VALUE;
			}

			@Override
			public boolean isIn(Vector x) {
				return Arrays.stream(ArrayUtils.toObject(x.getValue())).allMatch(domain::isIn);
			}

			@Override
			public String toString() {
				return "(" + domain.toString() + ")ⁿ";
			}
		};
	}
	
	public int dimension() {
		return this.domains.length;
	}

	@Override
	public boolean isIn(Vector x) {
		this.dimensionCheck(x);
		return ! IntStream.range(0, this.domains.length)
			.filter(value -> ! this.domains[value].isIn(x.at(value)))
			.findFirst()
			.isPresent();
	}

	@Override
	public boolean isEmpty() {
		return Arrays.stream(this.domains).allMatch(Domain::isEmpty);
	}

	@Override
	@SuppressWarnings("unchecked")
	public VDomain inter(Domain other) {
		Domain[] inter = new Domain[this.domains.length];
		IntStream.range(0, this.domains.length).forEach(index -> inter[index] = this.domains[index].inter(other));
		return new VDomain(inter);
	}

	@Override
	@SuppressWarnings("unchecked")
	public VDomain union(Domain[] other) {
		Domain[] union = new Domain[this.domains.length];
		IntStream.range(0, this.domains.length).forEach(index -> union[index] = this.domains[index].union(other));
		return new VDomain(union);
	}

	@Override
	public int compareTo(Domain o) {
		if (! (o instanceof VDomain)) {
			return -1;
		}
		if (this.dimension() != ((VDomain) o).dimension()) {
			return this.dimension() - ((VDomain) o).dimension(); 
		}
		
		CompareToBuilder builder = new CompareToBuilder();
		for (int i = 0; i < this.domains.length; i++) {
			builder.append(this.domains[i], ((VDomain) o).domains[i]);
		}
		return builder.toComparison();
	}

	@Override
	public String toString() {
		return "(" + Joiner.on(") X (").join(this.domains) + ")";
	}

	private void dimensionCheck(Vector input) {
		if (this.dimension() != input.dimension()) {
			throw new RuntimeException(
				"Vector [" + input.shortLabel() + "] does not match domain dimension [" + this.domains.length + "]"
			);
		}
	}
}
