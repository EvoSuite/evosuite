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
	
	/**
	 * Throw an exception if kill switch is on
	 * @throws RuntimeException
	 */
	public void checkTimeout() throws RuntimeException{
		if(kill){
			throw new RuntimeException("Kill switch"); 
		}
	}
	
	/**
	 * Wrapper around {@link KillSwitchHandler#checkTimeout()} to simplify instrumentation
	 * @throws RuntimeException
	 */
	public static void killIfTimeout() throws RuntimeException {
		getInstance().checkTimeout();
	}
}
