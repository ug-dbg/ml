package com.github.ugdbg.datatypes.matrix;

import com.github.ugdbg.datatypes.TYPE;
import com.github.ugdbg.datatypes.array.PrimitiveFloatArray;

import java.math.BigDecimal;

public class PrimitiveFloatMatrix implements NumericMatrix {
	
	private final float[][] rows;

	public PrimitiveFloatMatrix(int m, int n) {
		this.rows = new float[m][n];
	}

	public PrimitiveFloatMatrix(float[][] matrix) {
		this.rows = matrix;
	}

	@Override
	public TYPE getType() {
		return TYPE.PFLOAT;
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
	public Float at(int i, int j) {
		this.dimensionCheck(i, j);
		return this.floatAt(i, j);
	}

	@Override
	public PrimitiveFloatArray line(int x) {
		this.dimensionCheck(x, 0);
		return new PrimitiveFloatArray(this.rows[x]);
	}
	
	@Override
	public PrimitiveFloatArray column(int y) {
		this.dimensionCheck(0, y);
		PrimitiveFloatArray column = new PrimitiveFloatArray(this.getM());
		this.rowIndexStream().forEach(rowIndex -> column.floats()[rowIndex] = this.floatAt(rowIndex, y));
		return column;
	}

	@Override
	public float floatAt(int i, int j) {
		this.dimensionCheck(i, j);
		return this.rows[i][j];
	}

	@Override
	public double doubleAt(int i, int j) {
		this.dimensionCheck(i, j);
		return this.rows[i][j];
	}

	@Override
	public BigDecimal decimalAt(int i, int j) {
		this.dimensionCheck(i, j);
		return BigDecimal.valueOf(this.rows[i][j]);
	}

	@Override
	public PrimitiveFloatMatrix at(int i, int j, Number value) {
		this.dimensionCheck(i, j);
		this.rows[i][j] = value.floatValue();
		return this;
	}

	@Override
	public PrimitiveFloatMatrix at(int i, int j, float value) {
		this.dimensionCheck(i, j);
		this.rows[i][j] = value;
		return this;
	}

	@Override
	public PrimitiveFloatMatrix at(int i, int j, double value) {
		this.dimensionCheck(i, j);
		this.rows[i][j] = (float) value;
		return this;
	}

	@Override
	public PrimitiveFloatMatrix at(int i, int j, BigDecimal value) {
		this.dimensionCheck(i, j);
		this.rows[i][j] = value.floatValue();
		return this;
	}

	@Override
	public void sum(NumericMatrix with) {
		this.dimensionCheck(with);
		this.operation(((matrix, i, j) -> matrix.at(i, j, this.floatAt(i, j) + with.floatAt(i, j))));
	}

	@Override
	public void mul(NumericMatrix with) {
		this.dimensionCheck(with);
		this.operation(((matrix, i, j) -> matrix.at(i, j, this.floatAt(i, j) * with.floatAt(i, j))));
	}

	@Override
	public void mul(float with) {
		this.operation(((matrix, i, j) -> matrix.at(i, j, this.floatAt(i, j) * with)));
	}
	
	@Override
	public PrimitiveFloatMatrix transpose() {
		PrimitiveFloatMatrix transpose = new PrimitiveFloatMatrix(this.getN(), this.getM());
		for (int i = 0; i < this.getM(); i++) {
			for (int j = 0; j < this.getN(); j++) {
				transpose.rows[j][i] = this.rows[i][j];
			}
		}
		return transpose;
	}
}
