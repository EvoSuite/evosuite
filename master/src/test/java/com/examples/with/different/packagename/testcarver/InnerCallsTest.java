package com.examples.with.different.packagename.testcarver;

import org.junit.*;

public class InnerCallsTest {

	@Test
	public void test(){
		InnerCalls foo = new InnerCalls();
		foo.printA();
		foo.printB();
		foo.printAandB();
	}
}
