/*
 * Copyright (C) 2009 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Javalanche is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with Javalanche.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

public class EqualsAssertion extends Assertion {

	public VariableReference dest;
	
	@Override
	public Assertion clone() {
		EqualsAssertion s = new EqualsAssertion();
		s.source = source.clone();
		s.dest   = dest.clone();
		s.value  = value;
		return s;
	}

	@Override
	public String getCode() {
		if(((Boolean)value).booleanValue())
			return "assertTrue(var"+source.statement+".equals(var"+dest.statement+"))";
		else
			return "assertFalse(var"+source.statement+".equals(var"+dest.statement+"))";
	}

	@Override
	public boolean evaluate(Scope scope) {
		return scope.get(source).equals(scope.get(dest));
	}

}
