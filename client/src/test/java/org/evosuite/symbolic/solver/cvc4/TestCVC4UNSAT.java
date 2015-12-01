package org.evosuite.symbolic.solver.cvc4;

import java.io.IOException;

import org.evosuite.symbolic.solver.SolverEmptyQueryException;
import org.evosuite.symbolic.solver.SolverErrorException;
import org.evosuite.symbolic.solver.SolverParseException;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverUNSAT;
import org.junit.Test;

public class TestCVC4UNSAT extends TestCVC4 {

	@Test
	public void testUNSAT() throws SolverTimeoutException, IOException, SolverParseException, SolverEmptyQueryException,
			SolverErrorException {
		CVC4Solver solver = new CVC4Solver();
		TestSolverUNSAT.testUNSAT(solver);
	}
}
