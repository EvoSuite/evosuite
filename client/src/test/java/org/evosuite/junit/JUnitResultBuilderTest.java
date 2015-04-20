package org.evosuite.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class JUnitResultBuilderTest {



	@Test
	public void testTranslationFromJUnitRunner() {
		JUnitCore core = new JUnitCore();

		Class<?> fooTestClass = new FooTestClassLoader().loadFooTestClass();
		Result result = core.run(fooTestClass);
		JUnitResultBuilder builder = new JUnitResultBuilder();
		JUnitResult junitResult = builder.build(result);

		assertFalse(junitResult.wasSuccessful());
		assertEquals(1, junitResult.getFailureCount());
		assertEquals(1, junitResult.getFailures().size());

		JUnitFailure junitFailure = junitResult.getFailures().get(0);
		assertTrue(junitFailure.isAssertionError());
	}
}
