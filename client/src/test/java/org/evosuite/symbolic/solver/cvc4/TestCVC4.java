package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.Properties;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class TestCVC4 {

	private static final String DEFAULT_CVC4_PATH = Properties.CVC4_PATH;

	@BeforeClass
	public static void configureCVC4Path() {
		String cvc4_path = System.getenv("cvc4_path");
		if (cvc4_path != null) {
			Properties.CVC4_PATH = cvc4_path;
		}
	}
	
	@Before
	public void checkCVC4() {
		Assume.assumeTrue(Properties.CVC4_PATH!=null);
	}

	@AfterClass
	public static void restoreCVC4Path() {
		Properties.CVC4_PATH = DEFAULT_CVC4_PATH;
	}

}
