package com.github.ugdbg;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Miscellaneous utility methods for number conversions and operations.
 * <br>
 * Some of this code comes from Spring's NumberUtils.
 */
public abstract class NumberUtils {

	public static final BigDecimal E;
	public static final MathContext MATH_CONTEXT = new MathContext(10000, RoundingMode.HALF_UP);
	
	private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
	private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

	static {
		E = computeE(MATH_CONTEXT);
	}
	
	/** 'integer' types */
	private static final List<Class> INTEGERS = Arrays.asList(
		byte.class, 
		Byte.class, 
		short.class, 
		Short.class, 
		int.class, 
		Integer.class,
		long.class,
		Long.class
	);
	
	/** 'floating point precision' types */
	private static final List<Class> FLOATS = Arrays.asList(
		float.class, 
		Float.class, 
		double.class, 
		Double.class 
	);

	/**
	 * Compute 'e' using the Taylor series of the Exponential function for x = 1. 
	 * @param context the Math context to use (BigDecimal division)
	 * @return the BigDecimal value for 'e' using the given context
	 */
	public static BigDecimal computeE(MathContext context) {
		BigDecimal e = BigDecimal.ONE;
		BigDecimal fact = BigDecimal.ONE;
		
		for(int i = 1; i < 100; i++) {
			fact = fact.multiply(new BigDecimal(i));
			e = e.add(BigDecimal.ONE.divide(fact, context));
		}
		
		return e;
	}
	
	/**
	 * Convert the given number into an instance of the given target class.
	 * Supported Numbers : 
	 * <ul>
	 *     <li>{@link java.lang.Byte}</li>
	 *     <li>{@link java.lang.Short}</li>
	 *     <li>{@link java.lang.Integer}</li>
	 *     <li>{@link java.lang.Long}</li>
	 *     <li>{@link java.math.BigInteger}</li>
	 *     <li>{@link java.lang.Float}</li>
	 *     <li>{@link java.lang.Double}</li>
	 *     <li>{@link java.math.BigDecimal}</li>
	 * </ul>
	 * @param number      the number to convert
	 * @param targetClass the target class to convert to
	 * @return the converted number
	 * @throws IllegalArgumentException if the target class is not supported
	 * (i.e. not a standard Number subclass as included in the JDK)
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T convertNumberToTargetClass(
		Number number, 
		Class<T> targetClass)
		throws IllegalArgumentException {

		Class<? extends Number> wrapped = (Class) ClassUtils.primitiveToWrapper(targetClass);

		if (wrapped.isInstance(number)) {
			return (T) number;
		} else if (Byte.class == wrapped) {
			long value = checkedLongValue(number, wrapped);
			if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
				raiseOverflowException(number, wrapped);
			}
			return (T) Byte.valueOf(number.byteValue());
		} else if (Short.class == wrapped) {
			long value = checkedLongValue(number, wrapped);
			if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
				raiseOverflowException(number, wrapped);
			}
			return (T) Short.valueOf(number.shortValue());
		} else if (Integer.class == wrapped) {
			long value = checkedLongValue(number, wrapped);
			if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
				raiseOverflowException(number, wrapped);
			}
			return (T) Integer.valueOf(number.intValue());
		} else if (Long.class == wrapped) {
			long value = checkedLongValue(number, wrapped);
			return (T) Long.valueOf(value);
		} else if (BigInteger.class == wrapped) {
			if (number instanceof BigDecimal) {
				// do not lose precision - use BigDecimal's own conversion
				return (T) ((BigDecimal) number).toBigInteger();
			} else {
				// original value is not a Big* number - use standard long conversion
				return (T) BigInteger.valueOf(number.longValue());
			}
		} else if (Float.class == wrapped) {
			return (T) Float.valueOf(number.floatValue());
		} else if (Double.class == wrapped) {
			return (T) Double.valueOf(number.doubleValue());
		} else if (BigDecimal.class == wrapped) {
			// always use BigDecimal(String) here to avoid unpredictability of BigDecimal(double)
			// (see BigDecimal javadoc for details)
			return (T) new BigDecimal(number.toString());
		} else {
			throw new IllegalArgumentException(
				"Could not convert number [" + number + "] " 
				+ "of type [" + number.getClass().getName() + "] " 
				+ "to unsupported target class [" + targetClass.getName() + "]"
			);
		}
	}

	/**
	 * Check for a {@code BigInteger}/{@code BigDecimal} long overflow before returning the given number as a long value.
	 * @param number      the number to convert
	 * @param targetClass the target class to convert to
	 * @return the long value, if convertible without overflow
	 * @throws IllegalArgumentException if there is an overflow
	 * @see #raiseOverflowException
	 */
	private static long checkedLongValue(Number number, Class<? extends Number> targetClass) {
		BigInteger bigInt = null;
		if (number instanceof BigInteger) {
			bigInt = (BigInteger) number;
		}
		else if (number instanceof BigDecimal) {
			bigInt = ((BigDecimal) number).toBigInteger();
		}
		// Effectively analogous to JDK 8's BigInteger.longValueExact()
		if (bigInt != null && (bigInt.compareTo(LONG_MIN) < 0 || bigInt.compareTo(LONG_MAX) > 0)) {
			raiseOverflowException(number, targetClass);
		}
		return number.longValue();
	}

