package org.evosuite.runtime;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.junit.Test;

import com.examples.with.different.packagename.CallExit;

public class TestSUTWithSystemExit extends SystemTest {

	@Test
	public void testSystemExit() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = CallExit.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.REPLACE_CALLS = true;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		evosuite.parseCommandLine(command);
	}

}
