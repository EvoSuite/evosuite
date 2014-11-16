package org.evosuite.mock.java.lang;

import org.junit.Assert;

import org.evosuite.Properties;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.MockFramework;
import org.junit.After;
import org.junit.Test;

import com.examples.with.different.packagename.mock.java.lang.MemoryCheck;

public class MockRuntimeLoadingTest {

	private static final boolean DEFAULT_JVM = RuntimeSettings.mockJVMNonDeterminism;
	private static final boolean DEFAULT_REPLACE_CALLS = Properties.REPLACE_CALLS;
	
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
