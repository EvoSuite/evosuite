package org.evosuite.symbolic.z3;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverReader;
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
