/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.agent;

import com.examples.with.different.packagename.agent.StartThreads;
import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.instrumentation.MethodCallReplacementCache;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.thread.KillSwitchHandler;
import org.evosuite.runtime.thread.ThreadStopper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.examples.with.different.packagename.agent.InfiniteLoop;
import com.examples.with.different.packagename.agent.TimerClass;

public class InstrumentingAgent_threadIntTest {

	private final boolean replaceCalls = RuntimeSettings.mockJVMNonDeterminism;
	
	@BeforeClass
	public static void initClass(){
		InstrumentingAgent.initialize();
	}
	
	@Before
	public void storeValues() {
		KillSwitchHandler.getInstance().setKillSwitch(false);
		RuntimeSettings.mockJVMNonDeterminism = true;
        MethodCallReplacementCache.resetSingleton();
		Runtime.getInstance().resetRuntime();
	}

	@After
	public void resetValues() {
		RuntimeSettings.mockJVMNonDeterminism = replaceCalls;
		KillSwitchHandler.getInstance().setKillSwitch(false);
	}
	
	
	@Test
    public void testTooManyThreads(){

        RuntimeSettings.maxNumberOfThreads = 100;

		Object obj = null;

        try{
            InstrumentingAgent.activate();
			RuntimeSettings.className = StartThreads.class.getName();

            StartThreads st = new StartThreads();
            st.exe(50);
            st.exe(49);
            obj = st;

        } finally {
            InstrumentingAgent.deactivate();
        }

        StartThreads st = (StartThreads) obj;
        st.exe(2); //this should throw exception, but we are out of instrumentation

        try{
            MockFramework.enable();

            st.exe(2); // now exception
            Assert.fail();

        } catch(RuntimeException e){
          //expected
        } finally {
            MockFramework.disable();
        }

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
