package com.github.ugdbg.vector.format;

import java.util.stream.Collectors;

/**
 * An interface to manager number formatting with a fixed length.
 */
public interface Format {

	/**
	 * The output length of the formatter
	 * @return the number of characters any formatted value will have
	 */
	int getLength();

	/**
	 * Format any value (well, it should be a number I guess) 
	 * into a String whose length will be strictly equal to {@link #getLength()}
	 * @param value the value to format
	 * @return the formatted value
	 */
	String format(Object value);
	
	/**
	 * Create a String of X spaces
	 * @param amount the amount of spaces
	 * @return a String of X spaces
	 */
	default String spaces(int amount) {
		return new String(new char[Math.max(amount, 0)]).replace('\0', ' ');
	}
	
	/**
	 * Convert a number into a superscript digit representation.
	 * Example : -1234567 → ⁻¹²³⁴⁵⁶⁷
	 * @param number the number to format
	 * @return the superscript representation
	 */
	static String superscript(int number) {
		if (number < 0) {
			return "⁻" + superscript(number * -1);
		}
		if (number > 9) {
			return String.valueOf(number).chars().mapToObj(i -> superscript(i - 48)).collect(Collectors.joining());
		}
		String hex;
		switch (number) {
			case 1 : hex = "00B9"; break; 
			case 2 : 
			case 3 : hex = "00B" + number; break;
			default: hex = "207" + number; 
		}
		return String.valueOf((char)Integer.parseInt(hex, 16));
	}
}
