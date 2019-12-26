package com.github.ugdbg.datatypes.array;

import com.github.ugdbg.NumberUtils;
import com.github.ugdbg.datatypes.TYPE;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

public class PrimitiveDoubleArray implements NumericArray {
	private final double[] array;

	public PrimitiveDoubleArray(int length) {
		this.array = new double[length];
	}

	public PrimitiveDoubleArray(double[] values) {
		this.array = values;
	}
	
	@Override
	public TYPE getType() {
		return TYPE.PDOUBLE;
	}

	@Override
	public int length() {
		return this.array.length;
	}
	
	@Override
	public PrimitiveDoubleArray zero() {
		IntStream.range(0, this.array.length).forEach(index -> this.array[index] = 0);
		return this;
	}
	
	@Override
	public PrimitiveDoubleArray copy() {
		return new PrimitiveDoubleArray(Arrays.copyOf(this.array, this.array.length));
	}
	
	@Override
	public PrimitiveDoubleArray oneHot(int index) {
		return (PrimitiveDoubleArray) this.operation(
			(Operation<PrimitiveDoubleArray>) (array, i) -> array.array[i] = index == i ? 1 : 0
		);
	}
	
	@Override
	public int topIndex() {
		return this.indexStream().boxed().max(Comparator.comparing(index -> this.array[index])).orElse(-1);
	}
	
	@Override
	public Double at(int index) {
		return this.array[index];
	}
	
	@Override
	public void at(int i, Number value) {
		this.array[i] = NumberUtils.convertNumberToTargetClass(value, double.class);
	}
	
	@Override
	public void sum(NumericArray with) {
		double[] others = with.doubles();
		IntStream.range(0, this.array.length).forEach(i -> this.array[i] += others[i]);
	}

	@Override
	public void sub(NumericArray with) {
		double[] others = with.doubles();
		IntStream.range(0, this.array.length).forEach(i -> this.array[i] -= others[i]);
	}

	@Override
	public void mul(NumericArray with) {
		double[] others = with.doubles();
		IntStream.range(0, this.array.length).forEach(i -> this.array[i] *= others[i]);
	}

	@Override
	public void div(NumericArray with) {
		double[] others = with.doubles();
		IntStream.range(0, this.array.length).forEach(i -> this.array[i] /= others[i]);
	}
	
	@Override
	public void mul(float with) {
		this.indexStream().forEach(i -> this.array[i] = this.array[i] * with);
	}

	@Override
	public void mul(double with) {
		this.indexStream().forEach(i -> this.array[i] = this.array[i] * with);
	}

	@Override
	public void mul(BigDecimal with) {
		this.indexStream().forEach(i -> this.array[i] = this.array[i] * with.doubleValue());
	}

	@Override
	public Number sum() {
		return this.indexStream().mapToObj(i -> this.array[i]).reduce(0d, Double::sum);
	}
	
	@Override
	public Number linearCombination(NumericArray with) {
		return linearCombination(this.array, with.doubles());
	}

	@Override
	public float linearCombinationToFloat(NumericArray with) {
		return (float) linearCombination(this.array, with.doubles());
	}

	@Override
	public double linearCombinationToDouble(NumericArray with) {
		return linearCombination(this.array, with.doubles());
	}

	@Override
	public BigDecimal linearCombinationToDecimal(NumericArray with) {
		return new BigDecimal(linearCombination(this.array, with.doubles()));
	}

	@Override
	public double[] doubles() {
		return this.array;
	}

	@Override
	public float[] floats() {
		return Floats.toArray(Doubles.asList(this.array));
	}

	@Override
	public BigDecimal[] decimals() {
		return Arrays.stream(this.array).mapToObj(BigDecimal::new).toArray(BigDecimal[]::new);
	}
	
	/**
	 * Simple linear combination between an input vector and a column. <br>
	 * Sizes must match !
	 * @param input the input vector
	 * @return the linear combination between an input and a column.
	 */
	private static double linearCombination(double[] input, double[] column){
		if(input.length != column.length){
			throw new IllegalArgumentException(
				"Input size [" + input.length + "] does not match column size [" + column.length + "]"
			);
		}
		double out = 0;
		for (int i = 0; i < input.length; i++) {
			out += input[i] * column[i];
		}
		return out;
	}
}