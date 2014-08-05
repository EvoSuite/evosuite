package com.examples.with.different.packagename.testcarver;

import org.junit.Assert;
import org.junit.Test;

public class ClassDependingOnStaticFieldInOtherClassTestCase {


	@Test
	public void test2() {
		ClassDependingOnStaticFieldInOtherClass x = new ClassDependingOnStaticFieldInOtherClass();
		Assert.assertTrue(x.testMe(StaticFieldInOtherClass.x));
	}
}
