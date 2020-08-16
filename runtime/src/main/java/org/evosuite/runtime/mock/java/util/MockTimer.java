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
package org.evosuite.runtime.mock.java.util;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.thread.ThreadCounter;

public class MockTimer extends Timer implements OverrideMock{

	private static final Set<Timer> instances = new LinkedHashSet<>();  
	
	/**
	 * As interrupting threads might not work on Timer objects, 
	 * explicitly kill all created instances
	 */
	public static synchronized void stopAllTimers(){
		for(Timer timer : instances) {
			try {
				// Since SUT classes may inherit from MockTimer, this code (called from EvoSuite)
				// may lead to SUT exceptions.
				timer.cancel();
			} catch(Throwable t) {
				// Ignore, since this is only part of post-test cleanup
			}
		}
		instances.clear();
	}
	
	private static synchronized void registerTimer(Timer timer){
        if(MockFramework.isEnabled()) {
            try{
                ThreadCounter.getInstance().checkIfCanStartNewThread();
            } catch(RuntimeException e) {
                timer.cancel();
            }
        }
        instances.add(timer);
	}
	
	// ---------  constructors  --------------
	
	public MockTimer() {
		super();
		registerTimer(this);
	}

	public MockTimer(boolean isDaemon) {
		super(isDaemon);
		registerTimer(this);
	}

	public MockTimer(String name) {
		super(name);
		registerTimer(this);
	}

	public MockTimer(String name, boolean isDaemon) {
		super(name,isDaemon);
		registerTimer(this);
	}

	
	// ---------- unchanged methods ----------
	
	@Override
	public void schedule(TimerTask task, long delay) {
		super.schedule(task, delay);
	}

	@Override
	public void schedule(TimerTask task, Date time) {
		super.schedule(task, time);
	}

	@Override
	public void schedule(TimerTask task, long delay, long period) {
		super.schedule(task, delay, period);
	}

	@Override
	public void schedule(TimerTask task, Date firstTime, long period) {
		super.schedule(task, firstTime, period);
	}

	@Override
	public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
		super.scheduleAtFixedRate(task, delay, period);
	}

	@Override
	public void scheduleAtFixedRate(TimerTask task, Date firstTime,
			long period) {
		super.scheduleAtFixedRate(task, firstTime, period);
	}

	@Override
	public void cancel() {
		super.cancel();
	}

	@Override
	public int purge() {
		return super.purge();
	}

}
