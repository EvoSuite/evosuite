package org.evosuite.runtime.thread;

public class KillSwitchHandler implements KillSwitch{

	private static final KillSwitchHandler singleton = new KillSwitchHandler();
	
	private volatile boolean kill;
	
	/**
	 * singleton constructor
	 */
	private KillSwitchHandler(){
		kill = false;
	}
	
	public static KillSwitchHandler getInstance(){
		return singleton;
	}

	@Override
	public void setKillSwitch(boolean kill) {
		this.kill = kill;
	}
	
	public void checkTimeout() throws RuntimeException{
		if(kill){
			throw new RuntimeException("Kill switch"); 
		}
	}
	
}
