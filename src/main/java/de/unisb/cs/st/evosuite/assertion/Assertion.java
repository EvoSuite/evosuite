/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.unisb.cs.st.evosuite.assertion;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * Abstract class of an executable code assertion 
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class Assertion {
	
	/** Variable on which the assertion is made */
	VariableReference source;
	
	/** Expected value of variable */
	Object value;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Assertion other = (Assertion) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public VariableReference getSource() {
		return source;
	}

	/**
	 * This method returns the Java Code 
	 */
	public abstract String getCode();

	/**
	 * Return a copy of the assertion 
	 */
	public abstract Assertion clone();
	
	/**
	 * Determine if assertion holds in current scope
	 * @param scope
	 *        The scope of the test case execution 
	 */
	public abstract boolean evaluate(Scope scope);
		
}
