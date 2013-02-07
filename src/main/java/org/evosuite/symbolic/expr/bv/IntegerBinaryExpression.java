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

import gnu.trove.set.hash.THashSet;

import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.BinaryExpression;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IntegerBinaryExpression extends AbstractExpression<Long>
		implements IntegerValue, BinaryExpression<Long> {

	private static final long serialVersionUID = -986689442489666986L;

	protected static Logger log = LoggerFactory
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

	/** {@inheritDoc} */
	@Override
	public Long execute() {
		long leftVal = left.execute();
		long rightVal = right.execute();

		switch (op) {

		case SHL:
			return leftVal << rightVal;
		case SHR:
			return leftVal >> rightVal;
		case USHR:
			return leftVal >>> rightVal;
		case AND:
		case IAND:
			return leftVal & rightVal;
		case OR:
		case IOR:
			return leftVal | rightVal;
		case XOR:
		case IXOR:
			return leftVal ^ rightVal;
		case DIV:
			return leftVal / rightVal;
		case MUL:
			return leftVal * rightVal;
		case MINUS:
			return leftVal - rightVal;
		case PLUS:
			return leftVal + rightVal;
		case REM:
			return leftVal % rightVal;
		case MAX:
			return Math.max(leftVal, rightVal);
		case MIN:
			return Math.min(leftVal, rightVal);

		default:
			log.warn("IntegerBinaryExpression: unimplemented operator: " + op);
			return null;
		}
	}

	@Override
	public Set<Variable<?>> getVariables() {
		Set<Variable<?>> variables = new THashSet<Variable<?>>();
		variables.addAll(this.left.getVariables());
		variables.addAll(this.right.getVariables());
		return variables;
	}

}
