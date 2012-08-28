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
package org.evosuite.symbolic.expr;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegerBinaryExpression extends IntegerExpression implements
        BinaryExpression<Long> {

	private static final long serialVersionUID = -986689442489666986L;

	protected static Logger log = LoggerFactory.getLogger(IntegerBinaryExpression.class);

	protected Long concretValue;

	protected final Operator op;

	protected final Expression<Long> left;
	protected final Expression<Long> right;

	/**
	 * <p>
	 * Constructor for IntegerBinaryExpression.
	 * </p>
	 * 
	 * @param left2
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param op2
	 *            a {@link org.evosuite.symbolic.expr.Operator} object.
	 * @param right2
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param con
	 *            a {@link java.lang.Long} object.
	 */
	public IntegerBinaryExpression(Expression<Long> left2, Operator op2,
	        Expression<Long> right2, Long con) {
		this.concretValue = con;
		this.left = left2;
		this.right = right2;
		this.op = op2;
		this.containsSymbolicVariable = this.left.containsSymbolicVariable()
		        || this.right.containsSymbolicVariable();
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	/** {@inheritDoc} */
	@Override
	public Long getConcreteValue() {
		return concretValue;
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
			return this.op.equals(other.op) && this.getSize() == other.getSize()
			        && this.left.equals(other.left) && this.right.equals(other.right);
		}

		return false;
	}

	protected int size = 0;

	/** {@inheritDoc} */
	@Override
	public int getSize() {
		if (size == 0) {
			size = 1 + left.getSize() + right.getSize();
		}
		return size;
	}

	/** {@inheritDoc} */
	@Override
	public Long execute() {
		long leftVal = ExpressionHelper.getLongResult(left);
		long rightVal = ExpressionHelper.getLongResult(right);
		// long leftVal = left.execute();
		// long rightVal = right.execute();

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

}