	/**
	 * Raise an <em>overflow</em> exception for the given number and target class.
	 * @param number      the number we tried to convert
	 * @param targetClass the target class we tried to convert to
	 * @throws IllegalArgumentException if there is an overflow
	 */
	private static void raiseOverflowException(Number number, Class<?> targetClass) {
		throw new IllegalArgumentException(
			"Could not convert number [" + number + "] " 
			+ "of type [" + number.getClass().getName() + "] " 
			+ "to target class [" + targetClass.getName() + "]: overflow"
		);
	}

	/**
	 * Get the '1' value as a target Number type
	 * @param target the target Number class
	 * @param <T> the target number type
	 * @return a target type instance whose value is '1'.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number & Comparable<T>> T one(Class<T> target) {
		if (INTEGERS.contains(target) || FLOATS.contains(target)) {
			return convertNumberToTargetClass(1f, target);
		}
		if (BigInteger.class.isAssignableFrom(target)) {
			return (T) BigInteger.ONE;
		}
		if (BigDecimal.class.isAssignableFrom(target)) {
			return (T) BigDecimal.ONE;
		}
		throw new IllegalArgumentException("Unknown Number type [" + target + "]");
	}
	
	/**
	 * Get the '0' value as a target Number type
	 * @param target the target Number class
	 * @param <T> the target number type
	 * @return a target type instance whose value is '0'.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T zero(Class<T> target) {
		if (INTEGERS.contains(target) || FLOATS.contains(target)) {
			return convertNumberToTargetClass(0f, target);
		}
		if (BigInteger.class.isAssignableFrom(target)) {
			return (T) BigInteger.ZERO;
		}
		if (BigDecimal.class.isAssignableFrom(target)) {
			return (T) BigDecimal.ZERO;
		}
		throw new IllegalArgumentException("Unknown Number type [" + target + "]");
	}

	/**
	 * Add 2 numbers (a + b).
	 * @param a the left operand
	 * @param b the right operand
	 * @param <T> the number type
	 * @return a new target type instance whose value is the sum of 'a' and 'b'
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T add(T a, T b) {
		if (INTEGERS.contains(a.getClass())) {
			return convertNumberToTargetClass(a.intValue() + b.intValue(), (Class<T>) a.getClass());
		}
		if (FLOATS.contains(a.getClass())) {
			return convertNumberToTargetClass(a.doubleValue() + b.doubleValue(), (Class<T>) a.getClass());
		}
		if (a instanceof BigInteger) {
			return (T) ((BigInteger) a).add((BigInteger) b);
		}
		if (a instanceof BigDecimal) {
			return (T) ((BigDecimal) a).add((BigDecimal) b);
		}
		throw new IllegalArgumentException("Unknown Number type [" + a.getClass() + "]");
	}
	
	/**
	 * Subtract 2 numbers (a - b).
	 * @param a the left operand
	 * @param b the right operand
	 * @param <T> the number type
	 * @return a new target type instance whose value is the difference between 'a' and 'b'
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T sub(T a, T b) {
		if (INTEGERS.contains(a.getClass())) {
			return convertNumberToTargetClass(a.intValue() - b.intValue(), (Class<T>) a.getClass());
		}
		if (FLOATS.contains(a.getClass())) {
			return convertNumberToTargetClass(a.doubleValue() - b.doubleValue(), (Class<T>) a.getClass());
		}
		if (a instanceof BigInteger) {
			return (T) ((BigInteger) a).subtract((BigInteger) b);
		}
		if (a instanceof BigDecimal) {
			return (T) ((BigDecimal) a).subtract((BigDecimal) b);
		}
		throw new IllegalArgumentException("Unknown Number type [" + a.getClass() + "]");
	}
	
	/**
	 * Multiply 2 numbers (a * b).
	 * @param a the left operand
	 * @param b the right operand
	 * @param <T> the number type
	 * @return a new target type instance whose value is the product of 'a' multiplied by 'b'
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T mult(T a, T b) {
		if (INTEGERS.contains(a.getClass())) {
			return convertNumberToTargetClass(a.intValue() * b.intValue(), (Class<T>) a.getClass());
		}
		if (FLOATS.contains(a.getClass())) {
			return convertNumberToTargetClass(a.doubleValue() * b.doubleValue(), (Class<T>) a.getClass());
		}
		if (a instanceof BigInteger) {
			return (T) ((BigInteger) a).multiply((BigInteger) b);
		}
		if (a instanceof BigDecimal) {
			return (T) ((BigDecimal) a).multiply((BigDecimal) b);
		}
		throw new IllegalArgumentException("Unknown Number type [" + a.getClass() + "]");
	}
	
	/**
	 * Divide 2 numbers (a / b).
	 * @param a the left operand
	 * @param b the right operand
	 * @param <T> the number type
	 * @return a new target type instance whose value is the result of 'a' / 'b'
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T div(T a, T b) {
		if (INTEGERS.contains(a.getClass())) {
			return convertNumberToTargetClass(a.intValue() / b.intValue(), (Class<T>) a.getClass());
		}
		if (FLOATS.contains(a.getClass())) {
			return convertNumberToTargetClass(a.doubleValue() / b.doubleValue(), (Class<T>) a.getClass());
		}
		if (a instanceof BigInteger) {
			return (T) ((BigInteger) a).divide((BigInteger) b);
		}
		if (a instanceof BigDecimal) {
			return (T) ((BigDecimal) a).divide((BigDecimal) b, RoundingMode.HALF_DOWN);
		}
		throw new IllegalArgumentException("Unknown Number type [" + a.getClass() + "]");
	}
	
	/**
	 * Multiply a Number by a floating point value.
	 * @param a      the left operand
	 * @param scalar the right operand
	 * @param <T> the number type
	 * @return a new target type instance whose value is the product of 'a' multiplied by 'scalar'
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T mult(T a, double scalar) {
		if (INTEGERS.contains(a.getClass())) {
			return convertNumberToTargetClass(a.intValue() * scalar, (Class<T>) a.getClass());
		}
		if (FLOATS.contains(a.getClass())) {
			return convertNumberToTargetClass(a.doubleValue() * scalar, (Class<T>) a.getClass());
		}
		if (a instanceof BigInteger) {
			return (T) new BigDecimal(a.toString()).multiply(new BigDecimal(String.valueOf(scalar))).toBigInteger();
		}
		if (a instanceof BigDecimal) {
			return (T) ((BigDecimal) a).multiply((new BigDecimal(String.valueOf(scalar))));
		}
		throw new IllegalArgumentException("Unknown Number type [" + a.getClass() + "]");
	}

	/**
	 * Get the absolute value of a number.
	 * @param number the input number
	 * @param <T> the number type
	 * @return a new target type instance whose value is the absolute value of 'number'
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T abs(T number) {
		if (INTEGERS.contains(number.getClass())) {
			return (T) convertNumberToTargetClass(Math.abs(number.intValue()), number.getClass());
		}
		if (FLOATS.contains(number.getClass())) {
			return (T) convertNumberToTargetClass(Math.abs(number.doubleValue()), number.getClass());
		}
		
		if (number instanceof BigInteger) {
			return (T) ((BigInteger) number).abs();
		}
		if (number instanceof BigDecimal) {
			return (T) ((BigDecimal) number).abs();
		}
		throw new IllegalArgumentException("Unknown Number type [" + number.getClass() + "]");
	}

	/**
	 * Sum a collection of numbers.
	 * @param type    the numbers type class
	 * @param numbers the numbers to sum
	 * @param <T> the number type
	 * @return a new target type instance whose value is the sum of 'numbers'
	 */
	public static <T extends Number> T sum(Class<T> type, Collection<T> numbers) {
		AtomicReference<T> sum = new AtomicReference<>(zero(type));
		numbers.forEach(n -> sum.set(add(sum.get(), n)));
		return sum.get();
	}
	
