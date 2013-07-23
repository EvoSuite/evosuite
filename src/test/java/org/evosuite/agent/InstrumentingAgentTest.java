package org.evosuite.agent;

import org.junit.Assert;

import org.evosuite.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.with.different.packagename.agent.TimeA;
import com.examples.with.different.packagename.agent.TimeB;

public class InstrumentingAgentTest {

	private final boolean replaceCalls = Properties.REPLACE_CALLS;

	@BeforeClass
	public static void initClass(){
		InstrumentingAgent.initialize();
	}
	
	@Before
	public void storeValues() {
		Properties.REPLACE_CALLS = true;
	}

	@After
	public void resetValues() {
		Properties.REPLACE_CALLS = replaceCalls;
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
}

