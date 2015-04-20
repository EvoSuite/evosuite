package org.evosuite.junit.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.junit.FooTestClassLoader;
import org.evosuite.junit.JUnitExecutionException;
import org.evosuite.junit.JUnitResult;
import org.junit.After;
import org.junit.Test;

import com.examples.with.different.packagename.junit.PassingFooTest;

public class JUnitProcessLauncherTest {

	@Test
	public void testCorrectLaunch() throws JUnitExecutionException {

		JUnitProcessLauncher launcher = new JUnitProcessLauncher();
		JUnitResult result = launcher.startNewJUnitProcess(
				new Class<?>[] { PassingFooTest.class }, null);

		assertTrue(result.wasSuccessful());
		assertEquals(0, result.getFailureCount());
		assertEquals(3, result.getRunCount());
	}

	@Test
	public void testMissingClassFile() {

		FooTestClassLoader loader = new FooTestClassLoader();
		Class<?> fooTestClass = loader.loadFooTestClass();
		JUnitProcessLauncher launcher = new JUnitProcessLauncher();
		try {
			launcher.startNewJUnitProcess(new Class<?>[] { fooTestClass }, null);
			fail();
		} catch (JUnitExecutionException e) {
		}
	}

	@Test
	public void testMissingArguments() {

		JUnitProcessLauncher launcher = new JUnitProcessLauncher();
		try {
			launcher.startNewJUnitProcess(new Class<?>[] {}, null);
			fail();
		} catch (JUnitExecutionException e) {
			fail();
		} catch (IllegalArgumentException e) {
			
		}
	}
	
	@After
	public void resetHanlderAfterTest() {
		ClassPathHandler.resetSingleton();
	}

}
