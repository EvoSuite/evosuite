/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.runtime.thread;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.java.util.MockTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is used to try to stop all threads
 * spawn by the SUT.
 * 
 * @author arcuri
 *
 */
public class ThreadStopper {

	private static final Logger logger = LoggerFactory.getLogger(ThreadStopper.class);

	protected transient Set<Thread> currentRunningThreads;

	private final KillSwitch killSwitch;
	
	/**
	 * Set of threads that we shouldn't try to stop, usually 
	 * based on:  
	 * TestCaseExecutor.TEST_EXECUTION_THREAD,
	 * Properties.IGNORE_THREADS
	 */
	private final Set<String> threadsToIgnore;

	/**
	 * for how many ms should we wait on joining the threads?
	 */
	private final long timeout; 

	/**
	 * time stamp from when timeout are calculated
	 */
	private long startTime;
	
	
	/**
	 * 
	 */
	public ThreadStopper(KillSwitch killSwitch, Set<String> threadsToIgnore, long timeout){
		this.killSwitch = killSwitch;
		this.timeout = timeout;
		if(threadsToIgnore == null){
			this.threadsToIgnore = new LinkedHashSet<>();
		} else {
			this.threadsToIgnore = threadsToIgnore;
		}
		this.startTime = 0;
	}

	public ThreadStopper(KillSwitch killSwitch,  long timeout, String ... threadsToIgnore){
		this(killSwitch, new LinkedHashSet<String>(Arrays.asList(threadsToIgnore)), timeout);
	}
	
	public void startRecordingTime(){
		startTime = System.currentTimeMillis();
	}
	
	public long getStartTime(){
		return startTime;
	}
	
	/**
	 * <p>
	 * After the test case is executed, if any SUT thread is still running, we
	 * will wait for their termination. To identify which thread belong to SUT,
	 * before test case execution we should check which are the threads that are
	 * running.
	 * </p>
	 * <p>
	 * <b>WARNING</b>: The sandbox might prevent accessing thread informations, 
	 * so need to check carefully when/where this method is called
	 * </p>
	 */
	public void storeCurrentThreads() {
		if (currentRunningThreads == null) {
			currentRunningThreads = Collections.newSetFromMap(new IdentityHashMap<Thread, Boolean>());
		} else {
			currentRunningThreads.clear();
		}

		Map<Thread, StackTraceElement[]> threadMap = Thread.getAllStackTraces();
		for (Thread t : threadMap.keySet()) {
			if (t.isAlive()) {
				currentRunningThreads.add(t);
			}
		}
	}

	/**
	 * Try to kill (and then join) the SUT threads. Killing the SUT threads is
	 * important, because some spawn threads could just wait on objects/locks,
	 * and so make the test case executions always last TIMEOUT ms.
	 */
	public void killAndJoinClientThreads() throws IllegalStateException {

		if (currentRunningThreads == null) {
			throw new IllegalStateException(
			        "The current threads are not set. You need to call storeCurrentThreads() first");
		}

		if(RuntimeSettings.mockJVMNonDeterminism){
			MockTimer.stopAllTimers();
		}
		
		// Using enumerate here because getAllStackTraces may call hashCode of the SUT,
		// if the SUT is a subclass of Thread
		Thread[] threadArray = new Thread[Thread.activeCount() + 2];
		Thread.enumerate(threadArray);

		/*
		 * First we set the kill switch in the instrumented bytecode, this
		 * to prevent issues with code that do not handle interrupt 
		 */
		killSwitch.setKillSwitch(true);

		/*
		 * try to interrupt the SUT threads
		 */
		checkThreads:
		for (Thread t : threadArray) {
			// May happen...
			if(t == null){
				continue;
			}

			if (t.isAlive() && !currentRunningThreads.contains(t)) {
				/*
				 * We may want to ignore some threads such as GUI event handlers 
				 */
				for(String name : threadsToIgnore) {
					if(t.getName().startsWith(name)) {
						continue checkThreads;
					}
				}
				t.interrupt();
			}
		}

		/*
		 * now, join up to a total of TIMEOUT ms. 
		 */
		checkThreads:
		for (Thread t : threadArray) {
			// May happen...
			if(t == null)
				continue;

			if (t.isAlive() && !currentRunningThreads.contains(t)) {
				for(String name : threadsToIgnore) {
					if(t.getName().startsWith(name)) {
						continue checkThreads;
					}
				}

				logger.info("Found new thread");
				try {
					/*
					 * In total the test case should not run for more than Properties.TIMEOUT ms
					 */
					long delta = System.currentTimeMillis() - startTime;
					long waitingTime = timeout - delta;
					if (waitingTime > 0) {
						t.join(waitingTime);
					}
				} catch (InterruptedException e) {
					// What can we do?
					break;
				}
				if (t.isAlive()) {
					logger.info("Thread is still alive: " + t.getName());
				}
			}
		}

		/*
		 * we need it, otherwise issue during search in which accessing enum in SUT would call toString,
		 * and so throw a TimeoutExceeded exception 
		 */
		killSwitch.setKillSwitch(false);

		/*
		 * important. this is used to later check if current threads are set
		 */
		currentRunningThreads = null;
	}

}
