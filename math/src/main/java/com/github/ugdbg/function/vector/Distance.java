package com.github.ugdbg.function.vector;

import com.github.ugdbg.datatypes.TYPE;
import com.github.ugdbg.vector.Vector;

import java.math.BigDecimal;
import java.util.stream.IntStream;

/**
 * d:x(x₁,x₂,x₃...xₙ) → y(y₁,y₂,y₃...yₙ) where yₖ = (xₖ - tₖ)² and tₖ = (t₁,t₂,t₃...tₙ) is the expected output.
 */
public class Distance extends DomainCheckedFunction<Distance> implements ErrorFunction {
	
	private final Vector expected;

	public Distance(float[] expected) {
		this.expected = Vector.of(expected);
	}

	@Override
	public Vector expected() {
		return this.expected;
	}

	@Override
	public Vector doApply(Vector output) {
		TYPE type = output.getValue().getType();
		Vector distance = Vector.of(type, output.dimension());
		IntStream stream = output.getValue().indexStream();

		switch (type) {
			case PFLOAT:  stream.forEach(
					i -> distance.floats()[i] = (float) this.calculate(this.expected.floats()[i], output.floats()[i])
				);
				break;
			case PDOUBLE: stream.forEach(
					i -> distance.doubles()[i] = this.calculate(this.expected.doubles()[i], output.doubles()[i])
				);
				break;
			case DECIMAL:
				stream.forEach(
					i -> distance.decimals()[i] = this.calculate(this.expected.decimals()[i], output.decimals()[i])
				);
				break;
		}
		return distance;
	}

	@Override
	public VFunction derive() {
		return (VFunction) input -> {
			TYPE type = input.getValue().getType();
			Vector out = Vector.of(type, input.dimension());
			IntStream stream = input.getValue().indexStream();

			switch (type) {
				case PFLOAT: stream.forEach(i -> out.floats()[i] = 2 * (input.floats()[i] - this.expected.floats()[i]));
					break;
				case PDOUBLE: stream.forEach(i -> out.doubles()[i] = 2 * (input.doubles()[i] - this.expected.doubles()[i]));
					break;
				case DECIMAL: stream.forEach(
						i -> out.decimals()[i] = 
							(input.decimals()[i]
							.subtract(this.expected.decimals()[i])
							.multiply(BigDecimal.valueOf(2)))
					);
					break;
			}
			return out;
		};
	}
	
	private double calculate(double expected, double value) {
		return (float) Math.pow(value - expected, 2);
	}
	
	private BigDecimal calculate(BigDecimal expected, BigDecimal value) {
		return value.subtract(expected).pow(2);
	}
}
