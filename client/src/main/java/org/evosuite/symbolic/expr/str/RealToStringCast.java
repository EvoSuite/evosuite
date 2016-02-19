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
package org.evosuite.symbolic.expr.str;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.DSEStats;
import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.Cast;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RealToStringCast extends AbstractExpression<String> implements
        StringValue, Cast<Double> {

	private static final long serialVersionUID = -5322228289539145088L;

	protected static Logger log = LoggerFactory.getLogger(RealToStringCast.class);

	private final Expression<Double> expr;

	public RealToStringCast(Expression<Double> _expr, String concVal) {
		super(concVal, 1 + _expr.getSize(), _expr.containsSymbolicVariable());
		this.expr = _expr;

		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH) {
			DSEStats.getInstance().reportConstraintTooLong(getSize());
			throw new ConstraintTooLongException(getSize());
		}

	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "(String)" + expr.toString();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof RealToStringCast) {
			RealToStringCast other = (RealToStringCast) obj;
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
	public Expression<Double> getArgument() {
		return expr;
	}

	@Override
	public Set<Variable<?>> getVariables() {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		variables.addAll(this.expr.getVariables());
		return variables;
	}

	@Override
	public Set<Object> getConstants() {
		return this.expr.getConstants();
	}
	
	@Override
	public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}

}
