package com.examples.with.different.packagename;


import org.evosuite.Properties;
import org.evosuite.junit.EvoSuiteRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;



@RunWith(EvoSuiteRunner.class)
public class EvoSuiteRunnerTest {

	final boolean defaultReplace = Properties.REPLACE_CALLS;
	
	
	@After
	public void after(){
		Properties.REPLACE_CALLS = defaultReplace;
	}
	
	@Test
	public void test() {
		Properties.REPLACE_CALLS  = true;
		TimeOperation op = new TimeOperation();
		long long0 = 100L;
		org.evosuite.runtime.System.setCurrentTimeMillis(long0);
		boolean result = op.testMe();
		Assert.assertEquals(true, result);
		
	}
}
