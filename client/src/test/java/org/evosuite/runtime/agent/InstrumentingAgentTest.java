package org.evosuite.runtime.agent;

import org.junit.Assert;

import org.evosuite.Properties;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.agent.InstrumentingAgent;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.with.different.packagename.agent.TimeA;
import com.examples.with.different.packagename.agent.TimeB;

/**
 * FIXME: this should really be run as an integration test, as it requires
 * the creation of the jar file first
 * 
 * @author arcuri
 *
 */
public class InstrumentingAgentTest {

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
}

