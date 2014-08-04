package org.evosuite.runtime.agent;

import org.junit.*;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.agent.InstrumentingAgent;

import com.examples.with.different.packagename.agent.AbstractTime;
import com.examples.with.different.packagename.agent.ConcreteTime;
import com.examples.with.different.packagename.agent.TimeA;
import com.examples.with.different.packagename.agent.TimeB;

/**
 * Note: this needs be run as an integration test (IT), as it requires
 * the creation of the jar file first.
 * This is automatically set up in the pom file, but the test might fail
 * if run directly from an IDE
 * 
 * @author arcuri
 *
 */
public class InstrumentingAgent_IT {

	private final boolean replaceCalls = RuntimeSettings.mockJVMNonDeterminism;

	@BeforeClass
	public static void initClass(){
		InstrumentingAgent.initialize();
	}
	
	@Before
	public void storeValues() {
		RuntimeSettings.mockJVMNonDeterminism = true;
	}

	@After
	public void resetValues() {
		RuntimeSettings.mockJVMNonDeterminism = replaceCalls;
	}

	@Test
	public void testTime(){

		long now = System.currentTimeMillis();
		Assert.assertTrue("",TimeB.getTime() >= now);
		
		long expected = 42;
		org.evosuite.runtime.System.setCurrentTimeMillis(expected);

		try{
			InstrumentingAgent.activate();
			Assert.assertEquals(expected, TimeA.getTime());
		} finally {
			InstrumentingAgent.deactivate();
		}
	}
	
	@Test
	public void testTransformationInAbstractClass(){
		long expected = 42;
		org.evosuite.runtime.System.setCurrentTimeMillis(expected);
		try{
			InstrumentingAgent.activate();
			AbstractTime time = new ConcreteTime();
			Assert.assertEquals(expected, time.getTime());
		} finally {
			InstrumentingAgent.deactivate();
		}
	}
}

