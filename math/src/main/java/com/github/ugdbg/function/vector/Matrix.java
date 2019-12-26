package com.github.ugdbg.function.vector;

import com.github.ugdbg.NumberUtils;
import com.github.ugdbg.datatypes.TYPE;
import com.github.ugdbg.datatypes.array.NumericArray;
import com.github.ugdbg.datatypes.matrix.NumericMatrix;
import com.github.ugdbg.function.vector.domain.VDomain;
import com.github.ugdbg.function.vector.domain.VDomains;
import com.github.ugdbg.vector.Vector;
import com.github.ugdbg.vector.format.FloatFormat;
import com.github.ugdbg.vector.format.Format;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.AtomicDouble;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * m:x(x₁,x₂,x₃...xₘ) → y(y₁,y₂,y₃...yₙ)
 * <br>
 * where :
 * <ul>
 *     <li>y(y₁,y₂,y₃...yₙ) = M(m,n) * x(x₁,x₂,x₃...xₘ)</li>
 *     <li>M(m,n) is a rectangular (m,n) matrix.</li>
 *     <li>default {@link #domain} is {@link VDomains#R(int)} for dimension m</li>
 * </ul>
 * <br>
 * Example : 
 * <pre>
 * ┌              ┐
 * │0,20 0,30 0,10│
 * │0,40 0,30 0,50│
 * │0,10 0,70 0,10│
 * │0,60 0,30 0,90│
 * └              ┘
 * </pre>
 * <b>
 *     The numeric type of data stored in the matrix is delegated to {@link #weights}.
 *     See {@link NumericMatrix} and its implementations.
 * </b>
 */
public class Matrix extends DomainCheckedFunction<Matrix> implements VFunction {
	
	private final NumericMatrix weights;
	private transient Format format = new FloatFormat(3, 2);

	/**
	 * New Matrix M(m,n)
	 * @param m the matrix height (output dimension)
	 * @param n the matrix width (input dimension)
	 */
	public Matrix(int m, int n, TYPE type) {
		this.weights = type.matrix(m, n);
		this.domain = VDomains.R(this.getN());
	}

	private Matrix(NumericMatrix matrix) {
		this.weights = matrix;
	}

	@Override
	public Matrix onDomain(VDomain domain) {
		if (domain.dimension() != this.getN()) {
			throw new IllegalArgumentException("Domain [" + domain + "] is not applicable for [" + this + "]");
		}
		return super.onDomain(domain);
	}

	@Override
	public Vector doApply(Vector input) {
		int height = this.getM();

		NumericArray value = input.getValue();
		TYPE type = value.getType();
		Vector out = Vector.of(type, height);
		for (int i = 0; i < height; i++) {
			switch (type) {
				case PFLOAT:  out.floats()[i]   = value.linearCombinationToFloat(this.line(i).getValue());   break; 
				case PDOUBLE: out.doubles()[i]  = value.linearCombinationToDouble(this.line(i).getValue());  break; 
				case DECIMAL: out.decimals()[i] = value.linearCombinationToDecimal(this.line(i).getValue()); break;
				default:      throw new IllegalArgumentException("Unsupported input type [" + type.name() + "]");
			}
		}
		
		return out;
	}
	
	@Override
	public String label() {
		return this.shortLabel();
	}

	@Override
	public String toString() {
		return this.shortLabel();
	}
	
	/**
	 * Get the matrix height, i.e. the output dimension
	 * @return the matrix height
	 */
	public int getM() {
		return this.weights.getM();
	}

	/**
	 * Get the matrix width, i.e. the input dimension
	 * @return the matrix width (actually the length of the array at line #1
	 */
	public int getN() {
		return this.weights.getN();
	}

	/**
	 * Create a new M(m,n) matrix whose values are set from random Gaussian ("normally") distributed values.
	 * @param m      the matrix height
	 * @param n      the matrix width
	 * @param random the random number generator
	 * @return a new matrix instance
	 */
	public static Matrix randomGaussian(int m, int n, TYPE type, Random random) {
		@SuppressWarnings("unchecked")
		Class<? extends Number> target = type.targetClass();
		
		Matrix gaussian = new Matrix(m, n, type);
		gaussian.weights.operation(
			(matrix, i, j) -> matrix.at(i, j, NumberUtils.convertNumberToTargetClass(random.nextGaussian(), target)));
		return gaussian;
	}

	/**
	 * Return the outer product of 2 vectors : a ⊗ b.
	 * <br>
	 * If the two vectors have dimensions n and m, then their outer product is an n × m matrix.
	 * @param a vector a
	 * @param b vector b
	 * @return a new Matrix instance.
	 */
	public static Matrix outer(Vector a, Vector b) {
		TYPE type = a.getValue().getType();
		Matrix matrix = new Matrix(a.dimension(), b.dimension(), type);
		switch (type) {
			case PFLOAT  : return matrix.operation((outer, i, j) -> outer.at(i, j, a.floats()[i] * b.floats()[j]));
			case PDOUBLE : return matrix.operation((outer, i, j) -> outer.at(i, j, a.doubles()[i] * b.doubles()[j]));
			case DECIMAL : return matrix.operation((outer, i, j) -> outer.at(i, j, a.decimals()[i].multiply(b.decimals()[j])));
			default:      throw new IllegalArgumentException("Unsupported input type [" + type.name() + "]");
		}
		
	}

	/**
	 * Kronecker delta.
	 * <ul>
	 *     <li>i = j  → δij = 1</li>
	 *     <li>i != j → δij = 0</li>
	 * </ul>
	 */
	static int kroneckerDelta(int i, int j) {
		return (i == j) ? 1 : 0;
	}

	/**
	 * Set the format for this matrix
	 * @param format the format to ise
	 * @return the current Matrix instance
	 */
	public Matrix format(FloatFormat format) {
		this.format = format;
		return this;
	}

	/**
	 * Flip a matrix over its diagonal, i.e. switch the row and column indices of the matrix.
	 * @return a new Matrix instance, B | B = t(A)
	 */
	public Matrix transpose() {
		return new Matrix(this.weights.transpose());
	}

	/**
	 * Set the Matrix value @(i=x,j=y)
	 * @param x     the height coordinate
	 * @param y     the width coordinate
	 * @param value the value to set
	 * @return the current Matrix instance
	 * @throws IllegalArgumentException if the coordinates are outside the Matrix bounds
	 */
	public Matrix at(int x, int y, float value) {
		this.weights.at(x, y, value);
		return this;
	}
	
	/**
	 * Set the Matrix value @(i=x,j=y)
	 * @param x     the height coordinate
	 * @param y     the width coordinate
	 * @param value the value to set
	 * @return the current Matrix instance
	 * @throws IllegalArgumentException if the coordinates are outside the Matrix bounds
	 */
	public Matrix at(int x, int y, double value) {
		this.weights.at(x, y, value);
		return this;
	}
	
	/**
	 * Set the Matrix value @(i=x,j=y)
	 * @param x     the height coordinate
	 * @param y     the width coordinate
	 * @param value the value to set
	 * @return the current Matrix instance
	 * @throws IllegalArgumentException if the coordinates are outside the Matrix bounds
	 */
	public Matrix at(int x, int y, BigDecimal value) {
		this.weights.at(x, y, value);
		return this;
	}
	
	/**
	 * Get the Matrix value @(i=x,j=y)
	 * @param x     the height coordinate
	 * @param y     the width coordinate
	 * @return the Matrix value @(i=x, j=y)
	 * @throws IllegalArgumentException if the coordinates are outside the Matrix bounds
	 */
	public float at(int x, int y) {
		return this.weights.floatAt(x, y);
	}
	
	/**
	 * Get a line of the matrix @(i=x) as a FloatVector 
	 * @param x the height coordinate
	 * @return a new FloatVector of dimension {@link #getN()}
	 */
	public Vector line(int x) {
		return Vector.of(this.weights.line(x));
	}
	
	/**
	 * Get a column of the matrix @(j=y) as a FloatVector 
	 * @param y the width coordinate
	 * @return a new FloatVector of dimension {@link #getM()}
	 */
	public Vector col(int y) {
		return Vector.of(this.weights.column(y));
	}

	/**
	 * Get a representation of this matrix as a list of line vectors.
	 * @return a {@link #getM()} sized list of Vectors of dimension {@link #getN()}
	 */
	public List<Vector> lines() {
		List<Vector> lines = new ArrayList<>(this.getM());
		for (int i = 0; i < this.getM(); i++) {
			lines.add(this.line(i));
		}
		return lines;
	}
	
	/**
	 * Get a representation of this matrix as a list of column vectors.
	 * @return a {@link #getN()} sized list of Vectors of dimension {@link #getM()}
	 */
	public List<Vector> cols() {
		List<Vector> cols = new ArrayList<>(this.getN());
		for (int j = 0; j < this.getN(); j++) {
			cols.add(this.col(j));
		}
		return cols;
	}

	/**
	 * Sum this matrix with an other one.
	 * @param other the matrix to add to the current one. Dimension must match.
	 * @throws IllegalArgumentException if other matrix dimensions does not match the current ones.
	 */
	public void sum(Matrix other) {
		this.weights.sum(other.weights);
	}

	/**
	 * Multiply this matrix with a scalar.
	 * Every weight will be multiplied.
	 * @param with the scalar
	 * @return the current Matrix instance
	 */
	public Matrix mult(float with) {
		this.weights.mul(with);
		return this;
	}

	/**
	 * Get the weight of this matrix with the highest value.
	 * @return the max weight of this matrix
	 */
	public float max() {
		AtomicDouble max = new AtomicDouble(Double.NEGATIVE_INFINITY);
		this.operation((matrix, i, j) -> max.set(Math.max(matrix.at(i, j), max.get())));
		return max.floatValue();
	}

	/**
	 * Execute an operation on every weight
	 * @param lambda the operation to execute
	 * @return the current Matrix instance
	 */
	public Matrix operation(Operation lambda) {
		for (int i = 0; i < this.getM(); i++) {
			for (int j = 0; j < this.getN(); j++) {
				lambda.apply(this, i, j);
			}
		}
		return this;
	}

	/**
	 * Get a multiline full label for this matrix.
	 * Example : 
	 * <pre>
	 * ┌              ┐
	 * │0,20 0,30 0,10│
	 * │0,40 0,30 0,50│
	 * │0,10 0,70 0,10│
	 * │0,60 0,30 0,90│
	 * └              ┘
	 * </pre>
	 * @return a nice multiline label
	 */
	public List<String> fullLabel() {
		List<String> label = new ArrayList<>();
		int colLength = this.format.getLength() + 1;
		label.add("┌" + this.format.spaces(this.getN() * colLength - 1) + "┐");
		this.lines().forEach(line -> label.add("│" + this.format(line.getValue().floats()) + "│"));
		label.add("└" + this.format.spaces(this.getN() * colLength - 1) + "┘");
		return label;
	}

	/**
	 * Get a short label for this matrix.
	 * Example : M(4, 5)
	 * @return a short label for this Matrix
	 */
	public String shortLabel() {
		return "M(" + this.getM() + ", " + this.getN() + ")";
	}
	
	private String format(float[] vector) {
		return this.format(ArrayUtils.toObject(vector));
	}
	
	private String format(Float[] vector) {
		return Joiner.on(" ").join(Arrays.stream(vector).map(this::format).collect(Collectors.toList()));
	}
	
	private String format(float value) {
		return this.format.format(value);
	}
	
	/**
	 * An interface for an operation to execute on a matrix at a position.
	 */
	@FunctionalInterface
	interface Operation {
		void apply(Matrix matrix, int i, int j);
	}
}
