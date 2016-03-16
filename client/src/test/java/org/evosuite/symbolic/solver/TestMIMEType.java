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
package org.evosuite.symbolic.solver;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.Collection;

import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.testcase.DefaultTestCase;

import com.examples.with.different.packagename.concolic.MIMETypeTest;

public class TestMIMEType extends TestSolver {

	private static DefaultTestCase buildMIMETypeTest() throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		Method method = MIMETypeTest.class.getMethod("test");
		tc.appendMethod(null, method);
		return tc.getDefaultTestCase();
	}

	public static void testMIMEType(Solver solver)
			throws SecurityException, NoSuchMethodException, SolverTimeoutException {

		DefaultTestCase tc = buildMIMETypeTest();
		Collection<Constraint<?>> constraints = DefaultTestCaseConcolicExecutor.execute(tc);
		assertNotNull(constraints);
	}
}
