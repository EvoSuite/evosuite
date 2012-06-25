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
package org.evosuite.symbolic.expr;

public class RealVariable extends RealExpression implements Variable<Double>{
	private static final long serialVersionUID = 1L;
	
	protected String name;
	protected double concreteValue;
	protected double minValue;
	protected double maxValue;
	
	public RealVariable(String name, double conV, double minValue, double maxValue) {
		super();
		this.name = name;
		this.concreteValue = conV;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	@Override
	public Double getConcreteValue() {
		return concreteValue;
	}


	public void setConcreteValue(double conV) {
		this.concreteValue = conV;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Double getMinValue() {
		return minValue;
	}

	@Override
	public Double getMaxValue() {
		return maxValue;
	}

	@Override
	public String toString() {
		return this.name + "(" + concreteValue + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RealVariable)
		{
			RealVariable v=(RealVariable) obj;
			return this.getName().equals(v.getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		if(hash==0)
		{
			hash=this.name.hashCode();
		}
		return hash;
	}

	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public Double execute() {
		return concreteValue;
	}
	
	
	
}
