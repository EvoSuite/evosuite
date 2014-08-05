package com.examples.with.different.packagename.testcarver;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class ConcreteSubClassWithFieldsTestCase {

	@Test
	public void test1() {
		ConcreteSubClassWithFields foo = new ConcreteSubClassWithFields(42);
		assertFalse(foo.testMe(42));
	}
	
	@Test
	public void test2() {
		ConcreteSubClassWithFields foo = new ConcreteSubClassWithFields(42, 100);
		assertFalse(foo.testMe(42));
	}

}
