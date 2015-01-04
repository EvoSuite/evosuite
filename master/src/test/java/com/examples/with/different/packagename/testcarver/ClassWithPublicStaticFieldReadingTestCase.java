package com.examples.with.different.packagename.testcarver;

import org.junit.Assert;
import org.junit.Test;

public class ClassWithPublicStaticFieldReadingTestCase {

	@Test
	public void test2() {
		ClassWithPublicStaticField x = new ClassWithPublicStaticField();
		Assert.assertTrue(x.testMe(ClassWithPublicStaticField.x));
	}
}
