package org.evosuite.symbolic.solver.z3;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverReader;
import org.evosuite.symbolic.solver.z3.Z3Solver;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3StringReader {

	@Test
	public void testStringReader() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		Z3Solver solver = new Z3Solver();
		TestSolverReader.testStringReader(solver);
	}

	@BeforeClass 
	public static void setUpZ3Path() {
		Properties.Z3_PATH = System.getenv("Z3_PATH");
	}

}
