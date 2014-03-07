package org.evosuite.setup;

import java.lang.reflect.Method;
import java.util.Set;

import org.evosuite.Properties;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestAccessMethod {
	@After
	public void resetProperties() {
		Properties.CLASS_PREFIX = "";
		Properties.TARGET_CLASS = "";
	}

	protected Method getMethod(Class<?> clazz, String name) {
		Set<Method> methods = TestClusterGenerator.getMethods(clazz);
		for (Method m : methods) {
			if (m.getName().equals(name))
				return m;
		}
		Assert.fail("No such method: " + name);
		return null;
	}

	@Test
	public void testPublicMethod() {
		Properties.CLASS_PREFIX = "some.package";
		Properties.TARGET_CLASS = "some.package.Foo";
		Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
		                     "publicMethod");
		boolean result = TestClusterGenerator.canUse(f);
		Assert.assertTrue(result);
	}

	@Test
	public void testDefaultMethod() {
		Properties.CLASS_PREFIX = "some.package";
		Properties.TARGET_CLASS = "some.package.Foo";
		Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
		                     "defaultMethod");
		boolean result = TestClusterGenerator.canUse(f);
		Assert.assertFalse(result);
	}

	@Test
	public void testProtectedMethod() {
		Properties.CLASS_PREFIX = "some.package";
		Properties.TARGET_CLASS = "some.package.Foo";
		Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
		                     "protectedMethod");
		boolean result = TestClusterGenerator.canUse(f);
		Assert.assertFalse(result);
	}

	@Test
	public void testPrivateMethod() {
		Properties.CLASS_PREFIX = "some.package";
		Properties.TARGET_CLASS = "some.package.Foo";
		Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
		                     "privateMethod");
		boolean result = TestClusterGenerator.canUse(f);
		Assert.assertFalse(result);
	}

	@Test
	public void testPublicMethodTargetPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
		Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
		                     "publicMethod");
		boolean result = TestClusterGenerator.canUse(f);
		Assert.assertTrue(result);
	}

	@Test
	public void testDefaultMethodTargetPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
		Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
		                     "defaultMethod");
		boolean result = TestClusterGenerator.canUse(f);
		Assert.assertTrue(result);
	}

	@Test
	public void testDefaultMethodInSuperClass() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
		Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
		                     "defaultMethodInSuperClass");
		boolean result = TestClusterGenerator.canUse(f);
		Assert.assertFalse(result);
	}

	@Test
	public void testProtectedMethodTargetPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
		Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
		                     "protectedMethod");
		boolean result = TestClusterGenerator.canUse(f);
		Assert.assertTrue(result);
	}

	@Test
	public void testPrivateMethodTargetPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
		Method f = getMethod(com.examples.with.different.packagename.AccessExamples.class,
		                     "privateMethod");
		boolean result = TestClusterGenerator.canUse(f);
		Assert.assertFalse(result);
	}

	@Test
	public void testPublicMethodTargetSubPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename.subpackage";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.subpackage.Foo";
		Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
		                     "publicMethod");
		boolean result = TestClusterGenerator.canUse(f,
		                                             com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
		Assert.assertTrue(result);
	}

	@Test
	public void testProtectedMethodTargetSubPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename.subpackage";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.subpackage.Foo";
		Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
		                     "protectedMethod");
		boolean result = TestClusterGenerator.canUse(f,
		                                             com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
		Assert.assertFalse(result);
	}

	@Test
	public void testDefaultMethodTargetSubPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename.subpackage";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.subpackage.Foo";
		Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
		                     "defaultMethod");
		boolean result = TestClusterGenerator.canUse(f,
		                                             com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
		Assert.assertFalse(result);
	}

	@Test
	public void testPrivateMethodTargetSubPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename.subpackage";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.subpackage.Foo";
		Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
		                     "privateMethod");
		boolean result = TestClusterGenerator.canUse(f,
		                                             com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
		Assert.assertFalse(result);
	}

	@Test
	public void testPublicMethodTargetFromSubPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
		Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
		                     "publicMethod");
		boolean result = TestClusterGenerator.canUse(f,
		                                             com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
		Assert.assertTrue(result);
	}

	@Test
	public void testProtectedMethodTargetFromSubPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
		Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
		                     "protectedMethod");
		boolean result = TestClusterGenerator.canUse(f,
		                                             com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
		Assert.assertFalse(result);
	}

	@Test
	public void testDefaultMethodTargetFromSubPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
		Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
		                     "defaultMethod");
		boolean result = TestClusterGenerator.canUse(f,
		                                             com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
		Assert.assertFalse(result);
	}

	@Test
	public void testPrivateMethodTargetFromSubPackage() {
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.Foo";
		Method f = getMethod(com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class,
		                     "privateMethod");
		boolean result = TestClusterGenerator.canUse(f,
		                                             com.examples.with.different.packagename.subpackage.AccessExamplesSubclass.class);
		Assert.assertFalse(result);
	}

	@Test
	public void testArrayListBug() {
		Properties.TARGET_CLASS = "com.examples.with.different.packagename.ArrayStack";
		Properties.CLASS_PREFIX = "com.examples.with.different.packagename";
		try {
			Method m = getMethod(java.util.ArrayList.class, "elementData");
			boolean result = TestClusterGenerator.canUse(m,
			                                             com.examples.with.different.packagename.ArrayStack.class);
			Assert.assertFalse(result);
		} catch (Throwable e) {
			// Method elementData only exists in Java 7
		}
	}

}
