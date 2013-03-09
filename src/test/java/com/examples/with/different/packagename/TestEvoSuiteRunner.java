package com.examples.with.different.packagename;

import junit.framework.Assert;

import org.evosuite.junit.EvoSuiteRunner;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(EvoSuiteRunner.class)
public class TestEvoSuiteRunner {

	@Test
	public void test() {
		TimeOperation op = new TimeOperation();
		long long0 = 100L;
		org.evosuite.runtime.System.setCurrentTimeMillis(long0);
		boolean result = op.testMe();
		Assert.assertEquals(true, result);
	}
}
