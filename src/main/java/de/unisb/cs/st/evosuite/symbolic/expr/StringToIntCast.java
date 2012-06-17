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
package de.unisb.cs.st.evosuite.symbolic.expr;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.symbolic.ConstraintTooLongException;

/**
 * @author krusev
 * 
 */
public class StringToIntCast extends IntegerExpression implements Cast<String> {

	private static final long serialVersionUID = 2214987345674527740L;

	protected Long concValue;

	protected Expression<String> expr;

	public StringToIntCast(Expression<String> _expr, Long _concValue) {
		this.expr = _expr;
		this.concValue = _concValue;
		if (getSize() > Properties.DSE_CONSTRAINT_LENGTH)
			throw new ConstraintTooLongException();
	}

	@Override
	public Object getConcreteValue() {
		return concValue;
	}

	@Override
	public Expression<String> getConcreteObject() {
		return expr;
	}

	@Override
	public String toString() {
		return "((INT)" + expr + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof StringToIntCast) {
			StringToIntCast other = (StringToIntCast) obj;
			return this.expr.equals(other.expr) && this.concValue.equals(other.concValue);
		}

		return false;
	}

	protected int size = 0;

	@Override
	public int getSize() {
		if (size == 0) {
			size = 1 + expr.getSize();
		}
		return size;
	}

	@Override
	public Long execute() {
		return Long.parseLong(((String) expr.execute()));
	}

}
