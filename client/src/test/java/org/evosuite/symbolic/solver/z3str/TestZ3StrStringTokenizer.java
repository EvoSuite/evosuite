package org.evosuite.symbolic.solver.z3str;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverTokenizer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestZ3StrStringTokenizer {

	private static final String DEFAULT_Z3_STR_PATH = Properties.Z3_STR_PATH;

	@BeforeClass
	public static void configureZ3StrPath() {
		String z3StrPath = System.getenv("z3_str_path");
		if (z3StrPath != null) {
			Properties.Z3_STR_PATH = z3StrPath;
		}
	}

	@AfterClass
	public static void restoreZ3StrPath() {
		Properties.Z3_STR_PATH = DEFAULT_Z3_STR_PATH;
	}
	
	@Test
	public void testStringTokenizer() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		
		if (Properties.Z3_STR_PATH == null) {
			System.out
					.println("Warning: z3_str_path should be configured to execute this test case");
			return;
		}

		
		Z3StrSolver solver = new Z3StrSolver();
		TestSolverTokenizer.testStringTokenizer(solver);
	}

}
