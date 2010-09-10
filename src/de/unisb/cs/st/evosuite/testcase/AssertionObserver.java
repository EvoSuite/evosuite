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

import java.util.ArrayList;
import java.util.List;

public class AssertionObserver extends ExecutionObserver {

	List<Boolean> status = new ArrayList<Boolean>();
	TestCase current_test;
	
	public void setTest(TestCase test) {
		current_test = test;
		status = new ArrayList<Boolean>();
	}
	
	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void statement(int position, Scope scope, VariableReference retval) {
		for(Assertion a : current_test.statements.get(position).assertions) {
			status.add(a.evaluate(scope));
		}
	}

	public List<Boolean> getStatus() {
		return status;
	}
}
