package com.github.ugdbg.vector;

import com.github.ugdbg.vector.format.Format;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Vector interface.
 * Whatever the Number implementation, this is what a vector should at least do.
 * @param <V> the vector implementation
 */
public interface IVector<V extends IVector> {

	/**
	 * Set the formatter to use.
	 * @param format the formatter to use
	 * @return the current vector
	 */
	V format(Format format);

	/**
	 * Get the vector dimension, i.e. the number of components.
	 * @return the vector dimension
	 */
	int dimension();

	/**
	 * Get the vector components as an array of Numbers. It might be a copy or the underlying array.
	 * @return an array of numbers
	 */
	Number[] getValue();

	/**
	 * What is the vector component at i ?
	 * @param i the index
	 * @return the i-th (0-based) component of the vector 
	 */
	Number at(int i);

	/**
	 * Set the vector component at i, i.e. the i-th (0-based) component of the vector.
	 * @param i     the index
	 * @param value the value
	 */
	void at(int i, Number value);

	/**
	 * Normalize (cross multiplication) this vector (in-place) to the [0, 1] segment. 
	 * @param min the vector current min bound
	 * @param max the vector current max bound
	 * @return the current vector
	 */
	V normalize(float min, float max);

	/**
	 * Sum all the components of the vector.
	 * @return the sum of all components of this vector
	 */
	Number sum();

	/**
	 * Compute the average value of the vector components. 
	 * @param abs true to use the absolute value of each component
	 * @return the average value of the components
	 */
	Number avg(boolean abs);
	
	/**
	 * Find the max value of the vector components. 
	 * Return 0 if the vector is empty.
	 * @param abs true to use the absolute value of each component
	 * @return the max value of the components
	 */
	Number max(boolean abs);

	/**
	 * Sum the current vector (in-place) with another one.
	 * <br>
	 * Dimensions should be equal !
	 * @param other the other vector
	 * @return the current vector
	 */
	V sum(V other);
	
	/**
	 * Subtract a vector from the current vector (in-place)
	 * <br>
	 * Dimensions should be equal !
	 * @param other the other vector
	 * @return the current vector
	 */
	V sub(V other);

	/**
	 * Multiply the current vector (in-place) with another one.
	 * <br>
	 * Dimensions should be equal !
	 * @param other the other vector
	 * @return the current vector
	 */
	V mult(V other);
	
	/**
	 * divide the current vector (in-place) with another one.
	 * <br>
	 * Dimensions should be equal !
	 * @param other the other vector
	 * @return the current vector
	 */
	V div(V other);

	/**
	 * Create a short label for this vector
	 * @return e.g. V(12)
	 */
	default String shortLabel() {
		return "V(" + this.dimension() + ")";
	}

	/**
	 * Scalar product of the current vector with another one.
	 * @param other the other vector
	 * @return the scalar product
	 */
	default Number scalar(V other) {
		return this.mult(other).sum();
	}

	/**
	 * Find the index of the component with the highest value
	 * @return the component with the highest value. -1 if the vector is empty
	 */
	default int topIndex() {
		AtomicInteger top = new AtomicInteger(-1);
		AtomicDouble value = new AtomicDouble(Float.NEGATIVE_INFINITY);
		this.operation((vector, i) -> {
			if (vector.at(i).floatValue() > value.floatValue()) {
				top.set(i);
				value.set(vector.at(i).floatValue());
			}
		});
		return top.get();
	}

	/**
	 * Apply the given operation to the current vector, i.e. at every index.
	 * @param op the operation to apply
	 * @return the current vector
	 */
	@SuppressWarnings("unchecked")
	default V operation(Operation<V> op) {
		for(int i = 0; i < this.dimension(); i++) {
			op.doAt((V) this, i);
		}
		return (V) this;
	}

	/**
	 * An operation on a vector at a given index
	 * @param <V> the vector implementation type
	 */
	interface Operation<V> {
		void doAt(V vector, int i);
	}
}
