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

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;

/**
 * <p>
 * StringBuilderExpression class.
 * </p>
 * 
 * @author krusev
 */
public class StringBuilderExpression extends StringExpression {

	private static final long serialVersionUID = 5944852577642617563L;

	protected boolean undef_func;
	protected Expression<String> expr;

	/**
	 * <p>
	 * Constructor for StringBuilderExpression.
	 * </p>
	 * 
	 * @param _expr
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	public StringBuilderExpression(Expression<String> _expr) {
		this.expr = _expr;
		this.undef_func = false;
		this.containsSymbolicVariable = this.expr.containsSymbolicVariable();
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	/**
	 * <p>
	 * Getter for the field <code>expr</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	public Expression<String> getExpr() {
		return expr;
	}

	/**
	 * <p>
	 * Setter for the field <code>expr</code>.
	 * </p>
	 * 
	 * @param _expr
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	public void setExpr(Expression<String> _expr) {
		expr = _expr;
	}

	/**
	 * <p>
	 * append
	 * </p>
	 * 
	 * @param _expr
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	public void append(Expression<String> _expr) {
		if (expr == null)
			expr = _expr;
		else {
			if (_expr instanceof StringConstant) {
				if (expr instanceof StringConstant) {
					expr = new StringConstant(
							((String) expr.getConcreteValue())
									+ ((String) _expr.getConcreteValue()));
					return;
				} else if (expr instanceof StringBinaryExpression) {
					StringBinaryExpression sBin = (StringBinaryExpression) expr;
					if (sBin.getRightOperand() instanceof StringConstant) {
						StringConstant strConst = new StringConstant(sBin
								.getRightOperand().getConcreteValue()
								+ ((String) _expr.getConcreteValue()));
						expr = new StringBinaryExpression(
								sBin.getLeftOperand(), Operator.APPEND,
								strConst, sBin.getConcreteValue()
										+ ((String) _expr.getConcreteValue()));
						return;
					}
				}

			}

			expr = new StringBinaryExpression(expr, Operator.APPEND, _expr,
					((String) expr.getConcreteValue())
							+ ((String) _expr.getConcreteValue()));
		}
		this.containsSymbolicVariable = expr.containsSymbolicVariable();
	}

	/**
	 * <p>
	 * has_undef_func
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public boolean has_undef_func() {
		return undef_func;
	}

	/**
	 * <p>
	 * set_undef_func
	 * </p>
	 */
	public void set_undef_func() {
		undef_func = true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "StringBuilder(" + expr.toString() + ")";
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof StringBuilderExpression) {
			StringBuilderExpression other = (StringBuilderExpression) obj;
			return this.expr.equals(other.expr)
					&& this.getSize() == other.getSize();
		}

		return false;
	}

	protected int size = 0;

	/** {@inheritDoc} */
	@Override
	public int getSize() {
		int expr_size = 0;
		if (expr != null)
			expr_size = expr.getSize();
		if (size == 0) {
			size = 1 + expr_size;
		}
		return size;
	}

	/** {@inheritDoc} */
	@Override
	public String getConcreteValue() {

		return (String) expr.getConcreteValue();
	}

	/** {@inheritDoc} */
	@Override
	public String execute() {
		return (String) expr.execute();
	}

}
