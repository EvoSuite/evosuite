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

public class ExceptionAssertion extends Assertion {

	Throwable exception = null;
	
	int statement = 0;
	
	public ExceptionAssertion(Throwable exception, int statement) {
		this.exception = exception;
		this.statement = statement;
	}

	@Override
	public Assertion clone() {
		return new ExceptionAssertion(exception, statement);
	}

	@Override
	public boolean evaluate(Scope scope) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean evaluate(Throwable t) {
		return (exception.getMessage() != null && !exception.getMessage().equals(t.getMessage())); 
	}
	
	@Override
	public String getCode() {
		return "// Expecting exception: "+exception.getMessage();
	}

}
