package org.evosuite.runtime.agent;

import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.thread.KillSwitchHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.with.different.packagename.agent.InfiniteLoop;

public class InstrumentingAgent_threadIT {

	private final boolean replaceCalls = RuntimeSettings.mockJVMNonDeterminism;
	
	@BeforeClass
	public static void initClass(){
		InstrumentingAgent.initialize();
	}
	
	@Before
	public void storeValues() {
		KillSwitchHandler.getInstance().setKillSwitch(false);
		RuntimeSettings.mockJVMNonDeterminism = true;
		Runtime.getInstance().resetRuntime();
	}

	@After
	public void resetValues() {
		RuntimeSettings.mockJVMNonDeterminism = replaceCalls;
		KillSwitchHandler.getInstance().setKillSwitch(false);
	}
	
	@Test
	public void testKillSwitch() throws InterruptedException{
		
		RuntimeSettings.mockJVMNonDeterminism  = false; // just in case one day we mock Thread
		
		Object obj = null;
		
		try{
			InstrumentingAgent.activate();
			obj = new InfiniteLoop();
		} finally {
			InstrumentingAgent.deactivate();
		}
		
		InfiniteLoop loop = (InfiniteLoop) obj;
		Thread t = loop.getInfiniteLoop();
		t.start();
		
		t.join(200);
		Assert.assertTrue(t.isAlive());
		
		//interrupting should fail to kill
		t.interrupt();
		t.join(200);
		Assert.assertTrue(t.isAlive());

		KillSwitchHandler.getInstance().setKillSwitch(true);
		t.interrupt();
		t.join(500);
		//should be dead now 
		Assert.assertFalse(t.isAlive());
	}
}
