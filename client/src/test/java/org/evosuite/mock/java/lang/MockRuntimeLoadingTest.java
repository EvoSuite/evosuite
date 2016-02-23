/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.mock.java.lang;

import org.evosuite.classpath.ClassPathHandler;
import org.junit.*;

import org.evosuite.Properties;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.MockFramework;

import com.examples.with.different.packagename.mock.java.lang.MemoryCheck;

public class MockRuntimeLoadingTest {

	private static final boolean DEFAULT_JVM = RuntimeSettings.mockJVMNonDeterminism;
	private static final boolean DEFAULT_REPLACE_CALLS = Properties.REPLACE_CALLS;

	@BeforeClass
	public static void init(){
		String cp = System.getProperty("user.dir") + "/target/test-classes";
		ClassPathHandler.getInstance().addElementToTargetProjectClassPath(cp);
	}

	@After
	public void tearDown(){
		RuntimeSettings.mockJVMNonDeterminism = DEFAULT_JVM;
		Properties.REPLACE_CALLS = DEFAULT_REPLACE_CALLS;
	}
	
	@Test
	public void testReplacementMethod() throws Exception{
		RuntimeSettings.mockJVMNonDeterminism  = true;
		Properties.REPLACE_CALLS = true;
		
		InstrumentingClassLoader cl = new InstrumentingClassLoader();
		MockFramework.enable();
		Class<?> clazz = cl.loadClass(MemoryCheck.class.getCanonicalName());
		
		Object mc = clazz.newInstance();
		String expected = "500"; //this is hard coded in the mock
		Assert.assertEquals(expected, mc.toString());
	}
	
}
