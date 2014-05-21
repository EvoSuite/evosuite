package org.evosuite.ga.variables;

import java.text.MessageFormat;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Variable for double values
 * 
 * @author José Campos
 */
public class DoubleVariable implements Variable {

	private static final String VALUE_OUT_OF_BOUNDS = 
			"value out of bounds (value: {0}, min: {1}, max: {2})";

	/**
	 * The current value of this variable
	 */
	private double value;

	/**
	 * The lower bound of this variable
	 */
	private final double lowerBound;

	/**
	 * The upper bound of this variable
	 */
	private final double upperBound;

	/**
	 * Constructs a real variable in the range {@code lowerBound <= x <=
	 * upperBound} with an uninitialized value
	 * 
	 * @param lowerBound the lower bound of this variable, inclusive
	 * @param upperBound the upper bound of this variable, inclusive
	 */
	public DoubleVariable(double lowerBound, double upperBound) {
		this(Double.NaN, lowerBound, upperBound);
	}

	/**
	 * Constructs a real variable in the range {@code lowerBound <= x <=
	 * upperBound} with the specified initial value
	 * 
	 * @param value the initial value of this variable
	 * @param lowerBound the lower bound of this variable, inclusive
	 * @param upperBound the upper bound of this variable, inclusive
	 * @throws IllegalArgumentException if the value is out of bounds
	 *         {@code (value < lowerBound) || (value > upperBound)}
	 */
	public DoubleVariable(double value, double lowerBound, double upperBound) {
		super();
		this.value = value;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;

		if ((value < lowerBound) || (value > upperBound)) {
			throw new IllegalArgumentException(MessageFormat.format(
					VALUE_OUT_OF_BOUNDS, value, lowerBound, upperBound));
		}
	}

	/**
	 * Returns the current value of this variable
	 * 
	 * @return the current value of this variable
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Sets the value of this variable
	 * 
	 * @param value the new value for this variable
	 * @throws IllegalArgumentException if the value is out of bounds
	 *         {@code (value < getLowerBound()) || (value > getUpperBound())}
	 */
	public void setValue(double value) {
		if ((value < lowerBound) || (value > upperBound)) {
			throw new IllegalArgumentException(MessageFormat.format(
					VALUE_OUT_OF_BOUNDS, value, lowerBound, upperBound));
		}

		this.value = value;
	}

	/**
	 * Returns the lower bound of this variable
	 * 
	 * @return the lower bound of this variable, inclusive
	 */
	public double getLowerBound() {
		return lowerBound;
	}

	/**
	 * Returns the upper bound of this variable
	 * 
	 * @return the upper bound of this variable, inclusive
	 */
	public double getUpperBound() {
		return upperBound;
	}

	@Override
	public DoubleVariable clone() {
		return new DoubleVariable(value, lowerBound, upperBound);
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(lowerBound)
				.append(upperBound)
				.append(value)
				.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if ((obj == null) || (obj.getClass() != getClass())) {
			return false;
		} else {
			DoubleVariable rhs = (DoubleVariable)obj;

			return new EqualsBuilder()
					.append(lowerBound, rhs.lowerBound)
					.append(upperBound, rhs.upperBound)
					.append(value, rhs.value)
					.isEquals();
		}
	}
}
