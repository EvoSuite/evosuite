package org.evosuite.junit.examples;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;

public class JUnit3Suite extends TestSuite {

	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(JUnit3Test.class);
	}
}
