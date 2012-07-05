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
 * <p>StringVariable class.</p>
 *
 * @author krusev
 */
public class StringVariable extends StringExpression implements Variable<String> {

	private static final long serialVersionUID = 5925030390824261492L;

	protected String name;

	protected String minValue;
	
	protected String concValue;

	protected String maxValue;

	/**
	 * <p>Constructor for StringVariable.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @param concVal a {@link java.lang.String} object.
	 * @param minValue a {@link java.lang.String} object.
	 * @param maxValue a {@link java.lang.String} object.
	 */
	public StringVariable(String name, String concVal, String minValue, String maxValue) {
		super();
		this.name = name;
		this.concValue = concVal;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}	

	
	/** {@inheritDoc} */
	@Override
	public String getConcreteValue() {
		return concValue;
	}
	

	/**
	 * <p>setConcreteValue</p>
	 *
	 * @param concValue the concValue to set
	 */
	public void setConcreteValue(String concValue) {
		this.concValue = concValue;
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
	 * <p>Setter for the field <code>maxValue</code>.</p>
	 *
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(String maxValue) {
		this.maxValue = maxValue;
	}
	
	/*
	 * store the working value here
	 */
	/** {@inheritDoc} */
	@Override
	public String getMinValue() {
		return minValue;
	}

	/**
	 * <p>Setter for the field <code>minValue</code>.</p>
	 *
	 * @param minValue the minValue to set
	 */
	public void setMinValue(String minValue) {
		this.minValue = minValue;
	}
	
	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name + "(" + minValue + ")";
	}
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StringVariable) {
			StringVariable v = (StringVariable) obj;
			return this.getName().equals(v.getName());
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		if (hash == 0) {
			hash = this.name.hashCode();
		}
		return hash;
	}
	
	/** {@inheritDoc} */
	@Override
	public int getSize() {
		return 1;
	}

	/** {@inheritDoc} */
	@Override
	public String execute() {
		return minValue;
		
	}

}
