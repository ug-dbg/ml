package com.github.ugdbg.datatypes.array;

import com.github.ugdbg.NumberUtils;
import com.github.ugdbg.datatypes.TYPE;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface NumericArray extends Serializable {
	TYPE getType();
	
	int length();
	
	NumericArray zero();
	
	NumericArray copy();
	
	NumericArray oneHot(int index);
	
	int topIndex();
	
	Number at(int index);
	
	void at(int i, Number value);
	
	void sum(NumericArray with);
	void sub(NumericArray with);
	void mul(NumericArray with);
	void div(NumericArray with);
	
	void mul(float with);
	void mul(double with);
	void mul(BigDecimal with);
	
	Number sum();
	
	Number linearCombination(NumericArray with);
	
	float linearCombinationToFloat(NumericArray with);
	double linearCombinationToDouble(NumericArray with);
	BigDecimal linearCombinationToDecimal(NumericArray with);
	
	float[] floats();
	double[] doubles();
	BigDecimal[] decimals();
	
	default void normalize(Number min, Number max) {
		this.operation((vector, i) -> this.at(i, NumberUtils.normalize(this.at(i), min, max)));
	}
	
	default List<Number> asList() {
		return this.indexStream().mapToObj(this::at).collect(Collectors.toList());
	}
	
	default Number[] asArray() {
		return this.indexStream().mapToObj(this::at).toArray(Number[]::new);
	}
	
	default IntStream indexStream() {
		return IntStream.range(0, this.length());
	}
	
	@SuppressWarnings("unchecked")
	default NumericArray operation(Operation operation) {
		for (int index = 0; index < this.length(); index++) {
			operation.apply(this, index);
		}
		return this;
	}
}
