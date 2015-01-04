package com.examples.with.different.packagename.testcarver;

import org.junit.*;

public class InnerConstructorTest {

	@Test
	public void test(){
		InnerConstructor c = new InnerConstructor();
		c.getFoo();
	}
}
