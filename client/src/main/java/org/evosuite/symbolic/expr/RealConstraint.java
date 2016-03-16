/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.expr;

import org.evosuite.Properties;
import org.evosuite.symbolic.ConstraintTooLongException;
import org.evosuite.symbolic.DSEStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RealConstraint extends Constraint<Double> {

	static Logger log = LoggerFactory.getLogger(RealConstraint.class);

	private static final long serialVersionUID = 6021027178547577289L;

	/**
	 * <p>
	 * Constructor for RealConstraint.
	 * </p>
	 * 
	 * @param left
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 * @param cmp
	 *            a {@link org.evosuite.symbolic.expr.Comparator} object.
	 * @param right
	 *            a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	public RealConstraint(Expression<Double> left, Comparator cmp,
			Expression<Double> right) {
		super();
		this.left = left;
		this.cmp = cmp;
		this.right = right;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH) {
			DSEStats.getInstance().reportConstraintTooLong(getSize());
			throw new ConstraintTooLongException(getSize());
		}
	}

	private final Expression<Double> left;
	private final Comparator cmp;
	private final Expression<Double> right;

	/** {@inheritDoc} */
	@Override
	public Comparator getComparator() {
		return cmp;
	}

	/** {@inheritDoc} */
	@Override
	public Expression<Double> getLeftOperand() {
		return left;
	}

	/** {@inheritDoc} */
	@Override
	public Expression<Double> getRightOperand() {
		return right;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return left + cmp.toString() + right;
	}

	@Override
	public Constraint<Double> negate() {
		return new RealConstraint(left, cmp.not(), right);
	}

	@Override
	public <K, V> K accept(ConstraintVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}

}
