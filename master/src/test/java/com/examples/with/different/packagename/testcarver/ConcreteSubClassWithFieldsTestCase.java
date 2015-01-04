package com.examples.with.different.packagename.testcarver;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConcreteSubClassWithFieldsTestCase {

	@Test
	public void test1() {
		ConcreteSubClassWithFields foo = new ConcreteSubClassWithFields(42);
		assertTrue(foo.testMe(42));
	}
	
	@Test
	public void test2() {
		ConcreteSubClassWithFields foo = new ConcreteSubClassWithFields(42, 100);
		assertTrue(foo.testMe(42));
	}

}
