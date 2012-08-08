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
/**
 * 
 */
package org.evosuite.symbolic.expr;

import java.util.logging.Logger;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;

import gov.nasa.jpf.JPF;

/**
 * <p>
 * StringUnaryExpression class.
 * </p>
 * 
 * @author krusev
 */
public class StringUnaryExpression extends StringExpression implements
		UnaryExpression<String> {

	private static final long serialVersionUID = -384874147850376188L;

	static Logger log = JPF
			.getLogger("org.evosuite.symbolic.expr.StringUnaryExpression");

	protected String concretValue;

	// protected int conretIntValue;

	protected final Operator op;

	protected final Expression<String> expr;

	/**
	 * <p>
	 * Constructor for StringUnaryExpression.
	 * </p>
	 * 
	 * @param param
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param op2
	 *            a {@link org.evosuite.symbolic.expr.Operator} object.
	 * @param con
	 *            a {@link java.lang.String} object.
	 */
	public StringUnaryExpression(Expression<String> param, Operator op2,
			String con) {
		this.concretValue = con;
		this.expr = param;
		this.op = op2;
		this.containsSymbolicVariable = this.expr.containsSymbolicVariable();
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	/** {@inheritDoc} */
	@Override
	public String getConcreteValue() {
		return concretValue;
	}

	/** {@inheritDoc} */
	@Override
	public Operator getOperator() {
		return op;
	}

	/** {@inheritDoc} */
	@Override
	public Expression<String> getOperand() {
		return expr;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return expr + "." + op.toString().trim() + "(" + ")";
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof StringUnaryExpression) {
			StringUnaryExpression other = (StringUnaryExpression) obj;
			return this.op.equals(other.op) && this.expr.equals(other.expr);
		}

		return false;
	}

	protected int size = 0;

	/** {@inheritDoc} */
	@Override
	public int getSize() {
		if (size == 0) {
			size = 1 + expr.getSize();
		}
		return size;
	}

	/** {@inheritDoc} */
	@Override
	public String execute() {
		String exOn = (String) expr.execute();

		switch (op) {

		case TOLOWERCASE:
			return exOn.toLowerCase();
		case TOUPPERCASE:
			return exOn.toUpperCase();
		case TRIM:
			return exOn.trim();
		case LENGTH:
			return Integer.toString(exOn.length());
		default:
			log.warning("StringUnaryExpression: unimplemented operator!");
			return null;
		}
	}

}
