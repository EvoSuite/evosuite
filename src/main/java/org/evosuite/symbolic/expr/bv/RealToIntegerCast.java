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
package org.evosuite.symbolic.expr.bv;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.DSEStats;
import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.Cast;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Variable;

public final class RealToIntegerCast extends AbstractExpression<Long> implements
        Cast<Double>, IntegerValue {

	private static final long serialVersionUID = 1L;

	private final Expression<Double> expr;

	/**
	 * <p>
	 * Constructor for RealToIntegerCast.
	 * </p>
	 * 
	 * @param _expr
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param _concValue
	 *            a {@link java.lang.Long} object.
	 */
	public RealToIntegerCast(Expression<Double> _expr, Long _concValue) {
		super(_concValue, 1 + _expr.getSize(), _expr.containsSymbolicVariable());
		this.expr = _expr;

		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH) {
			DSEStats.reportConstraintTooLong(getSize());
			throw new ConstraintTooLongException(getSize());
		}
	}

	/** {@inheritDoc} */
	@Override
	public Expression<Double> getArgument() {
		return expr;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "((INT)" + expr + ")";
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof RealToIntegerCast) {
			RealToIntegerCast other = (RealToIntegerCast) obj;
			return this.expr.equals(other.expr);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return this.expr.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public Long execute() {
		Double exprVal = expr.execute();
		return exprVal.longValue();
	}

	@Override
	public Set<Variable<?>> getVariables() {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		variables.addAll(this.expr.getVariables());
		return variables;
	}

	@Override
	public Set<Object> getConstants() {
		return expr.getConstants();
	}
}
