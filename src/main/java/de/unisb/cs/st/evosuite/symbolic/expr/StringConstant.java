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

/**
 * @author krusev
 *
 */
public class StringConstant extends StringExpression {

	private static final long serialVersionUID = 6785078290753992374L;

	protected String value;

	public StringConstant(String StringValue) {
		this.value = StringValue;
	}

	@Override
	public String getConcreteValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StringConstant) {
			StringConstant v = (StringConstant) obj;
			return this.value.equals(v.value);
		}
		return false;
	}

	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public String execute() {
		return value;
		
	}

}