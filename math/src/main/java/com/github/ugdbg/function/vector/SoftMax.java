package com.github.ugdbg.function.vector;

import com.github.ugdbg.datatypes.TYPE;
import com.github.ugdbg.vector.Vector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * sm:x(x₁,x₂,x₃...xₙ) → y(y₁,y₂,y₃...yₙ)
 * <br>
 * where yₖ = exp(xₖ) / ∑₁→ₙ (exp(x₁),exp(x₂),exp(x₃)...exp(xₙ)) .
 */
public class SoftMax extends DomainCheckedFunction<SoftMax> implements VDerivable {
	@Override
	public Vector doApply(Vector input) {
		TYPE type = input.getValue().getType();
		Vector out = Vector.of(type, input.dimension());
		IntStream stream = input.getValue().indexStream();
		
		switch (type) {
			case PFLOAT:
				float[] floats = input.floats();
				stream.forEach(index -> out.floats()[index] = (float) (Math.exp(floats[index]) / this.expSum(floats)));
				break;
			case PDOUBLE:
				double[] doubles = input.doubles();
				stream.forEach(index -> out.doubles()[index] = Math.exp(doubles[index]) / this.expSum(doubles));
				break;
			case DECIMAL:
				BigDecimal[] decimals = input.decimals();
				stream.forEach(index -> out.decimals()[index] = 
					BigDecimal
					.valueOf(Math.exp(decimals[index].doubleValue()))
					.divide(this.expSum(decimals), RoundingMode.HALF_DOWN)
				);
				break;
		}
		
		return out;
	}

	@Override
	public VFunction derive() {
		return (VFunction) input -> this.jacobian(input).apply(input);
	}

	@Override
	public String label() {
		return "e(xₖ) / ∑₁→ₙ (e(x₁),e(x₂),e(x₃)...e(xₙ))";
	}

	private Matrix jacobian(Vector input) {
		int dimension = input.dimension();
		Vector softmaxOut = SoftMax.this.apply(input);

		TYPE type = input.getValue().getType();
		Matrix jacobian = new Matrix(dimension, dimension, type);
		switch (type) {
			case PFLOAT:
				return jacobian.operation(
					(matrix, i, j) -> matrix.at(i, j, jacobianOperation(i, j, softmaxOut.floats()))
				);
			case PDOUBLE:
				return jacobian.operation(
					(matrix, i, j) -> matrix.at(i, j, jacobianOperation(i, j, softmaxOut.doubles()))
				);
			case DECIMAL:
				return jacobian.operation(
					(matrix, i, j) -> matrix.at(i, j, jacobianOperation(i, j, softmaxOut.decimals()))
				);
			default: throw new IllegalArgumentException("Unsupported input vector type [" + type + "]");
		}
	}
	
	private static float jacobianOperation(int i, int j, float[] softmaxOut) {
		return softmaxOut[j] * (Matrix.kroneckerDelta(i, j) - softmaxOut[i]);
	}
	
	private static double jacobianOperation(int i, int j, double[] softmaxOut) {
		return softmaxOut[j] * (Matrix.kroneckerDelta(i, j) - softmaxOut[i]);
	}
	
	private static BigDecimal jacobianOperation(int i, int j, BigDecimal[] softmaxOut) {
		return softmaxOut[j].multiply(BigDecimal.valueOf(Matrix.kroneckerDelta(i, j)).subtract(softmaxOut[i]));
	}
	
	private float expSum(float[] input) {
		return (float) IntStream.range(0, input.length).mapToDouble(i -> Math.exp(input[i])).sum();
	}
	
	private double expSum(double[] input) {
		return (float) Arrays.stream(input).map(Math::exp).sum();
	}
	
	private BigDecimal expSum(BigDecimal[] input) {
		return IntStream
			.of(0, input.length)
			.mapToObj(i -> BigDecimal.valueOf(Math.exp(input[i].doubleValue())))
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
