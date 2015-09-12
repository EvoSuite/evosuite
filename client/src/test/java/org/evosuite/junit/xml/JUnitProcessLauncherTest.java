/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
