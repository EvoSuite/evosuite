/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.expr.fp;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.Variable;

public final class RealVariable extends AbstractExpression<Double> implements RealValue,
        Variable<Double> {
	private static final long serialVersionUID = 1L;

	private final String name;
	private final double minValue;
	private final double maxValue;

	/**
	 * <p>
	 * Constructor for RealVariable.
	 * </p>
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @param conV
	 *            a double.
	 * @param minValue
	 *            a double.
	 * @param maxValue
	 *            a double.
	 */
	public RealVariable(String name, double conV, double minValue, double maxValue) {
		super(conV, 1, true);
		this.name = name;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	/**
	 * <p>
	 * Setter for the field <code>concreteValue</code>.
	 * </p>
	 * 
	 * @param conV
	 *            a double.
	 */
	public final void setConcreteValue(double conV) {
		this.concreteValue = conV;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public Double getMinValue() {
		return minValue;
	}

	/** {@inheritDoc} */
	@Override
	public Double getMaxValue() {
		return maxValue;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.name + "(" + concreteValue + ")";
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RealVariable) {
			RealVariable v = (RealVariable) obj;
			return this.getName().equals(v.getName());
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public Set<Variable<?>> getVariables() {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		variables.add(this);
		return variables;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.symbolic.expr.AbstractExpression#getConstants()
	 */
	@Override
	public Set<Object> getConstants() {
		// Do not include original values?
		return new HashSet<Object>();
	}
	
	@Override
	public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}
}
