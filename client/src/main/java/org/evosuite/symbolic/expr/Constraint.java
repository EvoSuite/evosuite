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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class Constraint<T extends Object> implements Serializable {

	private static final long serialVersionUID = 7547747352755232472L;

	/**
	 * <p>
	 * getComparator
	 * </p>
	 * 
	 * @param <T>
	 *            a T object.
	 * @return a {@link org.evosuite.symbolic.expr.Comparator} object.
	 */
	abstract public Comparator getComparator();

	/**
	 * <p>
	 * getLeftOperand
	 * </p>
	 * 
	 * @return a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	abstract public Expression<?> getLeftOperand();

	/**
	 * <p>
	 * getRightOperand
	 * </p>
	 * 
	 * @return a {@link org.evosuite.symbolic.expr.Expression} object.
	 */
	abstract public Expression<?> getRightOperand();

	private int hash = 0;

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		if (hash != 0) {

		} else {
			hash = getLeftOperand().hashCode() + getComparator().hashCode()
					+ getRightOperand().hashCode();
		}
		return hash;
	}

	protected int size = 0;

	/**
	 * <p>
	 * Getter for the field <code>size</code>.
	 * </p>
	 * 
	 * @return a int.
	 */
	public int getSize() {
		if (size == 0) {
			size = 1 + getLeftOperand().getSize() + getRightOperand().getSize();
		}
		return size;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Constraint<?>)) {
			return false;
		}

		Constraint<?> other = (Constraint<?>) obj;
		if (this.getComparator().equals(other.getComparator())
				// && this.getSize() == other.getSize()
				&& this.getLeftOperand().equals(other.getLeftOperand())
				&& this.getRightOperand().equals(other.getRightOperand())) {
			return true;
		}
		return false;
	}

	/**
	 * Sound but not complete
	 * 
	 * @return a boolean.
	 */
	public boolean isSolveable() {
		if (getLeftOperand().equals(getRightOperand())) {
			if (getComparator() == Comparator.LT
					|| getComparator() == Comparator.GT
					|| getComparator() == Comparator.NE) {
				return false;
			}
		}
		return true;
	}

	public abstract Constraint<T> negate();

	/**
	 * Returns x/(x+1)
	 * 
	 * @param x
	 * @return a normalized double value
	 */
	protected static double normalize(double x) {
		return x / (x + 1.0);
	}

	public Set<Variable<?>> getVariables() {
		Set<Variable<?>> result = new HashSet<Variable<?>>();
		result.addAll(this.getLeftOperand().getVariables());
		result.addAll(this.getRightOperand().getVariables());
		return result;
	}

	public Set<Object> getConstants() {
		Set<Object> result = new HashSet<Object>();
		result.addAll(this.getLeftOperand().getConstants());
		result.addAll(this.getRightOperand().getConstants());
		return result;
	}

	public abstract <K, V> K accept(ConstraintVisitor<K, V> v, V arg);
}
