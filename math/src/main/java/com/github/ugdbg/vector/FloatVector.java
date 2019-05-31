package com.github.ugdbg.vector;

import com.github.ugdbg.vector.format.FloatFormat;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;

/**
 * A vector of Float values.
 */
public class FloatVector extends Vector<Float> implements Serializable  {
	
	public FloatVector(Float[] value) {
		super(value);
		this.format = new FloatFormat(3, 2);
	}
	
	public FloatVector(float[] value) {
		super(ArrayUtils.toObject(value));
		this.format = new FloatFormat(3, 2);
	}
	
	public static FloatVector of(float... values) {
		return new FloatVector(values);
	}
	
	public static FloatVector of(int dimension) {
		return new FloatVector(new float[dimension]);
	}
	
	public static FloatVector oneHot(int index, int length) {
		return new FloatVector(Vector.oneHot(Float.class, index, length).value);
	}

	public float[] getPrimitiveValue() {
		return ArrayUtils.toPrimitive(this.value);
	}

	@Override
	protected Float zero() {
		return 0f;
	}

	@Override
	protected Float one() {
		return 1f;
	}

	@Override
	protected Float add(Float a, Float b) {
		return a + b;
	}

	@Override
	protected Float sub(Float a, Float b) {
		return a - b;
	}

	@Override
	protected Float mult(Float a, Float b) {
		return a * b;
	}

	@Override
	protected Float mult(Float a, float scalar) {
		return a * scalar;
	}

	@Override
	protected Float div(Float a, Float b) {
		return a / b;
	}
}
