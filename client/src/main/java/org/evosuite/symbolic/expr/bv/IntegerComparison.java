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
package org.evosuite.symbolic.expr.bv;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.DSEStats;
import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IntegerComparison extends AbstractExpression<Long> implements
        IntegerValue {

	private static final long serialVersionUID = 8551234172104612736L;

	/**
	 * <p>
	 * Constructor for IntegerComparison.
	 * </p>
	 * 
	 * @param left
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param right
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param con
	 *            a {@link java.lang.Long} object.
	 */
	public IntegerComparison(Expression<Long> left, Expression<Long> right, Long con) {
		super(con, 1 + left.getSize() + right.getSize(), left.containsSymbolicVariable()
		        || right.containsSymbolicVariable());
		this.left = left;
		this.right = right;

		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH) {
			DSEStats.getInstance().reportConstraintTooLong(getSize());
			throw new ConstraintTooLongException(getSize());
		}
	}

	private final Expression<Long> left;
	private final Expression<Long> right;

	protected static final Logger log = LoggerFactory.getLogger(IntegerComparison.class);

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof IntegerComparison) {
			IntegerComparison other = (IntegerComparison) obj;
			return this.left.equals(other.left) && this.right.equals(other.right);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return this.left.hashCode() + this.right.hashCode();
	}

	/**
	 * <p>
	 * getRightOperant
	 * </p>
	 * 
	 * @return a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	public Expression<Long> getRightOperant() {
		return right;
	}

	/**
	 * <p>
	 * getLeftOperant
	 * </p>
	 * 
	 * @return a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	public Expression<Long> getLeftOperant() {
		return left;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "(" + left + " cmp " + right + ")";
	}

	@Override
	public Set<Variable<?>> getVariables() {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		variables.addAll(this.left.getVariables());
		variables.addAll(this.right.getVariables());
		return variables;
	}

	@Override
	public Set<Object> getConstants() {
		Set<Object> result = new HashSet<Object>();
		result.addAll(this.left.getConstants());
		result.addAll(this.right.getConstants());
		return result;
	}

	@Override
	public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}
}
