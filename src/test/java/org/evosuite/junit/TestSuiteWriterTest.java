package org.evosuite.junit;

import org.junit.*;

public class TestSuiteWriterTest {

	@Test
	public void testGetBeforeAndAfterMethods(){
		TestSuiteWriter writer = new TestSuiteWriter();
		String result = writer.getBeforeAndAfterMethods(true);
		System.out.println(result);
		//This is just for regression. If changes in the code, check output manually if it makes sense 
		Assert.assertEquals(588, result.length());
	}
	
}
