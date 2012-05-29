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

public class IntToStringCast extends StringExpression implements Cast<Long>{
	
	private static final long serialVersionUID = 2414222998301630838L;

	static Logger log = JPF.getLogger((IntToStringCast.class).toString());
	
	protected Expression<Long> intVar;

	public IntToStringCast(Expression<Long> expr) {
		this.intVar = expr;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}
	
	@Override
	public String execute() {
		return Long.toString((Long)intVar.execute());
	}

	@Override
	public String getConcreteValue() {
		return Long.toString((Long)intVar.getConcreteValue());
	}
	
	@Override
	public String toString() {
		return intVar.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof IntToStringCast) {
			IntToStringCast other = (IntToStringCast) obj;
			return this.intVar.equals(other.intVar);
		}

		return false;
	}
	
	protected int size=0;
	@Override
	public int getSize() {
		if(size == 0)
		{
			size=1 + intVar.getSize();
		}
		return size;
	}

	@Override
	public Expression<Long> getConcreteObject() {
		return intVar;
	}
}
