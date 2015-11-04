package org.evosuite.symbolic.solver.z3str2;

import org.evosuite.Properties;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class TestZ3Str2 {

	private static final String DEFAULT_Z3_STR_PATH = Properties.Z3_STR2_PATH;

	@BeforeClass
	public static void configureZ3StrPath() {
		String z3StrPath = System.getenv("z3_str2_path");
		if (z3StrPath != null) {
			Properties.Z3_STR2_PATH = z3StrPath;
		}
	}

	@AfterClass
	public static void restoreZ3StrPath() {
		Properties.Z3_STR2_PATH = DEFAULT_Z3_STR_PATH;
	}

	@Before
	public void checkZ3Str2() {
		Assume.assumeTrue(Properties.Z3_STR2_PATH!=null);
	}
}
