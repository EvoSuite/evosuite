/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.assertion;

import org.evosuite.Properties;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.MethodStatement;


/**
 * <p>UnitAssertionGenerator class.</p>
 *
 * @author Gordon Fraser
 */
public class UnitAssertionGenerator extends AssertionGenerator {

	private boolean isRelevant(Statement s, TestCase t) {
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
	 * @see org.evosuite.assertion.AssertionGenerator#addAssertions(org.evosuite.testcase.TestCase)
	 */
	/** {@inheritDoc} */
	@Override
	public void addAssertions(TestCase test) {
		ExecutionResult result = runTest(test);
		for (OutputTrace<?> trace : result.getTraces()) {
			trace.getAllAssertions(test);
		}

		for (int i = 0; i < test.size(); i++) {
			Statement s = test.getStatement(i);
			if (!isRelevant(s, test))
				s.removeAssertions();
		}
	}

}
