package com.github.ugdbg.vector;

import com.github.ugdbg.NumberUtils;
import com.github.ugdbg.vector.format.Format;
import com.google.common.base.Joiner;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A generic vector implementation.
 * <br>
 * This implementation is not suitable for primitives and relies a lot on auto-boxing : performance is not that good.
 * @param <T> the number implementation for the vector components
 * @param <V>
 */
public class Vector<T extends Number & Comparable<T>, V extends Vector<T, V>> implements IVector<V>, Serializable {
	
	protected final T[] value;
	protected final Class<T> type;
	protected Format format;

	/**
	 * Constructor from explicit array value.
	 * @param value the vector components
	 */
	@SuppressWarnings("unchecked")
	public Vector(T[] value) {
		this.value = Arrays.copyOf(value, value.length);
		this.type = (Class<T>) value.getClass().getComponentType();
	}

	/**
	 * Constructor from explicit array values.
	 * @param values the vector components
	 */
	@SafeVarargs
	protected static <T extends Number & Comparable<T>> Vector of(T... values) {
		return new Vector<>(values);
	}

	/**
	 * Create a new vector instance of a given Number type with the given dimension.
	 * every component of the vector will be set to 0.
	 * @param type      the components Number implementation
	 * @param dimension the vector dimension
	 * @param <T> the component type
	 * @return a new vector instance
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number & Comparable<T>> Vector<T, ?> of(Class<T> type, int dimension) {
		return new Vector<>((T[]) Array.newInstance(type, dimension)).operation(Vector::zero);
	}

	/**
	 * Copy a vector.
	 * @param source the source vector
	 * @param <T> the component type
	 * @param <V> the vector implementation. It should have a public constructor with explicit value array parameter. 
	 * @return a copy of the source vector
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number & Comparable<T>, V extends Vector<T, V>> V copy(V source) {
		try {
			T[] array = source.value;
			return (V) source.getClass().getDeclaredConstructor(array.getClass()).newInstance((Object) array);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Could not instantiate [" + source.getClass() + "]", e);
		}
	}

	/**
	 * Create a one-hot vector, i.e. a vector whose components are all set to '0' but one index which is set to '1'.
	 * @param type   the components Number implementation
	 * @param index  the index of the component that should be set to 1
	 * @param length the vector dimension
	 * @param <T> the components Number type
	 * @return a new one-hot vector instance
	 */
	public static <T extends Number & Comparable<T>> Vector<T, ?> oneHot(Class<T> type, int index, int length) {
		Vector<T, ?> oneHot = Vector.of(type, length);
		oneHot.value[index] = NumberUtils.one(type);
		return oneHot;
	}

	/**
	 * Sum two vectors into a new one. Dimensions should be compatible !
	 * @param a the left operand
	 * @param b the right operand
	 * @param <T> the components Number type
	 * @param <V> the vector implementation. It should have a public constructor with explicit value array parameter. 
	 * @return a new vector instance that is the sum of a and b
	 */
	public static <T extends Number & Comparable<T>, V extends Vector<T, V>> V sum(V a, V b) {
		return copy(a).sum(b);
	}

	/**
	 * Subtract two vectors into a new one. Dimensions should be compatible !
	 * @param a the left operand
	 * @param b the right operand
	 * @param <T> the components Number type
	 * @param <V> the vector implementation. It should have a public constructor with explicit value array parameter. 
	 * @return a new vector instance that is a - b
	 */
	public static <T extends Number & Comparable<T>, V extends Vector<T, V>> V sub(V a, V b) {
		return copy(a).sub(b);
	}

	@Override
	@SuppressWarnings("unchecked")
	public V format(Format format) {
		this.format = format;
		return (V) this;
	}

	@Override
	public T[] getValue() {
		return this.value;
	}

	@Override
	public int dimension() {
		return this.value.length;
	}
	
	@Override
	public T at(int i) {
		return this.value[i];
	}
	
	@Override
	public void at(int i, Number value) {
		this.value[i] = NumberUtils.convertNumberToTargetClass(value, this.type);
	}

	public void zero(int i) {
		this.value[i] = NumberUtils.zero(this.type);
	}
	
	@Override
	public V sum(V other) {
		return this.operation((vector, i) -> vector.at(i, vector.add(vector.at(i), other.at(i))));
	}
	
	@Override
	public V sub(V other) {
		return this.operation((vector, i) -> vector.at(i, vector.sub(vector.at(i), other.at(i))));
	}
	
	public V mult(float scalar) {
		return this.operation((vector, i) -> vector.at(i, vector.mult(vector.at(i), scalar)));
	}
	
	@Override
	public V mult(V other) {
		return this.operation((vector, i) -> this.at(i, vector.mult(vector.at(i), other.at(i))));
	}
	
	@Override
	public V div(V other) {
		return this.operation((vector, i) -> this.at(i, vector.div(vector.at(i), other.at(i))));
	}
	
	@Override
	public T sum() {
		return NumberUtils.sum(this.type, Arrays.asList(this.value));
	}
	
	@Override
	public T avg(boolean abs) {
		return NumberUtils.avg(this.type, Arrays.asList(this.value), abs);
	}
	
	@Override
	public T max(boolean abs) {
		Function<T, T> mapper = abs ? NumberUtils::abs : Function.identity();
		return Arrays.stream(this.value).map(mapper).max(Comparator.naturalOrder()).orElse(NumberUtils.zero(this.type));
	}

	@Override
	public V normalize(float min, float max) {
		return this.normalize(
			NumberUtils.convertNumberToTargetClass(min, this.type), 
			NumberUtils.convertNumberToTargetClass(max, this.type)
		);
	}
	
	/**
	 * Normalize every component of this vector (cross multiplication) into the [0, 1] segment.
	 * e.g. (75, 0, 100) → 0.75  
	 * @param min    the number min bound inclusive
	 * @param max    the number max bound inclusive
	 * @return the current vector instance normalized to the [0, 1] segment.
	 */
	public V normalize(T min, T max) {
		return this.operation((vector, i) -> this.at(i, NumberUtils.normalize(this.at(i), min, max)));
	}
	
	@Override
	public String toString() {
		return "[" + this.format(this.value) + "]";
	}
	
	protected String format(Number[] vector) {
		return Joiner.on(" ").join(Arrays.stream(vector).map(this::format).collect(Collectors.toList()));
	}
	
	protected String format(Number value) {
		return this.format.format(value);
	}
	
	protected T zero() {
		return NumberUtils.zero(this.type);
	}
	
	protected T one() {
		return NumberUtils.one(this.type);
	}
	
	protected T add(T a, T b) {
		return NumberUtils.add(a, b);
	}
	
	protected T sub(T a, T b) {
		return NumberUtils.sub(a, b);
	}
	
	protected T mult(T a, T b) {
		return NumberUtils.mult(a, b);
	}
	
	protected T mult(T a, float scalar) {
		return NumberUtils.mult(a, scalar);
	}
	
	protected T div(T a, T b) {
		return NumberUtils.div(a, b);
	}
}
