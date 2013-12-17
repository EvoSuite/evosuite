package org.evosuite.utils;

import org.junit.Assert;
import org.junit.Test;

public class ResourceListTest {

	@Test
	public void testLoadOfEvoSuiteTestClasses() {

		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();

		String className = ResourceListFoo.class.getName();
		String res = ResourceList.getClassAsResource(className);
		Assert.assertNotNull(res);
	}

	@Test
	public void testClassPathCacheRegressionBug() {
		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();

		String className1 = ResourceListFoo.class.getName();
		String className2 = ResourceListFoo2.class.getName();
		String res1 = ResourceList.getClassAsResource(className1);
		String res2 = ResourceList.getClassAsResource(className2);
		Assert.assertNotEquals(res1, res2);
	}

	private class ResourceListFoo {
	};

	private class ResourceListFoo2 {
	};

}
