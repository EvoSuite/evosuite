/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
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
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.assertion;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Gordon Fraser
 * 
 */
public class UnitAssertionGenerator extends AssertionGenerator {

	private boolean isRelevant(StatementInterface s, TestCase t) {
		// Always allow assertions on the last statement
		if (s.getPosition() == (t.size() - 1))
			return true;

		// Allow assertions after method calls on the UUT
		if (s instanceof MethodStatement) {
			MethodStatement ms = (MethodStatement) s;
			String declaringClass = ms.getMethod().getDeclaringClass().getName();
			while (declaringClass.contains("$"))
				declaringClass = declaringClass.substring(0, declaringClass.indexOf("$"));

			if (declaringClass.equals(Properties.TARGET_CLASS) || (!Properties.TARGET_CLASS_PREFIX.isEmpty() && declaringClass.startsWith(Properties.TARGET_CLASS_PREFIX)))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.assertion.AssertionGenerator#addAssertions(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public void addAssertions(TestCase test) {
		ExecutionResult result = runTest(test);
		for (OutputTrace<?> trace : result.getTraces()) {
			trace.getAllAssertions(test);
		}

		for (int i = 0; i < test.size(); i++) {
			StatementInterface s = test.getStatement(i);
			if (!isRelevant(s, test))
				s.removeAssertions();
		}
	}

}
