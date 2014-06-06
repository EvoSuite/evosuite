package com.examples.with.different.packagename.testcarver;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class ClassWithPublicFieldReadingTestCase {

	
	@Test
	public void test2() {
		ClassWithPublicField x = new ClassWithPublicField();
		Locale y = x.x;
		Assert.assertTrue(x.testMe(y));
	}
}
