package com.examples.with.different.packagename.testcarver;

import static org.junit.Assert.assertEquals;

import org.evosuite.agent.InstrumentingAgent;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestPersonWithJavaAgent {

	
	@BeforeClass 
	public static void initEvoSuiteFramework(){ 
		org.evosuite.Properties.REPLACE_CALLS = true;
		InstrumentingAgent.initialize();
	} 

	@Before
	public void init() {
		InstrumentingAgent.activate();
	}

	@After
	public void tearDown() {
		InstrumentingAgent.deactivate();
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
