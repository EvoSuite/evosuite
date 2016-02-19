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
/**
 * 
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
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * StringComparison class.
 * </p>
 * 
 * @author krusev
 */
public final class StringBinaryComparison extends AbstractExpression<Long> implements
        StringComparison {

	private static final long serialVersionUID = -2959676064390810341L;

	protected static final Logger log = LoggerFactory.getLogger(StringBinaryComparison.class);

	/**
	 * <p>
	 * Constructor for StringComparison.
	 * </p>
	 * 
	 * @param left
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param op
	 *            a {@link org.evosuite.symbolic.expr.Operator} object.
	 * @param right2
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param con
	 *            a {@link java.lang.Long} object.
	 */
	public StringBinaryComparison(Expression<String> left, Operator op,
	        Expression<?> right, Long con) {
		super(con, 1 + left.getSize() + right.getSize(), left.containsSymbolicVariable()
		        || right.containsSymbolicVariable());
		this.left = left;
		this.op = op;
		this.right = right;

		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH) {
			DSEStats.getInstance().reportConstraintTooLong(getSize());
			throw new ConstraintTooLongException(getSize());
		}
	}

	private final Expression<String> left;
	private final Operator op;
	private final Expression<?> right;

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof StringBinaryComparison) {
			StringBinaryComparison other = (StringBinaryComparison) obj;
			return this.op.equals(other.op) && this.left.equals(other.left)
			        && this.right.equals(other.right);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return this.left.hashCode() + this.op.hashCode() + this.right.hashCode();
	}

	/**
	 * <p>
	 * getRightOperand
	 * </p>
	 * 
	 * @return a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	public Expression<?> getRightOperand() {
		return right;
	}

	/**
	 * <p>
	 * getLeftOperand
	 * </p>
	 * 
	 * @return a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	public Expression<String> getLeftOperand() {
		return left;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "(" + left + op.toString() + right + ")";
	}

	/**
	 * <p>
	 * getOperator
	 * </p>
	 * 
	 * @return a {@link org.evosuite.symbolic.expr.Operator} object.
	 */
	public Operator getOperator() {
		return op;
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
