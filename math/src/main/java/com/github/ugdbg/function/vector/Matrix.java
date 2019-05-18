package com.github.ugdbg.function.vector;

import com.github.ugdbg.function.FloatFormat;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.AtomicDouble;
import org.apache.commons.lang3.ArrayUtils;

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
 */
public class Matrix implements VFunction {
	
	private final float[][] weights;
	private transient FloatFormat format = new FloatFormat(3, 2);

	public Matrix(int m, int n) {
		this.weights = new float[m][n];
	}

	public Matrix(float[][] weights) {
		this.weights = weights;
	}

	public int getM() {
		return this.weights.length;
	}
	
	public int getN() {
		return this.weights[0].length;
	}

	/**
	 * Create a new M(m,n) matrix whose values are set from random Gaussian ("normally") distributed values.
	 * @param m      the matrix height
	 * @param n      the matrix width
	 * @param random the random number generator
	 * @return a new matrix instance
	 */
	public static Matrix randomGaussian(int m, int n, Random random) {
		return new Matrix(m, n).operation((gaussian, i, j) ->  gaussian.at(i, j, (float) random.nextGaussian()));
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
		return new Matrix(a.dimension(), b.dimension()).operation((outer, i, j) -> outer.at(i, j, a.at(i) * b.at(j)));
	}

	/**
	 * Kronecker delta.
	 * <ul>
	 *     <li>i = j  → δij = 1</li>
	 *     <li>i != j → δij = 0</li>
	 * </ul>
	 */
	public static int kroneckerDelta(int i, int j) {
		return (i == j) ? 1 : 0;
	}
	
	
	public Matrix format(FloatFormat format) {
		this.format = format;
		return this;
	}

	/**
	 * Flip a matrix over its diagonal, i.e. switch the row and column indices of the matrix.
	 * @return a new Matrix instance, B | B = t(A)
	 */
	public Matrix transpose() {
		return new Matrix(this.getN(), this.getM()).operation(((matrix, i, j) -> matrix.at(i, j, this.at(j, i))));
	}
	
	public Matrix at(int x, int y, float value) {
		try {
			this.weights[x][y] = value;
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(
				"Illegal position for matrix " + this.shortLabel() + " @ " + this.position(x, y) 
			);
		}
		return this;
	}
	
	public float at(int x, int y) {
		try {
			return this.weights[x][y];
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(
				"Illegal position for matrix " + this.shortLabel() + " @ " + this.position(x, y) 
			);
		}
	}

	public Matrix atX(int x, float... values) {
		try {
			this.weights[x] = values;
		} catch (RuntimeException e) {
			throw new IllegalArgumentException("Illegal position for matrix " + this.shortLabel() +  " @ [" + x + ":Y]");
		}
		return this;
	}
	
	public Matrix atY(int y, float... values) {
		try {
			int index = 0;
			for (float[] lines : this.weights) {
				lines[y] = values[index];
				index++;
			}
		} catch (RuntimeException e) {
			throw new IllegalArgumentException("Illegal position for matrix " + this.shortLabel() + " @ [X:" + y + "]");
		}
		return this;
	}
	
	public Vector line(int x) {
		try {
			return new Vector(this.weights[x]);
		} catch (RuntimeException e) {
			throw new IllegalArgumentException("Illegal position for matrix " + this.shortLabel() + " @ [" + x + "]");
		}
	}
	
	public Vector col(int y) {
		Vector out = Vector.of(this.getM());
		for (int i = 0; i < this.getM(); i++) {
			out.at(i, this.at(i, y));
		}
		return out;
	}
	
	public List<Vector> lines() {
		List<Vector> lines = new ArrayList<>(this.getM());
		for (int i = 0; i < this.getM(); i++) {
			lines.add(this.line(i));
		}
		return lines;
	}
	
	public List<Vector> cols() {
		List<Vector> lines = new ArrayList<>(this.getN());
		for (int j = 0; j < this.getN(); j++) {
			lines.add(this.col(j));
		}
		return lines;
	}
	
	public void sum(Matrix other) {
		if (this.getM() != other.getM() || this.getN() != other.getN()) {
			throw new IllegalArgumentException(
				"Could not sum matrix " + this.shortLabel() + " with " + other.shortLabel()
			);
		}
		this.operation(((matrix, i, j) -> matrix.at(i, j, this.at(i, j) + other.at(i, j))));
	}
	
	public Matrix mult(float with) {
		return this.operation(((matrix, i, j) -> matrix.at(i, j, this.at(i, j) * with)));
	}
	
	public float max() {
		AtomicDouble max = new AtomicDouble(Double.NEGATIVE_INFINITY);
		this.operation((matrix, i, j) -> max.set(Math.max(matrix.at(i, j), max.get())));
		return max.floatValue();
	}
	
	@Override
	public float[] apply(float[] input) {
		int width = this.getN();
		int height = this.getM();
		if (width != input.length) {
			throw new IllegalArgumentException(
				"Vector length [" + input.length + "] != matrix width [" + width + "]"
			);
		}
		
		float[] out = new float[height];
		for (int i = 0; i < height; i++) {
			out[i] = linearCombination(input, this.line(i).getValue());
		}
		
		return out;
	}

	public Matrix operation(Operation lambda) {
		for (int i = 0; i < this.getM(); i++) {
			for (int j = 0; j < this.getN(); j++) {
				lambda.apply(this, i, j);
			}
		}
		return this;
	}
	
	private String position(int x, int y) {
			return "[" + x + ", " + y + "]";
		}
	
	@Override
	public String toString() {
		return Joiner.on("\n").join(this.label());
	}
	
	public List<String> label() {
		List<String> label = new ArrayList<>();
		int colLength = this.format.getLength() + 1;
		label.add("┌" + this.format.spaces(this.getN() * colLength - 1) + "┐");
		this.lines().forEach(line -> label.add("│" + this.format(line.getValue()) + "│"));
		label.add("└" + this.format.spaces(this.getN() * colLength - 1) + "┘");
		return label;
	}

	public String shortLabel() {
		return "M(" + this.getM() + ", " + this.getN() + ")";
	}
	
	private String format(float[] vector) {
		return this.format(ArrayUtils.toObject(vector));
	}
	
	private String format(Float[] vector) {
		return Joiner.on(" ").join(Arrays.stream(vector).map(this::format).collect(Collectors.toList()));
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

	private String format(int x, int y) {
		return this.format(this.at(x, y));
	}
	
	private String format(float value) {
		return this.format.format(value);
	}
	
	interface Operation {
		void apply(Matrix matrix, int i, int j);
	}
}
