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
package org.evosuite.symbolic.expr.str;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.Variable;

/**
 * <p>
 * StringConstant class.
 * </p>
 * 
 * @author krusev
 */
public final class StringConstant extends AbstractExpression<String> implements
        StringValue {

	private static final long serialVersionUID = 6785078290753992374L;

	/**
	 * <p>
	 * Constructor for StringConstant.
	 * </p>
	 * 
	 * @param StringValue
	 *            a {@link java.lang.String} object.
	 */
	public StringConstant(String stringValue) {
		super(stringValue, 1, false);

	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.concreteValue;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StringConstant) {
			StringConstant v = (StringConstant) obj;
			return this.concreteValue.equals(v.concreteValue);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.concreteValue.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public String execute() {
		return this.concreteValue;

	}

	@Override
	public Set<Variable<?>> getVariables() {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		return variables;
	}

}
