
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
public class RealComparison extends RealExpression {
	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for RealComparison.</p>
	 *
	 * @param left a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param right a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param con a {@link java.lang.Long} object.
	 */
	public RealComparison(Expression<Double> left, Expression<Double> right,
			Long con) {
		super();
		this.left = left;
		this.right = right;
		this.con = con;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	private Long con;
	private Expression<Double> left;
	private Expression<Double> right;
	
	/** {@inheritDoc} */
	@Override
	public Long getConcreteValue() {
		return con;
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if(obj==this)
		{
			return true;
		}
		if(obj instanceof RealComparison)
		{
			RealComparison other=(RealComparison) obj;
			return  this.con.equals(other.con) 
					&& this.getSize()==other.getSize() 
					&& this.left.equals(other.left) && this.right.equals(other.right);
		}

		return false;
	}

	/**
	 * <p>getRightOperant</p>
	 *
	 * @return a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	public Expression<Double> getRightOperant() {
		return right;
	}

	/**
	 * <p>getLeftOperant</p>
	 *
	 * @return a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	public Expression<Double> getLeftOperant() {
		return left;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "("+left+" cmp "+right+")";
	}
	
	protected int size=0;
	/** {@inheritDoc} */
	@Override
	public int getSize() {
		if(size==0)
		{
			size=1+left.getSize()+right.getSize();
		}
		return size;
	}

	/** {@inheritDoc} */
	@Override
	public Double execute() {
		// this is never used
		return null;
	}
}
