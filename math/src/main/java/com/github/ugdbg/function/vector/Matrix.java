package com.github.ugdbg.function.vector;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A rectangular (m,n) matrix that can compute an input m sized vector.
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
	private String format = "%.2f";

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

	public Matrix format(String format) {
		this.format = format;
		return this;
	}

	public Matrix at(int x, int y, float value) {
		try {
			this.weights[x][y] = value;
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(
				"Illegal position for matrix [" + this.getM() + "," + this.getN() + "] @ [" + x + "," + y + "]" 
			);
		}
		return this;
	}
	
	public float at(int x, int y) {
		try {
			return this.weights[x][y];
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(
				"Illegal position for matrix [" + this.getM() + "," + this.getN() + "] @ [" + x + "," + y + "]" 
			);
		}
	}

	public Matrix at(int x, float... values) {
		try {
			this.weights[x] = values;
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(
				"Illegal position for matrix [" + this.getM() + "," + this.getN() + "] @ [" + x + "]" 
			);
		}
		return this;
	}
	
	public float[] line(int x) {
		try {
			return this.weights[x];
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(
				"Illegal position for matrix [" + this.getM() + "," + this.getN() + "] @ [" + x + "]" 
			);
		}
	}
	
	public float[] col(int y) {
		float[] out = new float[this.getM()];
		for (int i = 0; i < this.getM(); i++) {
			try {
				out[i] = this.weights[i][y];
			} catch (RuntimeException e) {
				throw new IllegalArgumentException(
					"Illegal position for matrix [" + this.getM() + "," + this.getN() + "] @ [" + i + "," + y + "]" 
				);
			}
		}
		return out;
	}
	
	@Override
	public float[] apply(float[] input) {
		int height = this.getM();
		if (height != input.length) {
			throw new IllegalArgumentException(
				"Vector length [" + input.length + "] != matrix height [" + height + "]"
			);
		}

		int width = this.getN();
		float[] out = new float[width];
		for (int i = 0; i < width; i++) {
			out[i] = linearCombination(input, this.col(i));
		}
		
		return out;
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder("┌" + spaces(this.getN() * 5 - 1) + "┐" + "\n");
		for (int i = 0; i < this.getM(); i++) {
			out.append("│");
			out.append(this.format(this.line(i)));
			out.append("│\n");
		}
		return out + "└" + spaces(this.getN() * 5 -1) + "┘";
	}

	public String toString(float[] vector) {
		return "[" + this.format(vector) + "]";
	}
	
	private String format(float[] vector) {
		return this.format(ArrayUtils.toObject(vector));
	}
	
	private String format(Float[] vector) {
		return Joiner.on(" ").join(Arrays.stream(vector).map(this::format).collect(Collectors.toList()));
	}
	
	/**
	 * Simple linear combination between an input vector and the neuron's weights. <br>
	 * Sizes must be the same !
	 * @param input the input vector
	 * @return the linear combination with the neuron's weights.
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
		return String.format(this.format, value);
	}
	
	private static String spaces(int amount) {
		return new String(new char[amount]).replace('\0', ' ');
	}
	
	public static void main(String[] args) {
		Matrix matrix = new Matrix(4, 3)
			.at(0, 0.2f, 0.3f, 0.1f)
			.at(1, 0.4f, 0.3f, 0.5f)
			.at(2, 0.1f, 0.7f, 0.1f)
			.at(3, 0.6f, 0.3f, 0.9f);

		System.out.println("m = " + matrix.getM());
		System.out.println("n = " + matrix.getN());
		System.out.println(matrix);

		System.out.println(matrix.toString(matrix.apply(new float[]{1, 0, 0, 0})));
	}
}
