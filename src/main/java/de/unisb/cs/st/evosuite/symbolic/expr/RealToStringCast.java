/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
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

import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.symbolic.ConstraintTooLongException;

import gov.nasa.jpf.JPF;

public class RealToStringCast extends StringExpression implements Cast<Double>{

	private static final long serialVersionUID = -5322228289539145088L;

	static Logger log = JPF.getLogger((RealToStringCast.class).toString());
	
	protected Expression<Double> expr;

	public RealToStringCast(Expression<Double> _expr) {
		this.expr = _expr;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}
	
	@Override
	public String execute() {
		return Double.toString((Double)expr.execute());
	}

	@Override
	public String getConcreteValue() {
		return Double.toString((Double)expr.getConcreteValue());
	}
	
	@Override
	public String toString() {
		return "(String)" + expr.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof RealToStringCast) {
			RealToStringCast other = (RealToStringCast) obj;
			return this.expr.equals(other.expr)
				&& this.getSize() == other.getSize();
		}

		return false;
	}
	
	protected int size=0;
	@Override
	public int getSize() {
		if(size == 0)
		{
			size=1 + expr.getSize();
		}
		return size;
	}

	@Override
	public Expression<Double> getConcreteObject() {
		return expr;
	}
}
