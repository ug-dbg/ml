package com.github.ugdbg.function.vector;

import com.github.ugdbg.vector.Vector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.IntStream;

/**
 * ce:x(x₁,x₂,x₃...xₙ) → y(y₁,y₂,y₃...yₙ)
 * <br>
 * where :
 * <ul>
 *     <li>yₖ = tₖ * log(xₖ) + (1 - tₖ * log(1 - xₖ))</li>
 *     <li>tₖ = (t₁,t₂,t₃...tₙ) is the expected output</li>
 * </ul>
 */
public class CrossEntropy extends DomainCheckedFunction<CrossEntropy> implements ErrorFunction {
	
	private final Vector expected;

	public CrossEntropy(Vector expected) {
		this.expected = expected;
	}

	@Override
	public Vector expected() {
		return this.expected;
	}

	@Override
	public Vector doApply(Vector output) {
		Vector error = Vector.of(output.getValue().getType(), output.dimension());

		IntStream stream = output.getValue().indexStream();
		switch (error.getValue().getType()) {
			case DECIMAL:
				stream.forEach(i -> error.at(i, this.calculate(
					this.expected.getValue().decimals()[i], 
					output.getValue().decimals()[i])
				));
				break;
			case PFLOAT:
				stream.forEach(i -> error.at(i, this.calculate(
					this.expected.getValue().floats()[i], 
					output.getValue().floats()[i])
				));
				break;
			case PDOUBLE:
				stream.forEach(i -> error.at(i, this.calculate(
					this.expected.getValue().doubles()[i], 
					output.getValue().doubles()[i])
				));
				break;
		}
		return error;
	}
	
	protected double calculate(double expected, double value) {
		return (float) (-1f * (expected * Math.log(value) + (1 - expected) * Math.log(1 - value)));
	}
	
	protected BigDecimal calculate(BigDecimal expected, BigDecimal value) {
		BigDecimal oneMinusExpected = BigDecimal.ONE.subtract(expected);
		BigDecimal oneMinusValue    = BigDecimal.ONE.subtract(value);
		BigDecimal logValue = BigDecimal.valueOf(Math.log(value.doubleValue()));
		BigDecimal logOneMinusValue = BigDecimal.valueOf(Math.log(oneMinusValue.doubleValue()));
		
		return (expected.multiply(logValue).add(oneMinusExpected.multiply(logOneMinusValue))).multiply(BigDecimal.valueOf(-1));
	}

	@Override
	public ErrorFunction derive() {
		return new CrossEntropy(CrossEntropy.this.expected()) {
			@Override
			protected double calculate(double expected, double value) {
				return -1 * (expected / value) + (1 - expected) / (1 - value);
			}
			
			@Override
			protected BigDecimal calculate(BigDecimal expected, BigDecimal value) {
				RoundingMode halfDown = RoundingMode.HALF_DOWN;
				BigDecimal expectedValueRatio = expected.divide(value, halfDown);
				BigDecimal oneMinusExpected = BigDecimal.ONE.subtract(expected);
				BigDecimal oneMinusValue    = BigDecimal.ONE.subtract(value);
				return oneMinusExpected
					.divide(oneMinusValue, halfDown)
					.add(expectedValueRatio)
					.multiply(BigDecimal.valueOf(-1));
			}
		};
	}
}
