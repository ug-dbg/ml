package com.github.ugdbg.function;

import java.util.stream.Collectors;

/**
 * A utility class to convert a number into an exponent representation.
 */
public class ExponentFormat {

	/**
	 * Convert a number into a superscript digit representation.
	 * Example : -1234567 → ⁻¹²³⁴⁵⁶⁷
	 * @param number the number to format
	 * @return the superscript representation
	 */
	public static String superscript(int number) {
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
