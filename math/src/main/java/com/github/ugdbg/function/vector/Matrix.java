package com.github.ugdbg.function.vector;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	private transient String format = "%+.2f";

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
	
	public Matrix transpose() {
		Matrix transposed = new Matrix(this.getN(), this.getM());
		for (int i = 0; i < this.getM(); i++) {
			for (int j = 0; j < this.getN(); j++) {
				transposed.at(j, i, this.at(i, j));
			}
		}
		return transposed;
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
	
	public List<Vector> lines() {
		List<Vector> lines = new ArrayList<>(this.getM());
		for (int i = 0; i < this.getM(); i++) {
			lines.add(new Vector(this.line(i)));
		}
		return lines;
	}
	
	public List<Vector> cols() {
		List<Vector> lines = new ArrayList<>(this.getN());
		for (int j = 0; j < this.getN(); j++) {
			lines.add(new Vector(this.col(j)));
		}
		return lines;
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
		StringBuilder out = new StringBuilder("┌" + spaces(this.getN() * 6 - 1) + "┐" + "\n");
		for (int i = 0; i < this.getM(); i++) {
			out.append("│");
			out.append(this.format(this.line(i)));
			out.append("│\n");
		}
		return out + "└" + spaces(this.getN() * 6 -1) + "┘";
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
}
