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
			DSEStats.reportConstraintTooLong(getSize());
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

	public double getRealDist() {
		double left = (Double) this.getLeftOperand().execute();
		double right = (Double) this.getRightOperand().execute();

		Comparator cmpr = this.getComparator();

		switch (cmpr) {

		case EQ:

			return Math.abs(left - right);
		case NE:

			return (left - right) != 0 ? 0 : 1;
		case LT:

			return left - right < 0 ? 0 : left - right + 1;
		case LE:

			return left - right <= 0 ? 0 : left - right;
		case GT:

			return left - right > 0 ? 0 : right - left + 1;
		case GE:

			return left - right >= 0 ? 0 : right - left;

		default:
			log.warn("getIntegerDist: unimplemented comparator");
			return Double.MAX_VALUE;
		}
	}

}
