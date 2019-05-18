package com.github.ugdbg.function.vector;

import com.github.ugdbg.function.FloatFormat;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.AtomicDouble;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

/**
 * A vector of float values.
 * TODO : parameterize 'float' with 'T extends Number' ?
 */
public class Vector implements Serializable  {
	
	private final float[] value;
	private transient FloatFormat format = new FloatFormat(3, 2);

	public Vector(Float[] value) {
		this(ArrayUtils.toPrimitive(value));
	}
	
	public Vector(float[] value) {
		this.value = Arrays.copyOf(value, value.length);
	}

	public static Vector of(float... values) {
		return new Vector(values);
	}
	
	public static Vector of(int dimension) {
		return new Vector(new float[dimension]);
	}
	
	public static Vector oneHot(int index, int length) {
		Vector oneHot = new Vector(new float[length]);
		oneHot.value[index] = 1;
		return oneHot;
	}
	
	public static Vector sum(Vector a, Vector b) {
		return Vector.of(Math.min(a.dimension(), b.dimension())).operation((v, i) -> v.at(i, a.at(i) + b.at(i)));
	}
	
	public static Vector sub(Vector a, Vector b) {
		return Vector.of(Math.min(a.dimension(), b.dimension())).operation((v, i) -> v.at(i, a.at(i) - b.at(i)));
	}
	
	public float[] getValue() {
		return this.value;
	}

	public Vector format(FloatFormat format) {
		this.format = format;
		return this;
	}
	
	public int dimension() {
		return this.value.length;
	}
	
	public String shortLabel() {
		return "V(" + this.dimension() + ")";
	}
	
	public float at(int i) {
		return this.value[i];
	}
	
	public void at(int i, float value) {
		this.value[i] = value;
	}
	
	public Vector sum(Vector other) {
		return this.operation((vector, i) -> vector.at(i, vector.at(i) + other.at(i)));
	}
	
	public Vector sub(Vector other) {
		return this.operation((vector, i) -> vector.at(i, vector.at(i) - other.at(i)));
	}
	
	public Vector mult(float scalar) {
		return this.operation((vector, i) -> vector.at(i, vector.at(i) * scalar));
	}
	
	public Vector mult(Vector other) {
		return this.operation((vector, i) -> this.at(i, this.at(i) * other.at(i)));
	}
	
	public float scalar(Vector other) {
		return this.mult(other).sum();
	}
	
	public float sum() {
		return (float) Arrays.stream(ArrayUtils.toObject(this.value)).mapToDouble(f -> (double) f).sum();
	}
	
	public float avg(boolean abs) {
		ToDoubleFunction<Float> toDouble = abs ? Math::abs : f -> (double) f;
		return (float) this.doubleStream(toDouble).average().orElse(0);
	}
	
	public float max(boolean abs) {
		ToDoubleFunction<Float> toDouble = abs ? Math::abs : f -> (double) f;
		return (float) this.doubleStream(toDouble).max().orElse(0);
	}
	
	public int topIndex() {
		AtomicInteger top = new AtomicInteger(-1);
		AtomicDouble value = new AtomicDouble(Float.NEGATIVE_INFINITY);
		this.operation((vector, i) -> {
			if (vector.at(i) > value.floatValue()) {
				top.set(i);
				value.set(vector.value[i]);
			}
		});
		return top.get();
	}
	
	public Vector normalize(float min, float max) {
		return this.operation((vector, i) -> this.at(i, (this.at(i) - min) / (max - min)));
	}
	
	public Matrix outer(Vector other) {
		return Matrix.outer(this, other);
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
	
	private Vector operation(Operation op) {
		for(int i = 0; i < this.value.length; i++) {
			op.doAt(this, i);
		}
		return this;
	}

	interface Operation {
		void doAt(Vector vector, int i);
	}
}
