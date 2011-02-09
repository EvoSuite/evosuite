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
import de.unisb.cs.st.evosuite.testcase.VariableReference;

public class EqualsAssertion extends Assertion {

	public VariableReference dest;

	@Override
	public Assertion clone() {
		EqualsAssertion s = new EqualsAssertion();
		s.source = source.clone();
		s.dest = dest.clone();
		s.value = value;
		return s;
	}

	@Override
	public String getCode() {
		if (((Boolean) value).booleanValue())
			return "assertTrue(" + source.getName() + ".equals("
			        + dest.getName() + "));";
		else
			return "assertFalse(" + source.getName() + ".equals("
			        + dest.getName() + "));";
	}

	@Override
	public boolean evaluate(Scope scope) {
		if (((Boolean) value).booleanValue()) {
			if (scope.get(source) == null)
				return scope.get(dest) == null;
			else
				return scope.get(source).equals(scope.get(dest));
		} else {
			if (scope.get(source) == null)
				return scope.get(dest) != null;
			else
				return !scope.get(source).equals(scope.get(dest));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((dest == null) ? 0 : dest.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EqualsAssertion other = (EqualsAssertion) obj;
		if (dest == null) {
			if (other.dest != null)
				return false;
		} else if (!dest.equals(other.dest))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.evosuite.assertion.Assertion#getReferencedVariables()
	 */
	@Override
	public Set<VariableReference> getReferencedVariables() {
		Set<VariableReference> vars = new HashSet<VariableReference>();
		vars.add(source);
		vars.add(dest);
		return vars;
	}
}
