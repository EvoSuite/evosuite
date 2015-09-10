package org.evosuite.junit;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.evosuite.SystemTest;
import org.evosuite.runtime.instrumentation.MethodCallReplacementClassAdapter;
import org.junit.Test;

import com.examples.with.different.packagename.Euclidean_ESTest;

public class TestExecutionOfGeneratedTestCases  {

	@Test
	public void test() {
		MethodCallReplacementClassAdapter.dirtyHack_applyUIDTransformation = false;

		try {
			JUnitRunner jR = new JUnitRunner(Euclidean_ESTest.class);
			jR.run();

			List<JUnitResult> results = jR.getTestResults();
			assertTrue(results.size() == 1);
			assertTrue(results.get(0).wasSuccessful());
		} finally {
			MethodCallReplacementClassAdapter.dirtyHack_applyUIDTransformation = true;
		}
	}
}
