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
package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverBitwise;
import org.junit.Test;

public class TestCVC4Bitwise extends TestCVC4 {


	@Test
	public void testBitAnd() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		CVC4Solver solver = new CVC4Solver();
		TestSolverBitwise.testBitAnd(solver);
	}

	@Test
	public void testBitNot() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		CVC4Solver solver = new CVC4Solver();
		TestSolverBitwise.testBitNot(solver);
	}

	@Test
	public void testBitOr() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		CVC4Solver solver = new CVC4Solver();
		TestSolverBitwise.testBitOr(solver);
	}

	@Test
	public void testBitXor() throws SecurityException, NoSuchMethodException,
			SolverTimeoutException {
		CVC4Solver solver = new CVC4Solver();
		TestSolverBitwise.testBitXor(solver);
	}

	@Test
	public void testShiftLeft() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		CVC4Solver solver = new CVC4Solver();
		TestSolverBitwise.testShiftLeft(solver);
	}

	@Test
	public void testShiftRight() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		CVC4Solver solver = new CVC4Solver();
		TestSolverBitwise.testShiftRight(solver);
	}

	@Test
	public void testShiftRightUnsigned() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		CVC4Solver solver = new CVC4Solver();
		TestSolverBitwise.testShiftRightUnsigned(solver);
	}
}
