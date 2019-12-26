package com.github.ugdbg.vector;

import com.github.ugdbg.NumberUtils;
import com.github.ugdbg.datatypes.TYPE;
import com.github.ugdbg.datatypes.array.DecimalArray;
import com.github.ugdbg.datatypes.array.NumericArray;
import com.github.ugdbg.datatypes.array.PrimitiveDoubleArray;
import com.github.ugdbg.datatypes.array.PrimitiveFloatArray;
import com.github.ugdbg.vector.format.FloatFormat;
import com.github.ugdbg.vector.format.Format;
import com.google.common.base.Joiner;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A vector implementation based on a {@link NumericArray}.
 * <br>
 * The numeric data store implementation is delegated to {@link #value}.
 */
public class Vector implements Serializable {
	
	protected final NumericArray value;
	protected Format format = new FloatFormat(5, 5);

	private Vector(NumericArray value) {
		this.value = value;
	}
	
	public Vector withFormat(Format format) {
		this.format = format;
		return this;
	}
	
	public NumericArray getValue() {
		return this.value;
	}
	
	public float[] floats() {
		return this.value.floats();
	}
	
	public double[] doubles() {
		return this.value.doubles();
	}
		
	public BigDecimal[] decimals() {
		return this.value.decimals();
	}
	
	public int dimension() {
		return this.value.length();
	}
	
	public Number at(int i) {
		return this.value.at(i);
	}
	
	public void at(int i, Number value) {
		this.value.at(i, value);
	}

	public void zero(int i) {
		this.at(i, 0);
	}
	
	public static Vector of(NumericArray value) {
		return new Vector(value);
	}
	
	/**
	 * Constructor from explicit array values.
	 * @param values the vector components
	 */
	public static Vector of(TYPE type, float... values) {
		if (type == TYPE.PFLOAT) {
			return new Vector(new PrimitiveFloatArray(values));
		}
		
		Vector out = Vector.of(type, values.length);
		out.getValue().operation((array, index) -> array.at(index, values[index]));
		return out;
	}
	
	/**
	 * Constructor from explicit array values.
	 * @param values the vector components
	 */
	public static Vector of(float... values) {
		return new Vector(new PrimitiveFloatArray(values));
	}
	
	/**
	 * Constructor from explicit array values.
	 * @param values the vector components
	 */
	public static Vector of(double... values) {
		return new Vector(new PrimitiveDoubleArray(values));
	}
	
	/**
	 * Constructor from explicit array values.
	 * @param values the vector components
	 */
	public static Vector of(BigDecimal... values) {
		return new Vector(new DecimalArray(values));
	}
	
	/**
	 * Create a new vector instance of a given Number type with the given dimension.
	 * every component of the vector will be set to 0.
	 * @param type      the components Number implementation
	 * @param dimension the vector dimension
	 * @return a new vector instance
	 */
	public static Vector of(TYPE type, int dimension) {
		return new Vector(type.array(dimension).zero());
	}

	/**
	 * Copy current vector. Array implementation is preserved.
	 * @return a copy of the current vector
	 */
	public Vector copy() {
		return new Vector(this.value.copy());
	}

	/**
	 * The vector index with the highest value.
	 * @return {@link NumericArray#topIndex()} for {@link #value}
	 */
	public int topIndex() {
		return this.value.topIndex();
	}
	
	/**
	 * Create a one-hot vector, i.e. a vector whose components are all set to '0' but one index which is set to '1'.
	 * @param type   the numeric array implementation
	 * @param index  the index of the component that should be set to 1
	 * @param length the vector dimension
	 * @return a new one-hot vector instance
	 */
	public static Vector oneHot(TYPE type, int index, int length) {
		Vector oneHot = Vector.of(type, length);
		oneHot.value.oneHot(index);
		return oneHot;
	}

	/**
	 * Sum two vectors into a new one. Dimensions should be compatible !
	 * @param a the left operand
	 * @param b the right operand
	 * @return a new vector instance that is the sum of a and b
	 */
	public static Vector sum(Vector a, Vector b) {
		return a.copy().sum(b);
	}

	/**
	 * Subtract two vectors into a new one. Dimensions should be compatible !
	 * @param a the left operand
	 * @param b the right operand
	 * @return a new vector instance that is a - b
	 */
	public static Vector sub(Vector a, Vector b) {
		return a.copy().sub(b);
	}

	/**
	 * Add the given vector value to the current instance
	 * @param other the vector to add to the current vector
	 * @return the current vector instance instance
	 */
	public Vector sum(Vector other) {
		Vector output = this.copy();
		output.value.sum(other.value);
		return output;
	}

	/**
	 * Subtract the given vector value to the current instance
	 * @param other the vector to subtract to the current vector
	 * @return the current vector instance instance
	 */
	public Vector sub(Vector other) {
		Vector output = this.copy();
		output.value.sub(other.value);
		return output;
	}
	
	/**
	 * Multiply the given vector value to the current instance
	 * @param other the vector to multiply to the current vector with
	 * @return the current vector instance instance
	 */
	public Vector mult(Vector other) {
		Vector output = this.copy();
		output.value.mul(other.value);
		return output;
	}

	/**
	 * Divide the given vector value to the current instance
	 * @param other the vector to divide to the current vector with
	 * @return the current vector instance instance
	 */
	public Vector div(Vector other) {
		Vector output = this.copy();
		output.value.div(other.value);
		return output;
	}
	
	/**
	 * Multiply the current vector instance with a scalar
	 * @param scalar the scalar factor
	 * @return the current vector instance
	 */
	public Vector mult(float scalar) {
		Vector output = this.copy();
		output.value.mul(scalar);
		return output;
	}

	/**
	 * Sum all the current instance values.
	 * @return the sum of the current vector values
	 */
	public Number sum() {
		return this.value.sum();
	}
	
	/**
	 * Compute the current instance average value.
	 * @param abs true to use absolute values
	 * @return the average value of the current vector values
	 */
	@SuppressWarnings("unchecked")
	public Number avg(boolean abs) {
		return NumberUtils.avg(this.value.getType().targetClass(), this.value.asList(), abs);
	}

	/**
	 * Find the max value of the current instance
	 * @param abs true to use absolute values
	 * @return the max value of the current vector values
	 */
	@SuppressWarnings("unchecked")
	public Number max(boolean abs) {
		Function<Number, Number> mapper = abs ? NumberUtils::abs : Function.identity();
		return (Number) this.value.asList().stream().map(mapper).max((Comparator) Comparator.naturalOrder()).orElse(0);
	}

	/**
	 * Normalize every component of this vector (cross multiplication) into the [0, 1] segment.
	 * e.g. (75, 0, 100) → 0.75  
	 * @param min    the number min bound inclusive
	 * @param max    the number max bound inclusive
	 * @return the current vector instance normalized to the [0, 1] segment.
	 */
	@SuppressWarnings("unchecked")
	public Vector normalize(float min, float max) {
		return this.normalize(
			NumberUtils.convertNumberToTargetClass(min, this.value.getType().targetClass()), 
			NumberUtils.convertNumberToTargetClass(max, this.value.getType().targetClass())
		);
	}
	
	/**
	 * Normalize every component of this vector (cross multiplication) into the [0, 1] segment.
	 * e.g. (75, 0, 100) → 0.75  
	 * @param min    the number min bound inclusive
	 * @param max    the number max bound inclusive
	 * @return the current vector instance normalized to the [0, 1] segment.
	 */
	public Vector normalize(Number min, Number max) {
		this.value.normalize(min, max);
		return this;
	}
	
	@Override
	public String toString() {
		return "[" + this.format(this.value.asArray()) + "]";
	}
	
	public String shortLabel() {
		return "V(" + this.value.getType().name() + ", " + this.dimension() + ")";
	}
	
	protected String format(Number[] vector) {
		return Joiner.on(" ").join(Arrays.stream(vector).map(this::format).collect(Collectors.toList()));
	}
	
	protected String format(Number value) {
		return this.format.format(value);
	}
}
