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
import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.BinaryExpression;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IntegerBinaryExpression extends AbstractExpression<Long>
		implements IntegerValue, BinaryExpression<Long> {

	private static final long serialVersionUID = -986689442489666986L;

	protected static final Logger log = LoggerFactory
			.getLogger(IntegerBinaryExpression.class);

	private final Expression<Long> left;
	private final Operator op;
	private final Expression<Long> right;

	/**
	 * <p>
	 * Constructor for IntegerBinaryExpression.
	 * </p>
	 * 
	 * @param left
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param op2
	 *            a {@link org.evosuite.symbolic.expr.Operator} object.
	 * @param right
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param con
	 *            a {@link java.lang.Long} object.
	 */
	public IntegerBinaryExpression(Expression<Long> left, Operator op2,
			Expression<Long> right, Long con) {
		super(con, 1 + left.getSize() + right.getSize(), left
				.containsSymbolicVariable() || right.containsSymbolicVariable());
		this.left = left;
		this.right = right;
		this.op = op2;

		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException(getSize());
	}

	/** {@inheritDoc} */
	@Override
	public Operator getOperator() {
		return op;
	}

	/** {@inheritDoc} */
	@Override
	public Expression<Long> getLeftOperand() {
		return left;
	}

	/** {@inheritDoc} */
	@Override
	public Expression<Long> getRightOperand() {
		return right;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "(" + left + op.toString() + right + ")";
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj instanceof IntegerBinaryExpression) {
			IntegerBinaryExpression other = (IntegerBinaryExpression) obj;
			return this.op.equals(other.op) && this.left.equals(other.left)
					&& this.right.equals(other.right);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return this.left.hashCode() + this.op.hashCode()
				+ this.right.hashCode();
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
