package org.evosuite.utils;

import org.junit.Assert;
import org.junit.Test;

public class ResourceListTest {

	@Test
	public void testLoadOfEvoSuiteTestClasses(){
		
		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
		
		String className = ResourceListFoo.class.getName();
		String res = ResourceList.getClassAsResource(className);
		Assert.assertNotNull(res);
	}

	
	private class ResourceListFoo{};
}
