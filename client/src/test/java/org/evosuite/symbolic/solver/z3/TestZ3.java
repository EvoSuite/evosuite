package org.evosuite.symbolic.solver.z3;

import org.evosuite.Properties;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class TestZ3 {

	private static final String DEFAULT_Z3_PATH = Properties.Z3_PATH;

	@BeforeClass
	public static void configureZ3Path() {
		String z3StrPath = System.getenv("z3_path");
		if (z3StrPath != null) {
			Properties.Z3_PATH = z3StrPath;
		}
	}

	@AfterClass
	public static void restoreZ3Path() {
		Properties.Z3_PATH = DEFAULT_Z3_PATH;
	}

	@Before
	public void checkZ3() {
		Assume.assumeTrue(Properties.Z3_PATH!=null);
	}
}
