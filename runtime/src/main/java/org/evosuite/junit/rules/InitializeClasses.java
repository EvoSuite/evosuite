package org.evosuite.junit.rules;

import java.util.Arrays;

public class InitializeClasses extends BaseRule {

	private String[] classNames;

	public InitializeClasses(String... classesToInitialize) {
		classNames = Arrays.copyOf(classesToInitialize, classesToInitialize.length);
	}

	@Override
	protected void before() {
		org.evosuite.agent.InstrumentingAgent.activate(); 
		for (int i=0; i< classNames.length;i++) {
			org.evosuite.runtime.Runtime.getInstance().resetRuntime(); 
			String classNameToLoad = classNames[i];
			ClassLoader classLoader = getClass().getClassLoader();
			try {
				Class.forName(classNameToLoad, true, classLoader);
			 } catch (ExceptionInInitializerError ex) {
				 System.err.println("Could not initialize " + classNameToLoad);
			 } catch (Throwable t) {
			 }
		}
		org.evosuite.agent.InstrumentingAgent.deactivate(); 
	}

	@Override
	protected void after() {
	}
}
