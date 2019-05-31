package com.github.ugdbg.vector.primitive;

import com.github.ugdbg.vector.IVector;
import com.github.ugdbg.vector.format.FloatFormat;
import com.github.ugdbg.vector.format.Format;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * A vector implementation, using primitive float values.
 * <br>
 * Performance is a lot better than {@link com.github.ugdbg.vector.Vector}.
 */
public class FloatVector implements IVector<FloatVector>, Serializable  {
	
	private final float[] value;
	private transient Format format = new FloatFormat(3, 2);

	public FloatVector(Float[] value) {
		this(ArrayUtils.toPrimitive(value));
	}
	
	public FloatVector(float[] value) {
		this.value = Arrays.copyOf(value, value.length);
	}

	public static FloatVector of(float... values) {
		return new FloatVector(values);
	}
	
	public static FloatVector of(int dimension) {
		return new FloatVector(new float[dimension]);
	}
	
	public static FloatVector oneHot(int index, int length) {
		FloatVector oneHot = new FloatVector(new float[length]);
		oneHot.value[index] = 1;
		return oneHot;
	}
	
	public static FloatVector sum(FloatVector a, FloatVector b) {
		return FloatVector.of(Math.min(a.dimension(), b.dimension())).operation((v, i) -> v.at(i, a.at(i) + b.at(i)));
	}
	
	public static FloatVector sub(FloatVector a, FloatVector b) {
		return FloatVector.of(Math.min(a.dimension(), b.dimension())).operation((v, i) -> v.at(i, a.at(i) - b.at(i)));
	}
	
	@Override
	public Float[] getValue() {
		return ArrayUtils.toObject(this.value);
	}
	
	public float[] getPrimitiveValue() {
		return this.value;
	}

	@Override
	public FloatVector format(Format format) {
		this.format = format;
		return this;
	}
	
	@Override
	public int dimension() {
		return this.value.length;
	}
	
	@Override
	public Float at(int i) {
		return this.value[i];
	}
	
	public float primitiveAt(int i) {
		return this.value[i];
	}
	
	@Override
	public void at(int i, Number value) {
		this.value[i] = value.floatValue();
	}
	
	public void at(int i, float value) {
		this.value[i] = value;
	}
	
	@Override
	public FloatVector sum(FloatVector other) {
		return this.operation((vector, i) -> vector.at(i, vector.primitiveAt(i) + other.primitiveAt(i)));
	}
	
	@Override
	public FloatVector sub(FloatVector other) {
		return this.operation((vector, i) -> vector.at(i, vector.primitiveAt(i) - other.primitiveAt(i)));
	}
	
	@Override
	public FloatVector mult(FloatVector other) {
		return this.operation((vector, i) -> this.at(i, this.primitiveAt(i) * other.primitiveAt(i)));
	}
	
	@Override
	public FloatVector div(FloatVector other) {
		return this.operation((vector, i) -> this.at(i, this.primitiveAt(i) / other.primitiveAt(i)));
	}

	public FloatVector mult(float scalar) {
		return this.operation((vector, i) -> vector.at(i, vector.primitiveAt(i) * scalar));
	}

	@Override
	public Double scalar(FloatVector other) {
		return this.mult(other).sum();
	}
	
	@Override
	public Double sum() {
		return Arrays.stream(ArrayUtils.toObject(this.value)).mapToDouble(f -> (double) f).sum();
	}
	
	@Override
	public Double avg(boolean abs) {
		ToDoubleFunction<Float> toDouble = abs ? Math::abs : f -> (double) f;
		return this.doubleStream(toDouble).average().orElse(0);
	}
	
	@Override
	public Double max(boolean abs) {
		ToDoubleFunction<Float> toDouble = abs ? Math::abs : f -> (double) f;
		return this.doubleStream(toDouble).max().orElse(0);
	}
	
	@Override
	public FloatVector normalize(float min, float max) {
		return this.operation((vector, i) -> this.at(i, (this.at(i) - min) / (max - min)));
	}

	@Override
	public String toString() {
		return "[" + this.format(ArrayUtils.toObject(this.value)) + "]";
	}
	
	private DoubleStream doubleStream(ToDoubleFunction<? super Float> function) {
		return Arrays.stream(ArrayUtils.toObject(this.value)).mapToDouble(function);
	}
	
	private String format(Float[] vector) {
		return Joiner.on(" ").join(Arrays.stream(vector).map(this::format).collect(Collectors.toList()));
	}
	
	private String format(float value) {
		return this.format.format(value);
	}
}
