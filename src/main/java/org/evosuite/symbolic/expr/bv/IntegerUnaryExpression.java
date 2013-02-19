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
import org.evosuite.symbolic.DSEStats;
import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.UnaryExpression;
import org.evosuite.symbolic.expr.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IntegerUnaryExpression extends AbstractExpression<Long> implements
        IntegerValue, UnaryExpression<Long> {

	private static final long serialVersionUID = 1966395070897274841L;

	protected static Logger log = LoggerFactory.getLogger(IntegerUnaryExpression.class);

	private final Operator op;
	private final Expression<Long> expr;

	/**
	 * <p>
	 * Constructor for IntegerUnaryExpression.
	 * </p>
	 * 
	 * @param e
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param op2
	 *            a {@link org.evosuite.symbolic.expr.Operator} object.
	 * @param con
	 *            a {@link java.lang.Long} object.
	 */
	public IntegerUnaryExpression(Expression<Long> e, Operator op2, Long con) {
		super(con, 1 + e.getSize(), e.containsSymbolicVariable());
		this.expr = e;
		this.op = op2;

		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH) {
			DSEStats.reportConstraintTooLong(getSize());
			throw new ConstraintTooLongException(getSize());
		}
	}

	/** {@inheritDoc} */
	@Override
	public Expression<Long> getOperand() {
		return expr;
	}

	/** {@inheritDoc} */
	@Override
	public Operator getOperator() {
		return op;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "(" + op.toString() + "(" + expr + "))";
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IntegerUnaryExpression) {
			IntegerUnaryExpression v = (IntegerUnaryExpression) obj;
			return this.op.equals(v.op) && this.expr.equals(v.expr);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.expr.hashCode() + this.op.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public Long execute() {
		long leftVal = expr.execute();

		switch (op) {

		case NEG:
			return -leftVal;
		case ABS:
			return Math.abs(leftVal);
		case GETNUMERICVALUE:
			return (long) Character.getNumericValue((char) leftVal);
		case ISLETTER:
			return Character.isLetter((char) leftVal) ? 1L : 0L;
		case ISDIGIT:
			return Character.isDigit((char) leftVal) ? 1L : 0L;

		default:
			log.warn("IntegerUnaryExpression: unimplemented operator: " + op);
			return null;
		}
	}

	@Override
	public Set<Variable<?>> getVariables() {
		Set<Variable<?>> variables = new THashSet<Variable<?>>();
		variables.addAll(this.expr.getVariables());
		return variables;
	}

	@Override
	public Set<Object> getConstants() {
		return this.expr.getConstants();
	}

}
