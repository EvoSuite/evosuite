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

/**
 * Assertion on comparison value of two objects 
 * 
 * @author Gordon Fraser
 * 
 */
public class CompareAssertion extends Assertion {

	public VariableReference dest;
	
	/**
	 * Create a copy of the compare assertion 
	 */
	@Override
	public Assertion clone() {
		CompareAssertion s = new CompareAssertion();
		s.source = source.clone();
		s.dest   = dest.clone();
		s.value  = value;
		return s;
	}

	/**
	 * This method returns the Java Code 
	 */
	@Override
	public String getCode() {
		return "assertEquals(var"+source.statement+".compareTo(var"+dest.statement+"), "+(Integer)value+")";
	}

	/**
	 * Determine if assertion holds in current scope
	 * @param scope
	 *        The scope of the test case execution 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean evaluate(Scope scope) {
		Comparable<Object> comparable = (Comparable<Object>)scope.get(source);
		return comparable.compareTo(scope.get(dest)) == (Integer)value;
	}

}