	/**
	 * Compute the average value of a collection of numbers.
	 * @param type    the numbers type class
	 * @param numbers the numbers to sum
	 * @param abs     if true, numbers will be summed as absolute values.
	 * @param <T> the number type
	 * @return a new target type instance whose value is the sum of 'numbers'
	 * @see #abs(Number)
	 */
	public static <T extends Number> T avg(Class<T> type, Collection<T> numbers, boolean abs) {
		if (CollectionUtils.isEmpty(numbers)) {
			return zero(type);
		}
		Function<T, T> mapper = abs ? NumberUtils::abs : Function.identity();
		AtomicReference<T> sum = new AtomicReference<>(zero(type));
		numbers.stream().map(mapper).forEach(n -> sum.set(add(sum.get(), n)));
		return div(sum.get(), convertNumberToTargetClass(numbers.size(), type));
	}

	/**
	 * Normalize a number (cross multiplication) into the [0, 1] segment.
	 * e.g. (75, 0, 100) → 0.75  
	 * @param number the input number
	 * @param min    the number min bound inclusive
	 * @param max    the number max bound inclusive
	 * @param <T> the number type
	 * @return a new target type instance whose value is the input number normalized to the [0, 1] segment.
	 */
	public static <T extends Number> T normalize(T number, T min, T max) {
		return div(sub(number, min), sub(max, min));
	}

	/**
	 * Find the max number in a collection
	 * @param numbers the numbers
	 * @param <T> the numbers type
	 * @return the existing instance in numbers whose value is the highest, or null if the collection is empty
	 */
	public static <T extends Number> T max(Collection<T> numbers) {
		return new TreeSet<>(numbers).pollLast();
	}
}