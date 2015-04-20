package com.examples.with.different.packagename.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PassingFooTest {
	
	@Test
	public void test1() {
		Foo foo = new Foo();
		int result = foo.add(10, 10);
		assertEquals(20,result);
	}

	@Test
	public void test2() {
		Foo foo = new Foo();
		int result = foo.add(10, 30);
		assertEquals(40,result);
	}

	@Test
	public void test3() {
		Foo foo = new Foo();
		int result = foo.add(40, 10);
		assertEquals(50,result);
	}

	
}
