package com.github.ugdbg.datatypes.array;

import com.github.ugdbg.NumberUtils;
import com.github.ugdbg.datatypes.TYPE;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

public class PrimitiveFloatArray implements NumericArray {
	private final float[] array;

	public PrimitiveFloatArray(int length) {
		this.array = new float[length];
	}

	public PrimitiveFloatArray(float[] values) {
		this.array = values;
	}
	
	@Override
	public TYPE getType() {
		return TYPE.PFLOAT;
	}

	@Override
	public int length() {
		return this.array.length;
	}
	
	@Override
	public PrimitiveFloatArray zero() {
		IntStream.range(0, this.array.length).forEach(index -> this.array[index] = 0);
		return this;
	}

	@Override
	public PrimitiveFloatArray copy() {
		return new PrimitiveFloatArray(Arrays.copyOf(this.array, this.array.length));
	}

	@Override
	public PrimitiveFloatArray oneHot(int index) {
		return (PrimitiveFloatArray) this.operation(
			(Operation<PrimitiveFloatArray>) (array, i) -> array.array[i] = index == i ? 1 : 0
		);
	}

	@Override
	public int topIndex() {
		return this.indexStream().boxed().max(Comparator.comparing(index -> this.array[index])).orElse(-1);
	}

	@Override
	public Float at(int index) {
		return this.array[index];
	}
	
	@Override
	public void at(int i, Number value) {
		this.array[i] = NumberUtils.convertNumberToTargetClass(value, float.class);
	}
	
	@Override
	public void sum(NumericArray with) {
		float[] others = with.floats();
		IntStream.range(0, this.array.length).forEach(i -> this.array[i] += others[i]);
	}

	@Override
	public void sub(NumericArray with) {
		float[] others = with.floats();
		IntStream.range(0, this.array.length).forEach(i -> this.array[i] -= others[i]);
	}

	@Override
	public void mul(NumericArray with) {
		float[] others = with.floats();
		IntStream.range(0, this.array.length).forEach(i -> this.array[i] *= others[i]);
	}

	@Override
	public void div(NumericArray with) {
		float[] others = with.floats();
		IntStream.range(0, this.array.length).forEach(i -> this.array[i] /= others[i]);
	}
	
	@Override
	public void mul(float with) {
		this.indexStream().forEach(i -> this.array[i] = this.array[i] * with);
	}

	@Override
	public void mul(double with) {
		this.indexStream().forEach(i -> this.array[i] = this.array[i] * (float) with);
	}

	@Override
	public void mul(BigDecimal with) {
		this.indexStream().forEach(i -> this.array[i] = this.array[i] * with.floatValue());
	}

	@Override
	public Number sum() {
		return this.indexStream().mapToObj(i -> this.array[i]).reduce(0f, Float::sum);
	}
	
	@Override
	public Number linearCombination(NumericArray with) {
		return linearCombination(this.array, with.floats());
	}

	@Override
	public float linearCombinationToFloat(NumericArray with) {
		return linearCombination(this.array, with.floats());
	}

	@Override
	public double linearCombinationToDouble(NumericArray with) {
		return linearCombination(this.array, with.floats());
	}

	@Override
	public BigDecimal linearCombinationToDecimal(NumericArray with) {
		return new BigDecimal(linearCombination(this.array, with.floats()));
	}

	@Override
	public float[] floats() {
		return this.array;
	}

	@Override
	public double[] doubles() {
		return IntStream.range(0, this.array.length).mapToDouble(i -> this.array[i]).toArray();
	}

	@Override
	public BigDecimal[] decimals() {
		return IntStream
			.range(0, this.array.length)
			.mapToObj(i -> new BigDecimal(this.array[i]))
			.toArray(BigDecimal[]::new);
	}
	
	@Override
	public void normalize(Number min, Number max) {
		this.operation((vector, i) -> this.floats()[i] = normalize(this.floats()[i], min, max));
	}
		
	/**
	 * Simple linear combination between an input vector and a column. <br>
	 * Sizes must match !
	 * @param input the input vector
	 * @return the linear combination between an input and a column.
	 */
	private static float linearCombination(float[] input, float[] column){
		if(input.length != column.length){
			throw new IllegalArgumentException(
				"Input size [" + input.length + "] does not match column size [" + column.length + "]"
			);
		}
		float out = 0;
		for (int i = 0; i < input.length; i++) {
			out += input[i] * column[i];
		}
		return out;
	}
	
	private static float normalize(float value, Number min, Number max) {
		return (value - min.floatValue()) / (max.floatValue() - min.floatValue());
	}
}