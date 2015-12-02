package org.evosuite.symbolic.solver.z3str2;

import java.io.IOException;

import org.evosuite.symbolic.solver.SolverEmptyQueryException;
import org.evosuite.symbolic.solver.SolverErrorException;
import org.evosuite.symbolic.solver.SolverParseException;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverUNSAT;
import org.junit.Test;

public class TestZ3Str2UNSAT extends TestZ3Str2 {
	@Test
	public void testUNSAT() throws SolverTimeoutException, IOException, SolverParseException, SolverEmptyQueryException,
			SolverErrorException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverUNSAT.testUNSAT(solver);
	}
}
