package org.evosuite.runtime.agent;

import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.thread.KillSwitchHandler;
import org.evosuite.runtime.thread.ThreadStopper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.with.different.packagename.agent.InfiniteLoop;
import com.examples.with.different.packagename.agent.TimerClass;

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
	
	@Test
	public void testTimer() throws InterruptedException{
		
		ThreadStopper stopper = new ThreadStopper(KillSwitchHandler.getInstance(), 1000);
		stopper.storeCurrentThreads();
		stopper.startRecordingTime();

		Object obj = null;
		
		try{
			InstrumentingAgent.activate();
			obj = new TimerClass();
		} finally {
			InstrumentingAgent.deactivate();
		}
		
		Thread.sleep(100); // just to be sure the thread has been started
		
		Assert.assertTrue(isThreadRunning(TimerClass.NAME));
		
		//this would disable the handling of MockTimer
		RuntimeSettings.mockJVMNonDeterminism = false;		
		//this should not be enough
		stopper.killAndJoinClientThreads();
		Assert.assertTrue(isThreadRunning(TimerClass.NAME));
		
		//now, be sure MockTimer is used
		RuntimeSettings.mockJVMNonDeterminism = true;
		stopper.storeCurrentThreads();
		stopper.startRecordingTime();
		stopper.killAndJoinClientThreads();
		Assert.assertFalse(isThreadRunning(TimerClass.NAME));
	}
	
	private boolean isThreadRunning(String name){
		Thread thread = null;
		for(Thread t : Thread.getAllStackTraces().keySet()){
			if(t.getName().equals(name)){
				thread = t;
				break;
			}
		}
		
		if(thread == null){
			return false;
		} else {
			return thread.isAlive();
		}
	}
}
