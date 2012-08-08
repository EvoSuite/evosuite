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

/**
 * <p>StringConstant class.</p>
 *
 * @author krusev
 */
public class StringConstant extends StringExpression {

	private static final long serialVersionUID = 6785078290753992374L;

	protected final String value;

	/**
	 * <p>Constructor for StringConstant.</p>
	 *
	 * @param StringValue a {@link java.lang.String} object.
	 */
	public StringConstant(String StringValue) {
		this.value = StringValue;
		this.containsSymbolicVariable = false;
	}

	/** {@inheritDoc} */
	@Override
	public String getConcreteValue() {
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StringConstant) {
			StringConstant v = (StringConstant) obj;
			return this.value.equals(v.value);
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
	public String execute() {
		return value;
		
	}

}
