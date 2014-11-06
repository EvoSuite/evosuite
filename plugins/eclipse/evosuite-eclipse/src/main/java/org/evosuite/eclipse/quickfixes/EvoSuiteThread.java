package org.evosuite.eclipse.quickfixes;

import org.eclipse.core.resources.IResource;

public class EvoSuiteThread implements Runnable {
	
	private IResource res;
	private TestGenerationTrigger testAction;
	
	public EvoSuiteThread(IResource r){
		res = r;
	}

	@Override
	public void run() {
		testAction = new TestGenerationTrigger(res);
		testAction.run(null);
	}

	public void stop(){
		testAction.stop();
	}
	
	public boolean hasStopped(){
		return testAction == null || !testAction.isRunning();
	}
	
}
