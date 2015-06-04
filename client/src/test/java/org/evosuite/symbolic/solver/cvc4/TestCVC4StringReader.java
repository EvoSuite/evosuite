package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverReader;
import org.junit.Test;

public class TestCVC4StringReader {

	@Test
	public void testStringReader() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		CVC4Solver solver = new CVC4Solver();
		TestSolverReader.testStringReader(solver);
	}

}
