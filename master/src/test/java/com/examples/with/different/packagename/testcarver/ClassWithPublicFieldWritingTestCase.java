package com.examples.with.different.packagename.testcarver;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

public class ClassWithPublicFieldWritingTestCase {

	@Test
	public void test() {
		ClassWithPublicField x = new ClassWithPublicField();
		x.x = Locale.ENGLISH;
		Assert.assertFalse(x.testMe(Locale.FRANCE));
		x.x = Locale.GERMAN;
		Assert.assertTrue(x.testMe(Locale.GERMAN));
	}
	
}
