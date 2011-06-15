/*
 * Copyright (C) 2010 Saarland University
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
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.assertion;

import java.util.HashSet;
import java.util.Set;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * Abstract class of an executable code assertion
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class Assertion {

	/** Variable on which the assertion is made */
	public VariableReference source;

	/** Expected value of variable */
	public Object value;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

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
	@Override
	public final Assertion clone(){
		throw new UnsupportedOperationException("Use Assertion.clone(TestCase)");
	}
	
	/**
	 * Return a copy of the assertion, which is valid in newTestCase
	 */
	public abstract Assertion clone(TestCase newTestCase);

	/**
	 * Determine if assertion holds in current scope
	 * 
	 * @param scope
	 *            The scope of the test case execution
	 */
	public abstract boolean evaluate(Scope scope);

	/**
	 * Return all the variables that are part of this assertion
	 * 
	 * @return
	 */
	public Set<VariableReference> getReferencedVariables() {
		Set<VariableReference> vars = new HashSet<VariableReference>();
		vars.add(source);
		return vars;
	}

}
