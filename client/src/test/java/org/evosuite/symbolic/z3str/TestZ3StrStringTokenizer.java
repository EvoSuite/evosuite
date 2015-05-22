package org.evosuite.symbolic.z3str;

import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverTokenizer;
import org.junit.Test;

public class TestZ3StrStringTokenizer {

	@Test
	public void testStringTokenizer() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverTokenizer.testStringTokenizer(solver);
	}

}
