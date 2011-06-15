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

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;

public class AssertionObserver extends ExecutionObserver {

	List<Boolean> status = new ArrayList<Boolean>();
	TestCase current_test;

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	public List<Boolean> getStatus() {
		return status;
	}

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	public void setTest(TestCase test) {
		current_test = test;
		status = new ArrayList<Boolean>();
	}

	@Override
	public void statement(StatementInterface statement, Scope scope, Throwable exception) {
		for (Assertion a : statement.getAssertions()) {
			status.add(a.evaluate(scope));
		}
	}
}
