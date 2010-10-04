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

public class PrimitiveAssertion extends Assertion {

	@Override
	public String getCode() {
		if(value == null) {
			return "null";
		} else if(value.getClass().equals(Long.class)) {
			String val = value.toString();
			return "assertEquals(var"+source.statement+", "+val+"L)";
		}
		else
			return "assertEquals(var"+source.statement+", "+value+")";
	}

	@Override
	public Assertion clone() {
		PrimitiveAssertion s = new PrimitiveAssertion();
		s.source = source.clone();
		s.value  = value;
		return s;
	}

	@Override
	public boolean evaluate(Scope scope) {
		return scope.get(source).equals(value);
	}

}
