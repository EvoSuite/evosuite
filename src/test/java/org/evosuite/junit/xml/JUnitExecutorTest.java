package org.evosuite.junit.xml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.evosuite.junit.FooTestClassLoader;
import org.evosuite.junit.JUnitResult;
import org.junit.Test;


public class JUnitExecutorTest {

	@Test
	public void testJUnitFoo() {
		JUnitExecutor executor = new JUnitExecutor();
		Class<?> fooTestClass = new FooTestClassLoader().loadFooTestClass();
		JUnitResult result = executor.execute(fooTestClass);
		assertNotNull(result);
		assertFalse(result.wasSuccessful());
	}

}
