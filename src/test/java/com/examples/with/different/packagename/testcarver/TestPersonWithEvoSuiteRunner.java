package com.examples.with.different.packagename.testcarver;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestPersonWithEvoSuiteRunner {

	@BeforeClass 
	public static void initEvoSuiteFramework(){ 
		org.evosuite.Properties.REPLACE_CALLS = true; 
	} 

	@Test
	public void test0_1() throws Throwable {
		Person person0 = new Person("", "");
		String string0 = person0.getFirstName();
		assertEquals("", string0);
	}



	@Test
	public void test0() throws Throwable {
		Person person0 = new Person("", "");
		String string0 = person0.getLastName();
		assertEquals("", string0);
	}

}
