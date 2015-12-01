package org.evosuite.symbolic.solver.z3;

import java.io.IOException;

import org.evosuite.symbolic.solver.SolverEmptyQueryException;
import org.evosuite.symbolic.solver.SolverErrorException;
import org.evosuite.symbolic.solver.SolverParseException;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverUNSAT;
import org.junit.Test;

public class TestZ3UNSAT extends TestZ3 {

	@Test
	public void testUNSAT() throws SolverTimeoutException, IOException, SolverParseException, SolverEmptyQueryException,
			SolverErrorException {
		Z3Solver solver = new Z3Solver();
		TestSolverUNSAT.testUNSAT(solver);
	}
}
