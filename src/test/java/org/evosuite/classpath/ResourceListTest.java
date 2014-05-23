package org.evosuite.classpath;

import java.io.IOException;
import java.io.InputStream;

import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.classpath.ResourceList;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ResourceListTest {

	@BeforeClass
	public static void initClass(){
		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
	} 
	
	@Test
	public void testLoadOfEvoSuiteTestClasses() {
		String className = ResourceListFoo.class.getName();
		String res = ResourceList.getClassAsResource(className);
		Assert.assertNotNull(res);
	}

	@Test
	public void testLoadOfEvoSuiteTestClassesAsStream() throws IOException {
		String className = ResourceListFoo.class.getName();
		InputStream res = ResourceList.getClassAsStream(className);
		Assert.assertNotNull(res);
		res.close();
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
