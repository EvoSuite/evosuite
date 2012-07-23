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
 * <p>StringConstraint class.</p>
 *
 * @author krusev
 */
public class StringConstraint extends Constraint<String> {

	private static final long serialVersionUID = 5871101668137509725L;

	/**
	 * <p>Constructor for StringConstraint.</p>
	 *
	 * @param left a {@link org.evosuite.symbolic.expr.StringExpression} object.
	 * @param cmp a {@link org.evosuite.symbolic.expr.Comparator} object.
	 * @param right a {@link org.evosuite.symbolic.expr.StringExpression} object.
	 */
	public StringConstraint(StringExpression left, Comparator cmp, StringExpression right) {
		super();
		this.left = left;
		this.cmp = cmp;
		this.right = right;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	protected Comparator cmp;

	protected Expression<String> left;
	protected Expression<String> right;

	/** {@inheritDoc} */
	@Override
	public Comparator getComparator() {
		return cmp;
	}

	/** {@inheritDoc} */
	@Override
	public Expression<String> getLeftOperand() {
		return left;
	}

	/** {@inheritDoc} */
	@Override
	public Expression<String> getRightOperand() {
		return right;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return left + cmp.toString() + right;
	}
}
