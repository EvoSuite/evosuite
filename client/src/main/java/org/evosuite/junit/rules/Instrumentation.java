package org.evosuite.junit.rules;

/**
 * Should be used as MethodRule
 */
public class Instrumentation extends BaseRule {

	public Instrumentation() {
		// TODO: Is that the right place for this?
		org.evosuite.runtime.agent.InstrumentingAgent.initialize(); 
	}
	
	@Override
	protected void before() {
		org.evosuite.runtime.agent.InstrumentingAgent.activate();
	}

	@Override
	protected void after() {
		org.evosuite.runtime.agent.InstrumentingAgent.deactivate();	
	}

}
