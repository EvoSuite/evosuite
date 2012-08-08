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

import java.util.ArrayList;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;


/**
 * <p>StringMultipleExpression class.</p>
 *
 * @author krusev
 */
public class StringMultipleExpression extends StringBinaryExpression implements
        BinaryExpression<String> {

	private static final long serialVersionUID = 7172041118401792672L;

	protected ArrayList<Expression<?>> other_v;

	/**
	 * <p>Constructor for StringMultipleExpression.</p>
	 *
	 * @param _left a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param _op a {@link org.evosuite.symbolic.expr.Operator} object.
	 * @param _right a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param _other a {@link java.util.ArrayList} object.
	 * @param con a {@link java.lang.String} object.
	 */
	public StringMultipleExpression(Expression<String> _left, Operator _op,
	        Expression<?> _right, ArrayList<Expression<?>> _other, String con) {
		super(_left, _op, _right, con);
		this.other_v = _other;
		
		if (_left.containsSymbolicVariable()) {
			this.containsSymbolicVariable=true;
		} else if (_right.containsSymbolicVariable()) {
			this.containsSymbolicVariable=true;
		} else {
			for (Expression<?> expression : _other) {
				if (expression.containsSymbolicVariable()) {
					this.containsSymbolicVariable=true;
					break;
				}
			}
		}
		
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	/**
	 * <p>getOther</p>
	 *
	 * @return the other
	 */
	public ArrayList<Expression<?>> getOther() {
		return other_v;
	}

	/** {@inheritDoc} */
	@Override
	public String getConcreteValue() {
		return super.getConcreteValue();
	}

	/** {@inheritDoc} */
	@Override
	public Operator getOperator() {
		return super.getOperator();
	}

	/** {@inheritDoc} */
	@Override
	public Expression<String> getLeftOperand() {
		return super.getLeftOperand();
	}

	/** {@inheritDoc} */
	@Override
	public Expression<?> getRightOperand() {
		return super.getRightOperand();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String str_other_v = "";
		for (int i = 0; i < this.other_v.size(); i++) {
			str_other_v += " " + this.other_v.get(i).toString();
		}

		return "(" + left + op.toString() + (right == null ? "" : right) + str_other_v
		        + ")";
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof StringMultipleExpression) {
			StringMultipleExpression other = (StringMultipleExpression) obj;

			boolean other_v_eq = true;

			if (other.other_v.size() == this.other_v.size()) {
				for (int i = 0; i < other.other_v.size(); i++) {
					if (!(other.other_v.get(i).equals(this.other_v.get(i)))) {
						other_v_eq = false;
					}
				}
			} else {
				other_v_eq = false;
			}

			return this.op.equals(other.op) && this.getSize() == other.getSize()
			        && this.left.equals(other.left) && this.right.equals(other.right)
			        && other_v_eq;
		}

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public int getSize() {
	    if (size == 0 && other_v!=null) {
	        int other_size = 0;
	        for (int i = 0; i < other_v.size(); i++) {
	            other_size += other_v.get(i).getSize();   
	        }
	        size = 1 + left.getSize() + right.getSize() + other_size;
	    }
	    return size;
	}
	
	//protected int size = 0;

	//@Override
	//public int getSize() {
	//	if (size == 0) {
	//		size = 1 + left.getSize() + right.getSize();
	//	}
	//	return size;
	//}

	/** {@inheritDoc} */
	@Override
	public String execute() {
		String first = (String) left.execute();
		long secLong, thrdLong;
		String secStr, thrdStr;

		switch (op) {

		case INDEXOFCI:
			secLong = ExpressionHelper.getLongResult(right);
			thrdLong = ExpressionHelper.getLongResult(other_v.get(0));
			return Integer.toString(first.indexOf((int) secLong, (int) thrdLong));
		case INDEXOFSI:
			secStr = (String) right.execute();
			thrdLong = ExpressionHelper.getLongResult(other_v.get(0));
			return Integer.toString(first.indexOf(secStr, (int) thrdLong));
		case LASTINDEXOFCI:
			secLong = ExpressionHelper.getLongResult(right);
			thrdLong = ExpressionHelper.getLongResult(other_v.get(0));
			return Integer.toString(first.lastIndexOf((int) secLong, (int) thrdLong));
		case LASTINDEXOFSI:
			secStr = (String) right.execute();
			thrdLong = ExpressionHelper.getLongResult(other_v.get(0));
			return Integer.toString(first.lastIndexOf(secStr, (int) thrdLong));
		case SUBSTRING:
			secLong = ExpressionHelper.getLongResult(right);
			thrdLong = ExpressionHelper.getLongResult(other_v.get(0));
			return first.substring((int) secLong, (int) thrdLong);
		case REPLACEC:
			secLong = ExpressionHelper.getLongResult(right);
			thrdLong = ExpressionHelper.getLongResult(other_v.get(0));
			return first.replace((char) secLong, (char) thrdLong);
		case REPLACECS:
			secStr = (String) right.execute();
			thrdStr = (String) other_v.get(0).execute();
			return first.replace(secStr, thrdStr);
		case REPLACEALL:
			secStr = (String) right.execute();
			thrdStr = (String) other_v.get(0).execute();
			return first.replaceAll(secStr, thrdStr);
		case REPLACEFIRST:
			secStr = (String) right.execute();
			thrdStr = (String) other_v.get(0).execute();
			return first.replaceFirst(secStr, thrdStr);
		default:
			log.warning("StringBinaryExpression: unimplemented operator!");
			return null;
		}
	}

}
