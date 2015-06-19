package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.ConstraintSolverTimeoutException;
import org.evosuite.symbolic.solver.TestSolverReader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestCVC4StringReader {

	private static final String DEFAULT_CVC4_PATH = Properties.CVC4_PATH;

	@Test
	public void testStringReader() throws SecurityException,
			NoSuchMethodException, ConstraintSolverTimeoutException {
		CVC4Solver solver = new CVC4Solver();
		TestSolverReader.testStringReader(solver);
	}

	@BeforeClass
	public static void configureCVC4Path() {
		String cvc4Path = System.getenv("cvc4_path");
		if (cvc4Path != null) {
			Properties.CVC4_PATH = cvc4Path;
		}
	}

	@AfterClass
	public static void restoreCVC4Path() {
		Properties.CVC4_PATH = DEFAULT_CVC4_PATH;
	}

}
