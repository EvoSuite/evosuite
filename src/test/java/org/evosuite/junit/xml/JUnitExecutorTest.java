package org.evosuite.junit.xml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.evosuite.junit.JUnitResult;
import org.junit.Test;

import com.examples.with.different.packagename.junit.FooTest;

public class JUnitExecutorTest {

	@Test
	public void testJUnitFoo() {
		JUnitExecutor executor = new JUnitExecutor();
		JUnitResult result = executor.execute(FooTest.class);
		assertNotNull(result);
		assertFalse(result.wasSuccessful());
	}

}
