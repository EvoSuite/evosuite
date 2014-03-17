package org.evosuite.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import com.examples.with.different.packagename.junit.FooTest;

public class JUnitResultBuilderTest {

	@Test
	public void testTranslationFromJUnitRunner() {
		JUnitCore core = new JUnitCore();
		Result result = core.run(FooTest.class);
		JUnitResultBuilder builder = new JUnitResultBuilder();
		JUnitResult junitResult = builder.build(result);
		
		assertFalse(junitResult.wasSuccessful());
		assertEquals(1, junitResult.getFailureCount());
		assertEquals(1, junitResult.getFailures().size());
		
		
		JUnitFailure junitFailure = junitResult.getFailures().get(0);
		assertTrue(junitFailure.isAssertionError());
	}
}
