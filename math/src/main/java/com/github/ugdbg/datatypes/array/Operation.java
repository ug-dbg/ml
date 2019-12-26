package com.github.ugdbg.datatypes.array;

/**
 * An interface for an operation to execute on an array at a position.
 */
@FunctionalInterface
public interface Operation<T extends NumericArray> {
	void apply(T array, int index);
}
