/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Gordon Fraser
 */
package org.evosuite.symbolic.expr.fp;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.Variable;

public final class RealConstant extends AbstractExpression<Double> implements RealValue {
	private static final long serialVersionUID = 1L;

	/**
	 * <p>
	 * Constructor for RealConstant.
	 * </p>
	 * 
	 * @param doubleValue
	 *            a double.
	 */
	public RealConstant(double doubleValue) {
		super(doubleValue, 1, false);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return Double.toString(this.concreteValue);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RealConstant) {
			RealConstant v = (RealConstant) obj;
			return this.concreteValue.equals(v.concreteValue);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.concreteValue.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public Double execute() {
		return this.concreteValue;
	}

	@Override
	public Set<Variable<?>> getVariables() {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		return variables;
	}

}
