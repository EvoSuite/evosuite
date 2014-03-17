package com.examples.with.different.packagename.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class FooTest {

	@Test
	public void test1() {
		Foo foo = new Foo();
		int result = foo.add(10, 15);
		assertEquals(25, result);
	}
	
	@Test
	public void test2() {
		Foo foo = new Foo();
		int result = foo.add(20, 35);
		assertEquals(55, result);
	}

	@Test
	public void test3() {
		Foo foo = new Foo();
		int result = foo.add(10, 35);
		assertEquals(46, result);
	}

}
