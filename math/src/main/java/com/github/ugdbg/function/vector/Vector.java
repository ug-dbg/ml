package com.github.ugdbg.function.vector;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A vector of float values.
 * TODO : parameterize 'float' with 'T extends Number' ?
 */
public class Vector implements Serializable  {
	
	private final float[] value;
	private transient String format = "%.2f";

	public Vector(Float[] value) {
		this(ArrayUtils.toPrimitive(value));
	}
	
	public Vector(float[] value) {
		this.value = Arrays.copyOf(value, value.length);
	}

	public float[] getValue() {
		return this.value;
	}

	public Vector format(String format) {
		this.format = format;
		return this;
	}
	
	public int dimension() {
		return this.value.length;
	}
	
	public float at(int i) {
		return this.value[i];
	}
	
	public Vector add(float[] other) {
		return new Vector(this.operation(other, Float::sum));
	}
	
	public Vector add(Vector other) {
		return new Vector(this.operation(other.value, Float::sum));
	}
	
	public Vector substract(float[] other) {
		return new Vector(this.operation(other, (a, b) -> a - b));
	}
	
	public Vector substract(Vector other) {
		return this.substract(other.value);
	}
	
	public Vector mult(float[] other) {
		return new Vector(this.operation(other, (a,b) -> a*b));
	}
	
	public Vector mult(Vector other) {
		return new Vector(this.operation(other.value, (a,b) -> a*b));
	}
	
	public float scalar(Vector other) {
		return this.mult(other).sum();
	}
	
	public float sum() {
		return (float) Arrays.stream(ArrayUtils.toObject(this.value)).mapToDouble(f -> (double) f).sum();
	}
	
	public Matrix outer(Vector other) {
		Matrix outer = new Matrix(this.value.length, other.value.length);
		for (int i = 0; i < this.value.length; i++) {
			for (int j = 0; j < other.value.length; j++) {
				outer.at(i, j, this.value[i] * other.value[j]);
			}
		}
		return outer;
	}

	@Override
	public String toString() {
		return "[" + this.format(ArrayUtils.toObject(this.value)) + "]";
	}
	
	private String format(Float[] vector) {
		return Joiner.on(" ").join(Arrays.stream(vector).map(this::format).collect(Collectors.toList()));
	}
	
	private String format(float value) {
		return String.format(this.format, value);
	}
	
	private float[] operation(float[] other, Operation op) {
		if (this.value.length != other.length) {
			throw new IllegalArgumentException(
				"Could not execute operation : this length [" + this.value.length + "] != [" + other.length + "]"
			);
		}
		float[] out = new float[this.value.length];
		for(int i = 0; i < this.value.length; i++) {
			out[i] = op.apply(this.value[i], other[i]);
		}
		return out;
	}

	interface Operation {
		float apply(float a, float b);
	}
}
