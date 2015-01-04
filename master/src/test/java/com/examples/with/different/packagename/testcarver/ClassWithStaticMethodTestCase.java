package com.examples.with.different.packagename.testcarver;

import static org.junit.Assert.*;

import org.junit.Test;

public class ClassWithStaticMethodTestCase {

	@Test
	public void testStaticMethod() {
		ClassWithStaticMethod c = ClassWithStaticMethod.getInstance();
		assertTrue(c.testMe(42));
	}
}
