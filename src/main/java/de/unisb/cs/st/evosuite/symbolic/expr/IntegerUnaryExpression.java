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
package de.unisb.cs.st.evosuite.symbolic.expr;

import gov.nasa.jpf.JPF;

import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.symbolic.ConstraintTooLongException;

public class IntegerUnaryExpression extends IntegerExpression implements
        UnaryExpression<Long> {

	private static final long serialVersionUID = 1966395070897274841L;

	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.expr.IntegerUnaryExpression");
	
	protected Long concretValue;

	protected Operator op;

	protected Expression<Long> expr;

	public IntegerUnaryExpression(Expression<Long> e, Operator op2, Long con) {
		this.expr = e;
		this.op = op2;
		this.concretValue = con;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	@Override
	public Long getConcreteValue() {
		return concretValue;
	}

	@Override
	public Expression<Long> getOperand() {
		return expr;
	}

	@Override
	public Operator getOperator() {
		return op;
	}

	@Override
	public String toString() {
		return "(" + op.toString() + "(" + expr + "))";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IntegerUnaryExpression) {
			IntegerUnaryExpression v = (IntegerUnaryExpression) obj;
			return this.op.equals(v.op) 
					&& this.getSize() == v.getSize()
			        && this.expr.equals(v.expr);
		}
		return false;
	}

	protected int size = 0;

	@Override
	public int getSize() {
		if (size == 0) {
			size = 1 + expr.getSize();
		}
		return size;
	}

	@Override
	public Long execute() {
		long leftVal = ExpressionHelper.getLongResult(expr);
		
		switch (op) {
		
		case NEG:
			return -leftVal;
		case ABS:
			return Math.abs(leftVal);
		default:
			log.warning("IntegerUnaryExpression: unimplemented operator!");
			return null;
		}
	}

}
