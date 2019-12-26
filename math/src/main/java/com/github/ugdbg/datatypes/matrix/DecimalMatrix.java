package com.github.ugdbg.datatypes.matrix;

import com.github.ugdbg.NumberUtils;
import com.github.ugdbg.datatypes.TYPE;
import com.github.ugdbg.datatypes.array.DecimalArray;

import java.math.BigDecimal;

/**
 * A numeric matrix implementation using {@link BigDecimal}, i.e. {@link TYPE#DECIMAL} type.
 */
public class DecimalMatrix implements NumericMatrix {
	
	private final BigDecimal[][] rows;

	public DecimalMatrix(int m, int n) {
		this.rows = new BigDecimal[m][n];
		this.operation((matrix, i, j) -> matrix.at(i, j, BigDecimal.ZERO));
	}
	
	public DecimalMatrix(BigDecimal[][] matrix) {
		this.rows = matrix;
	}

	@Override
	public TYPE getType() {
		return TYPE.DECIMAL;
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
	public BigDecimal at(int i, int j) {
		this.dimensionCheck(i, j);
		return this.decimalAt(i, j);
	}

	@Override
	public float floatAt(int i, int j) {
		this.dimensionCheck(i, j);
		return this.rows[i][j].floatValue();
	}

	@Override
	public double doubleAt(int i, int j) {
		this.dimensionCheck(i, j);
		return this.rows[i][j].doubleValue();
	}

	@Override
	public BigDecimal decimalAt(int i, int j) {
		this.dimensionCheck(i, j);
		return this.rows[i][j];
	}

	@Override
	public DecimalMatrix at(int i, int j, Number value) {
		this.dimensionCheck(i, j);
		this.rows[i][j] = NumberUtils.convertNumberToTargetClass(value, BigDecimal.class);
		return this;
	}

	@Override
	public DecimalMatrix at(int i, int j, float value) {
		this.dimensionCheck(i, j);
		this.rows[i][j] = new BigDecimal(value);
		return this;
	}

	@Override
	public DecimalMatrix at(int i, int j, double value) {
		this.dimensionCheck(i, j);
		this.rows[i][j] = new BigDecimal(value);
		return this;
	}

	@Override
	public DecimalMatrix at(int i, int j, BigDecimal value) {
		this.dimensionCheck(i, j);
		this.rows[i][j] = value;
		return this;
	}

	@Override
	public DecimalArray line(int x) {
		this.dimensionCheck(x, 0);
		return new DecimalArray(this.rows[x]);
	}
	
	@Override
	public DecimalArray column(int y) {
		this.dimensionCheck(0, y);
		DecimalArray column = new DecimalArray(this.getM());
		this.rowIndexStream().forEach(rowIndex -> column.decimals()[rowIndex] = this.decimalAt(rowIndex, y));
		return column;
	}
	
	@Override
	public void sum(NumericMatrix with) {
		this.dimensionCheck(with);
		this.operation(((matrix, i, j) -> matrix.at(i, j, this.decimalAt(i, j).add(with.decimalAt(i, j)))));
	}

	@Override
	public void mul(NumericMatrix with) {
		this.dimensionCheck(with);
		this.operation(((matrix, i, j) -> matrix.at(i, j, this.decimalAt(i, j).multiply(with.decimalAt(i, j)))));
	}
	
	@Override
	public void mul(float with) {
		this.operation(((matrix, i, j) -> matrix.at(i, j, this.decimalAt(i, j).multiply(BigDecimal.valueOf(with)))));
	}
	
	@Override
	public DecimalMatrix transpose() {
		DecimalMatrix transpose = new DecimalMatrix(this.getN(), this.getM());
		for (int i = 0; i < this.getM(); i++) {
			for (int j = 0; j < this.getN(); j++) {
				transpose.rows[j][i] = this.rows[i][j];
			}
		}
		return transpose;
	}
}
