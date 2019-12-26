package com.github.ugdbg.datatypes;

import com.github.ugdbg.datatypes.array.DecimalArray;
import com.github.ugdbg.datatypes.array.NumericArray;
import com.github.ugdbg.datatypes.array.PrimitiveDoubleArray;
import com.github.ugdbg.datatypes.array.PrimitiveFloatArray;
import com.github.ugdbg.datatypes.matrix.DecimalMatrix;
import com.github.ugdbg.datatypes.matrix.NumericMatrix;
import com.github.ugdbg.datatypes.matrix.PrimitiveDoubleMatrix;
import com.github.ugdbg.datatypes.matrix.PrimitiveFloatMatrix;

import java.math.BigDecimal;

/**
 * The available numeric data types.
 * <br><br>
 * The problem is : 
 * <ul>
 *     <li>We want a linear algebra API that is numeric data type independent.</li>
 *     <li>We want to keep using primitive types as long as possible since performance is much butter.</li>
 *     <li>We cannot use generics with primitive types for now. (Project Valhalla ?)</li>
 * </ul>
 * → We chose to expose an API that can deal with <b>all the types from this enum</b>.  <br>
 * → It is up to the <b>implementation</b> to deal with any conversion that is required. <br>
 * → It is up to the <b>caller</b> to keep using coherent API methods. <br>
 */
public enum TYPE {
	PFLOAT, PDOUBLE, DECIMAL;
	
	public Class targetClass() {
		switch (this) {
			case PFLOAT:  return float.class;
			case PDOUBLE: return double.class;
			case DECIMAL: return BigDecimal.class;
			default: throw new IllegalArgumentException("Unknown numeric type [" + this.name() + "]");
		}
	}
	
	public NumericArray array(int dimension) {
		switch (this) {
			case PFLOAT:  return new PrimitiveFloatArray(dimension);
			case PDOUBLE: return new PrimitiveDoubleArray(dimension);
			case DECIMAL: return new DecimalArray(dimension);
			default: throw new IllegalArgumentException("Unknown numeric array type [" + this.name() + "]");
		}
	}
	
	public NumericMatrix matrix(int m, int n) {
		switch (this) {
			case PFLOAT:  return new PrimitiveFloatMatrix(m, n);
			case PDOUBLE: return new PrimitiveDoubleMatrix(m, n);
			case DECIMAL: return new DecimalMatrix(m, n);
			default: throw new IllegalArgumentException("Unknown numeric matrix type [" + this.name() + "]");
		}
	}
}
