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
 */
package org.evosuite.symbolic.expr;

import gov.nasa.jpf.JPF;

import java.util.logging.Logger;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;


public class IntegerBinaryExpression extends IntegerExpression implements
        BinaryExpression<Long> {

	private static final long serialVersionUID = -986689442489666986L;

	static Logger log = JPF.getLogger("org.evosuite.symbolic.expr.IntegerBinaryExpression");
	
	protected Long concretValue;

	protected Operator op;

	protected Expression<Long> left;
	protected Expression<Long> right;

	public IntegerBinaryExpression(Expression<Long> left2, Operator op2,
	        Expression<Long> right2, Long con) {
		this.concretValue = con;
		this.left = left2;
		this.right = right2;
		this.op = op2;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	@Override
	public Long getConcreteValue() {
		return concretValue;
	}

	@Override
	public Operator getOperator() {
		return op;
	}

	@Override
	public Expression<Long> getLeftOperand() {
		return left;
	}

	@Override
	public Expression<Long> getRightOperand() {
		return right;
	}

	@Override
	public String toString() {
		return "(" + left + op.toString() + right + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof IntegerBinaryExpression) {
			IntegerBinaryExpression other = (IntegerBinaryExpression) obj;
			return this.op.equals(other.op) 
					&& this.getSize() == other.getSize()
			        && this.left.equals(other.left) && this.right.equals(other.right);
		}

		return false;
	}

	protected int size = 0;
	@Override
	public int getSize() {
		if (size == 0) {
			size = 1 + left.getSize() + right.getSize();
		}
		return size;
	}

	@Override
	public Long execute() {
		long leftVal = ExpressionHelper.getLongResult(left);
		long rightVal = ExpressionHelper.getLongResult(right);
		
		switch (op) {
		
		case SHL:
			return leftVal << rightVal;
		case SHR:
			return leftVal >> rightVal;
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
			log.warning("IntegerBinaryExpression: unimplemented operator!");
			return null;
		}
	}

}
