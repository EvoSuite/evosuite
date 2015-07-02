package org.evosuite.testcase;

import org.junit.Assert;
import org.junit.Test;

public class DefaultTestCaseTest {

	@Test
	public void testClone(){
				
		DefaultTestCase tc = new DefaultTestCase();
		DefaultTestCase clone = (DefaultTestCase) tc.clone();
		Assert.assertTrue(tc.statements != clone.statements);
	}
}
