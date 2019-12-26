package com.github.ugdbg.datatypes.matrix;

import com.github.ugdbg.datatypes.TYPE;
import com.github.ugdbg.datatypes.array.NumericArray;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.stream.IntStream;

/**
 * An interface for a {@link #getM()} x {@link #getN()} matrix data store.
 * <br><br>
 * <b>
 *     The data type stored in the matrix is up to the implementation.
 *     However, the numeric matrix should implement operations for all the available numeric types.
 *     <br>
 *     See {@link #getType()} and {@link TYPE}.
 *     <br>
 *     Knowing the matrix numeric data type, the caller holds responsibility for using the most appropriate functions.  
 * </b> 
 */
public interface NumericMatrix extends Serializable {

	/**
	 * What is the numeric type of data this matrix stores ?
	 * @return the matrix data type
	 */
	TYPE getType();
	
	/**
	 * Get the matrix height, i.e. the output dimension
	 * @return the matrix height
	 */
	int getM();

	/**
	 * Get the matrix width, i.e. the input dimension
	 * @return the matrix width (actually the length of the array at line #1
	 */
	int getN();

	/**
	 * Read the matrix value at the given coordinates. 
	 * @param i the line index
	 * @param j the column index
	 * @return a numeric representation of the value @(i, j)
	 */
	Number at(int i, int j);

	/**
	 * Read the matrix float value at the given coordinates. 
	 * @param i the line index
	 * @param j the column index
	 * @return a numeric representation of the value @(i, j)
	 */
	float floatAt(int i, int j);
	
	/**
	 * Read the matrix double value at the given coordinates. 
	 * @param i the line index
	 * @param j the column index
	 * @return a numeric representation of the value @(i, j)
	 */
	double doubleAt(int i, int j);
	
	/**
	 * Read the matrix BigDecimal value at the given coordinates. 
	 * @param i the line index
	 * @param j the column index
	 * @return a numeric representation of the value @(i, j)
	 */
	BigDecimal decimalAt(int i, int j);

	/**
	 * Set the matrix value at (i, j) coordinates
	 * @param i the line index
	 * @param j the column index
	 * @param value the numeric value to set. The implementation shall do the appropriate checks and conversion.
	 * @return the current matrix instance
	 */
	NumericMatrix at(int i, int j, Number value);

	/**
	 * Set the matrix value at (i, j) coordinates
	 * @param i the line index
	 * @param j the column index
	 * @param value the float value to set
	 * @return the current matrix instance
	 */
	NumericMatrix at(int i, int j, float value);
	
	/**
	 * Set the matrix value at (i, j) coordinates
	 * @param i the line index
	 * @param j the column index
	 * @param value the double value to set
	 * @return the current matrix instance
	 */
	NumericMatrix at(int i, int j, double value);
	
	/**
	 * Set the matrix value at (i, j) coordinates
	 * @param i the line index
	 * @param j the column index
	 * @param value the BigDecimal value to set
	 * @return the current matrix instance
	 */
	NumericMatrix at(int i, int j, BigDecimal value);

	/**
	 * Return an array representation of the matrix line @x
	 * @param x the matrix line index
	 * @return an array representation of the line @x
	 */
	NumericArray line(int x);
	
	/**
	 * Return an array representation of the matrix column @x
	 * @param y the matrix column index
	 * @return an array representation of the column @x
	 */
	NumericArray column(int y);

	/**
	 * Sum the current matrix instance with an other matrix 
	 * @param with an other matrix instance
	 */
	void sum(NumericMatrix with);

	/**
	 * Multiply the current matrix instance with an other matrix 
	 * @param with an other matrix instance
	 */
	void mul(NumericMatrix with);

	/**
	 * Multiply the current matrix instance with a scalar 
	 * @param with the scalar value as a float
	 */
	void mul(float with);

	/**
	 * Create the transpose of the current matrix
	 * @return a new matrix instance, transpose of the current instance
	 */
	NumericMatrix transpose();

	/**
	 * Create an integer stream that is set to match the current matrix heigth. 
	 * @return an integer stream from 0 (inclusive) to {@link #getM()} (exclusive)
	 */
	default IntStream rowIndexStream() {
		return IntStream.range(0, this.getM());
	}

	/**
	 * Create an integer stream that is set to match the current matrix width. 
	 * @return an integer stream from 0 (inclusive) to {@link #getN()} (exclusive)
	 */
	default IntStream colIndexStream() {
		return IntStream.range(0, this.getN());
	}
	
	/**
	 * Get a short label for this matrix.
	 * Example : M(4, 5)
	 * @return a short label for this Matrix
	 */
	default String shortLabel() {
		return "M(" + this.getM() + ", " + this.getN() + ")";
	}

	/**
	 * Check if the given coordinates belongs to the current matrix instance.
	 * @param m the height index
	 * @param n the width index
	 */
	default void dimensionCheck(int m, int n) {
		if (this.getM() < m || this.getN() < n) {
			throw new IllegalArgumentException(
				"Matrix dimension check failed : " + this.shortLabel() + " with (" + m + ", " + n + ")" 
			);
		}
	}
	
	/**
	 * Check if the given matrix has the same dimension as the current matrix instance.
	 * @param other the other matrix
	 */
	default void dimensionCheck(NumericMatrix other) {
		if (this.getM() != other.getM() || this.getN() != other.getN()) {
			throw new IllegalArgumentException(
				"Matrix dimension check failed : " + this.shortLabel() + " with " + other.shortLabel()
			);
		}
	}
	
	/**
	 * Execute an operation on every weight
	 * @param lambda the operation to execute
	 * @return the current Matrix instance
	 */
	default NumericMatrix operation(Operation lambda) {
		for (int i = 0; i < this.getM(); i++) {
			for (int j = 0; j < this.getN(); j++) {
				lambda.apply(this, i, j);
			}
		}
		return this;
	}
}
