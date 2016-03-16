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
/**
 * 
 */
package org.evosuite.symbolic.expr.str;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.symbolic.expr.AbstractExpression;
import org.evosuite.symbolic.expr.ExpressionVisitor;
import org.evosuite.symbolic.expr.Variable;

/**
 * <p>
 * StringVariable class.
 * </p>
 * 
 * @author krusev
 */
public final class StringVariable extends AbstractExpression<String> implements
        StringValue, Variable<String> {

	private static final long serialVersionUID = 5925030390824261492L;

	private final String name;

	private String maxValue;

	/**
	 * <p>
	 * Constructor for StringVariable.
	 * </p>
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @param concVal
	 *            a {@link java.lang.String} object.
	 * @param minValue
	 *            a {@link java.lang.String} object.
	 * @param maxValue
	 *            a {@link java.lang.String} object.
	 */
	public StringVariable(String name, String concVal) {
		super(concVal, 1, true);
		this.name = name;
		this.maxValue = concVal;
	}

	/**
	 * <p>
	 * setConcreteValue
	 * </p>
	 * 
	 * @param concValue
	 *            the concValue to set
	 */
	public void setConcreteValue(String concValue) {
		this.concreteValue = concValue;
	}

	/*
	 * store the better value here
	 */
	/** {@inheritDoc} */
	@Override
	public String getMaxValue() {
		return maxValue;
	}

	/**
	 * <p>
	 * Setter for the field <code>maxValue</code>.
	 * </p>
	 * 
	 * @param maxValue
	 *            the maxValue to set
	 */
	public void setMaxValue(String maxValue) {
		this.maxValue = maxValue;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	private static boolean isAsciiPrintable(char ch) {
		return ch >= 32 && ch < 127;
	}

	private String removeNonAsciiPrintable(String string) {
		StringBuffer bf = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			char charAt = string.charAt(i);
			if (isAsciiPrintable(charAt)) {
				bf.append(charAt);
			}
		}
		return bf.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		String asciiPrintableString = removeNonAsciiPrintable(concreteValue);
		return name + "(\"" + asciiPrintableString.replace("\n", "").replace(" ", "")
		        + "\")";
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof StringVariable) {
			StringVariable v = (StringVariable) obj;
			return this.getName().equals(v.getName());
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public Set<Variable<?>> getVariables() {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		variables.add(this);
		return variables;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.symbolic.expr.AbstractExpression#getConstants()
	 */
	@Override
	public Set<Object> getConstants() {
		// Do not include original values?
		return new HashSet<Object>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.symbolic.expr.Variable#getMinValue()
	 */
	@Override
	public String getMinValue() {
		return concreteValue;
	}

	
	@Override
	public <K, V> K accept(ExpressionVisitor<K, V> v, V arg) {
		return v.visit(this, arg);
	}

}
