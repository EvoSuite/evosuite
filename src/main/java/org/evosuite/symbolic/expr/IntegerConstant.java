
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
public class IntegerConstant extends IntegerExpression {

	private static final long serialVersionUID = 3770747666367222441L;

	protected long value;

	/**
	 * <p>Constructor for IntegerConstant.</p>
	 *
	 * @param longValue a long.
	 */
	public IntegerConstant(long longValue) {
		this.value = longValue;
		this.containsSymbolicVariable = false;
	}

	/** {@inheritDoc} */
	@Override
	public Long getConcreteValue() {
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return Long.toString(value);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IntegerConstant) {
			IntegerConstant v = (IntegerConstant) obj;
			return this.value == v.value;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public int getSize() {
		return 1;
	}

	/** {@inheritDoc} */
	@Override
	public Long execute() {
		return value;
	}

}
