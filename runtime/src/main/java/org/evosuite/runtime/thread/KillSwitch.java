package org.evosuite.runtime.thread;

/**
 * Interface specifying an entity that can stop 
 * the SUT execution. This can be for example achieved
 * by bytecode instrumentation, eg add a call after
 * every single statement
 * 
 * @author arcuri
 *
 */
public interface KillSwitch {

	public void setKillSwitch(boolean kill);
}
