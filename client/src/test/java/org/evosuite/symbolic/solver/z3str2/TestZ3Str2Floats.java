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
package org.evosuite.symbolic.solver.z3str2;

import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverFloats;
import org.junit.Test;

public class TestZ3Str2Floats extends TestZ3Str2 {

	@Test
	public void testFloatEq() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testEq(solver);
	}

	@Test
	public void testFloatNeq() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testNeq(solver);
	}

	@Test
	public void testFloatLt() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testLt(solver);
	}

	@Test
	public void testFloatGt() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testGt(solver);
	}

	@Test
	public void testFloatLte() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testLte(solver);
	}

	@Test
	public void testFloatGte() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testGte(solver);
	}

	@Test
	public void testFloatFraction() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testFraction(solver);
	}

	@Test
	public void testFloatAdd() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testAdd(solver);
	}

	@Test
	public void testFloatSub() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testSub(solver);
	}

	@Test
	public void testFloatMul() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testMul(solver);
	}

	@Test
	public void testFloatDiv() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testDiv(solver);
	}

	@Test
	public void testFloatMod() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverFloats.testMod(solver);
	}
}
