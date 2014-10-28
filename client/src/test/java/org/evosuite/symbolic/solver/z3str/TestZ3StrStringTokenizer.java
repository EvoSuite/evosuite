package org.evosuite.symbolic.solver.z3str;

import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverTokenizer;
import org.evosuite.symbolic.solver.z3str.Z3StrSolver;
import org.junit.Test;

public class TestZ3StrStringTokenizer {

	@Test
	public void testStringTokenizer() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverTokenizer.testStringTokenizer(solver);
	}

}
