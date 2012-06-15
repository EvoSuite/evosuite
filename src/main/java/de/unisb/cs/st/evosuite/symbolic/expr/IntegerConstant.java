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
package de.unisb.cs.st.evosuite.symbolic.expr;

public class IntegerConstant extends IntegerExpression {

	private static final long serialVersionUID = 3770747666367222441L;

	protected long value;

	public IntegerConstant(long longValue) {
		this.value = longValue;
	}

	@Override
	public Long getConcreteValue() {
		return value;
	}

	@Override
	public String toString() {
		return Long.toString(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IntegerConstant) {
			IntegerConstant v = (IntegerConstant) obj;
			return this.value == v.value;
		}
		return false;
	}

	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public Long execute() {
		return value;
	}

}
