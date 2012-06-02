/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.symbolic.expr;

public class RealConstant extends RealExpression{
	private static final long serialVersionUID = 1L;
	
	protected double value;

	public RealConstant(double doubleValue) {
		this.value=doubleValue;
	}

	@Override
	public Double getConcreteValue() {
		return value;
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RealConstant)
		{
			RealConstant v=(RealConstant) obj;
			return this.value==v.value;
		}
		return false;
	}

	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public Double execute() {
		return value;
	}
}
