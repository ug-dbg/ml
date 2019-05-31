package com.github.ugdbg.vector.format;

import org.apache.commons.lang3.StringUtils;

/**
 * A formatter for Floats with a fixed length. 
 * <br>
 * Useful to format matrices or vector. 
 * <br>
 */
public class FloatFormat implements Format {
	private final int length;
	private final String format;

	/**
	 * Default constructor : give me the number of digits and decimals.
	 * <br>
	 * Output length will always be digits + decimals +  
	 * @param digits   the number of digits (before ".")
	 * @param decimals the number of decimals (after ".")
	 */
	public FloatFormat(int digits, int decimals) {
		this.format = "%1$+" + digits + "." + decimals + "f";
		this.length = digits + decimals + 2;
	}

	/**
	 * The output length of any float after it is formatted.
	 * @return {@link #length}
	 */
	@Override
	public int getLength() {
		return this.length;
	}

	/**
	 * Format a given float.
	 * <ul>
	 *     <li>The output length will ALWAYS be {@link #length}</li>
	 *     <li>if the float cannot be formatted, a centered '!' is returned</li>
	 *     <li>There is always a leading +/- sign</li>
	 * </ul>
	 * @param value the float value to format
	 * @return the formatted value
	 */
	@Override
	public String format(Object value) {
		String out = String.format(this.format, value);
		if (value != null && value.equals(Float.NEGATIVE_INFINITY)) {
			out = "−∞";
		}
		
		if (value != null && value.equals(Float.POSITIVE_INFINITY)) {
			out = "+∞";
		}
		
		out = StringUtils.center(out, this.length);
		if (out.length() > this.length) {
			out = StringUtils.center("!", this.length);
		}
		return out;
	}
}
