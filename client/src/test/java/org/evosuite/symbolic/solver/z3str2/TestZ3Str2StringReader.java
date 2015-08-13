package org.evosuite.symbolic.solver.z3str2;

import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverReader;
import org.evosuite.symbolic.solver.z3str2.Z3Str2Solver;
import org.junit.Test;

public class TestZ3Str2StringReader {

	@Test
	public void testStringReader() throws SecurityException,
			NoSuchMethodException, SolverTimeoutException {
		Z3Str2Solver solver = new Z3Str2Solver();
		TestSolverReader.testStringReader(solver);
	}

}
