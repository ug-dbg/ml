package com.github.ugdbg.datatypes.matrix;

/**
 * An interface for an operation to execute on a matrix at a position.
 */
@FunctionalInterface
public interface Operation {
	void apply(NumericMatrix matrix, int i, int j);
}
