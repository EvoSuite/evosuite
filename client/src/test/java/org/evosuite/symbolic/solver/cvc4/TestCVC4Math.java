/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverMath;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestCVC4Math {

	private static final String DEFAULT_CVC4_PATH = Properties.CVC4_PATH;

	@BeforeClass
	public static void configureCVC4Path() {
		String cvc4_path = System.getenv("cvc4_path");
		if (cvc4_path != null) {
			Properties.CVC4_PATH = cvc4_path;
		}
	}

	@AfterClass
	public static void restoreCVC4Path() {
		Properties.CVC4_PATH = DEFAULT_CVC4_PATH;
	}

	@Test
	public void testAbs() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverMath.testAbs(solver);
	}

	@Test
	public void testMax() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverMath.testMax(solver);
	}

	@Test
	public void testMin() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {

		if (Properties.CVC4_PATH == null) {
			System.out
					.println("Warning: cvc4_path should be configured to execute this test case");
			return;
		}

		CVC4Solver solver = new CVC4Solver();
		TestSolverMath.testMin(solver);
	}
}
