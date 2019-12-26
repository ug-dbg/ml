package com.github.ugdbg.datatypes.matrix;

import com.github.ugdbg.datatypes.TYPE;
import com.github.ugdbg.datatypes.array.PrimitiveDoubleArray;

import java.math.BigDecimal;

public class PrimitiveDoubleMatrix implements NumericMatrix {
	
	private final double[][] rows;

	public PrimitiveDoubleMatrix(int m, int n) {
		this.rows = new double[m][n];
	}

	public PrimitiveDoubleMatrix(double[][] matrix) {
		this.rows = matrix;
	}
	
	@Override
	public TYPE getType() {
		return TYPE.PDOUBLE;
	}

	/**
	 * Get the matrix height, i.e. the output dimension
	 * @return the matrix height
	 */
	public int getM() {
		return this.rows.length;
	}

	/**
	 * Get the matrix width, i.e. the input dimension
	 * @return the matrix width (actually the length of the array at line #1
	 */
	public int getN() {
		return this.rows[0].length;
	}

	@Override
	public Double at(int i, int j) {
		return this.doubleAt(i, j);
	}
	
	@Override
	public float floatAt(int i, int j) {
		return (float) this.rows[i][j];
	}

	@Override
	public double doubleAt(int i, int j) {
		return this.rows[i][j];
	}

	@Override
	public BigDecimal decimalAt(int i, int j) {
		return BigDecimal.valueOf(this.rows[i][j]);
	}

	@Override
	public PrimitiveDoubleMatrix at(int i, int j, Number value) {
		this.rows[i][j] = value.doubleValue();
		return this;
	}

	@Override
	public PrimitiveDoubleMatrix at(int i, int j, float value) {
		this.rows[i][j] = value;
		return this;
	}

	@Override
	public PrimitiveDoubleMatrix at(int i, int j, double value) {
		this.rows[i][j] = value;
		return this;
	}

	@Override
	public PrimitiveDoubleMatrix at(int i, int j, BigDecimal value) {
		this.rows[i][j] = value.doubleValue();
		return this;
	}

	@Override
	public PrimitiveDoubleArray line(int x) {
		this.dimensionCheck(x, 0);
		return new PrimitiveDoubleArray(this.rows[x]);
	}
	
	@Override
	public PrimitiveDoubleArray column(int y) {
		this.dimensionCheck(0, y);
		PrimitiveDoubleArray column = new PrimitiveDoubleArray(this.getM());
		this.rowIndexStream().forEach(rowIndex -> column.doubles()[rowIndex] = this.doubleAt(rowIndex, y));
		return column;
	}
	
	@Override
	public void sum(NumericMatrix with) {
		this.dimensionCheck(with);
		this.operation(((matrix, i, j) -> matrix.at(i, j, this.doubleAt(i, j) + with.doubleAt(i, j))));
	}

	@Override
	public void mul(NumericMatrix with) {
		this.dimensionCheck(with);
		this.operation(((matrix, i, j) -> matrix.at(i, j, this.doubleAt(i, j) * with.doubleAt(i, j))));
	}
	
	@Override
	public void mul(float with) {
		this.operation(((matrix, i, j) -> matrix.at(i, j, this.doubleAt(i, j) * with)));
	}
	
	@Override
	public PrimitiveDoubleMatrix transpose() {
		PrimitiveDoubleMatrix transpose = new PrimitiveDoubleMatrix(this.getN(), this.getM());
		for (int i = 0; i < this.getM(); i++) {
			for (int j = 0; j < this.getN(); j++) {
				transpose.rows[j][i] = this.rows[i][j];
			}
		}
		return transpose;
	}
}
