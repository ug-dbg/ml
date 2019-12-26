package com.github.ugdbg.datatypes.array;

import com.github.ugdbg.NumberUtils;
import com.github.ugdbg.datatypes.TYPE;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

public class DecimalArray implements NumericArray {
	
	private final BigDecimal[] array;
	private RoundingMode roundingMode = RoundingMode.HALF_DOWN;
	
	public DecimalArray(int length) {
		this.array = new BigDecimal[length];
	}

	public DecimalArray(BigDecimal[] values) {
		this.array = values;
	}

	public RoundingMode getRoundingMode() {
		return this.roundingMode;
	}

	public void setRoundingMode(RoundingMode roundingMode) {
		this.roundingMode = roundingMode;
	}

	@Override
	public TYPE getType() {
		return TYPE.DECIMAL;
	}

	@Override
	public int length() {
		return this.array.length;
	}

	@Override
	public DecimalArray zero() {
		IntStream.range(0, this.array.length).forEach(index -> this.array[index] = BigDecimal.ZERO);
		return this;
	}
	
	@Override
	public DecimalArray copy() {
		return new DecimalArray(Arrays.copyOf(this.array, this.array.length));
	}

	@Override
	public DecimalArray oneHot(int index) {
		return (DecimalArray) this.operation(
			(Operation<DecimalArray>) (array, i) -> array.array[i] = index == i ? BigDecimal.ONE : BigDecimal.ZERO
		);
	}
	
	@Override
	public int topIndex() {
		return this.indexStream().boxed().max(Comparator.comparing(index -> this.array[index])).orElse(-1);
	}

	@Override
	public BigDecimal at(int index) {
		return this.array[index];
	}

	@Override
	public void at(int i, Number value) {
		this.array[i] = NumberUtils.convertNumberToTargetClass(value, BigDecimal.class);
	}

	@Override
	public void sum(NumericArray with) {
		BigDecimal[] others = with.decimals();
		IntStream.range(0, this.array.length).forEach(i -> this.array[i] = this.array[i].add(others[i]));
	}

	@Override
	public void sub(NumericArray with) {
		BigDecimal[] others = with.decimals();
		IntStream.range(0, this.array.length).forEach(i -> this.array[i] = this.array[i].subtract(others[i]));
	}

	@Override
	public void mul(NumericArray with) {
		BigDecimal[] others = with.decimals();
		IntStream.range(0, this.array.length).forEach(i -> this.array[i] = this.array[i].multiply(others[i]));
	}

	@Override
	public void div(NumericArray with) {
		BigDecimal[] others = with.decimals();
		IntStream.range(0, this.array.length).forEach(
			i -> this.array[i] = this.array[i].divide(others[i], this.roundingMode)
		);
	}

	@Override
	public void mul(float with) {
		this.indexStream().forEach(i -> this.array[i] = this.array[i].multiply(BigDecimal.valueOf(with)));
	}

	@Override
	public void mul(double with) {
		this.indexStream().forEach(i -> this.array[i] = this.array[i].multiply(BigDecimal.valueOf(with)));
	}

	@Override
	public void mul(BigDecimal with) {
		this.indexStream().forEach(i -> this.array[i] = this.array[i].multiply(with));
	}

	@Override
	public Number sum() {
		return this.indexStream().mapToObj(i -> this.array[i]).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@Override
	public Number linearCombination(NumericArray with) {
		return linearCombination(this.array, with.decimals());
	}

	@Override
	public float linearCombinationToFloat(NumericArray with) {
		return linearCombination(this.array, with.decimals()).floatValue();
	}

	@Override
	public double linearCombinationToDouble(NumericArray with) {
		return linearCombination(this.array, with.decimals()).doubleValue();
	}

	@Override
	public BigDecimal linearCombinationToDecimal(NumericArray with) {
		return linearCombination(this.array, with.decimals());
	}

	@Override
	public float[] floats() {
		return Floats.toArray(Doubles.asList(Arrays.stream(this.array).mapToDouble(BigDecimal::doubleValue).toArray()));
	}

	@Override
	public double[] doubles() {
		return Arrays.stream(this.array).mapToDouble(BigDecimal::doubleValue).toArray();
	}

	@Override
	public BigDecimal[] decimals() {
		return this.array;
	}
	
	/**
	 * Simple linear combination between an input vector and a column. <br>
	 * Sizes must match !
	 * @param input the input vector
	 * @return the linear combination between an input and a column.
	 */
	private static BigDecimal linearCombination(BigDecimal[] input, BigDecimal[] column){
		if(input.length != column.length){
			throw new IllegalArgumentException(
				"Input size [" + input.length + "] does not match column size [" + column.length + "]"
			);
		}
		BigDecimal out = new BigDecimal(0);
		for (int i = 0; i < input.length; i++) {
			try {
				out = out.add(input[i].multiply(column[i]));
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
		return out;
	}
}
