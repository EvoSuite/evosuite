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
 * @author krusev
 *
 */
public class StringVariable extends StringExpression implements Variable<String> {

	private static final long serialVersionUID = 5925030390824261492L;

	protected String name;

	protected String minValue;
	
	protected String concValue;

	protected String maxValue;

	public StringVariable(String name, String concVal, String minValue, String maxValue) {
		super();
		this.name = name;
		this.concValue = concVal;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}	

	
	/**
	 * @return the concValue
	 */
	@Override
	public String getConcreteValue() {
		return concValue;
	}
	

	/**
	 * @param concValue the concValue to set
	 */
	public void setConcreteValue(String concValue) {
		this.concValue = concValue;
	}
	
	/*
	 * store the better value here
	 */
	@Override
	public String getMaxValue() {
		return maxValue;
	}

	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(String maxValue) {
		this.maxValue = maxValue;
	}
	
	/*
	 * store the working value here
	 */
	@Override
	public String getMinValue() {
		return minValue;
	}

	/**
	 * @param minValue the minValue to set
	 */
	public void setMinValue(String minValue) {
		this.minValue = minValue;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name + "(" + minValue + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StringVariable) {
			StringVariable v = (StringVariable) obj;
			return this.getName().equals(v.getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (hash == 0) {
			hash = this.name.hashCode();
		}
		return hash;
	}
	
	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public String execute() {
		return minValue;
		
	}

}
