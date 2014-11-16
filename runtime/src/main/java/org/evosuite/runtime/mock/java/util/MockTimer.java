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
		for(Timer timer : instances){
			timer.cancel();
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
