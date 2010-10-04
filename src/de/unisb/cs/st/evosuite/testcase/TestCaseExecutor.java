/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.assertion.StringOutputTrace;
import de.unisb.cs.st.evosuite.assertion.StringTraceExecutionObserver;

/**
 * The test case executor manages thread creation/deletion to execute a test case
 * 
 * @author Gordon Fraser
 *
 */
public class TestCaseExecutor {

	private Logger logger = Logger.getLogger(TestCaseExecutor.class);
	
	private boolean log = true;
	
	public static long timeout = Properties.getPropertyOrDefault("timeout", 5000);
	
	private StringOutputTrace trace = null;
	
	private List<ExecutionObserver> observers;
	
	protected boolean static_hack = Properties.getPropertyOrDefault("static.hack", false);

	public TestCaseExecutor() {
		observers = new ArrayList<ExecutionObserver>();
	}
	
	
	public static class TimeoutExceeded extends RuntimeException {
	    private static final long serialVersionUID = -5314228165430676893L;
	  }
	
	public void setup() {
		// start own thread
	}
	
	public void pulldown() {
		// stop thread
	}

	public void setLogging(boolean value) {
		log = value;
	}
	
	public void addObserver(ExecutionObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(ExecutionObserver observer) {
		observers.remove(observer);
	}

	public void newObservers() {
		observers = new ArrayList<ExecutionObserver>();
	}
	
	private void resetObservers() {
		for(ExecutionObserver observer : observers) {
			observer.clear();
		}
	}
	
	public Map<Integer,Throwable> runWithTrace(TestCase tc) {
		StringTraceExecutionObserver obs = new StringTraceExecutionObserver();
		//observers.add(obs);
		ExecutionTracer.getExecutionTracer().clear();
		if(static_hack)
			TestCluster.getInstance().resetStaticClasses();
		Map<Integer,Throwable> result = run(tc);
		//tc.exceptionThrown = result;
		//observers.remove(obs);
	    //System.out.println(obs.getTrace());
		
		trace = obs.getTrace();
		
		return result;
	}	
	
	public StringOutputTrace getTrace() {
		return trace;
	}
	
	@SuppressWarnings("deprecation")
	public Map<Integer,Throwable> run(TestCase tc) {
		resetObservers();
		
	    TestRunner runner = new TestRunner(null);
	    runner.setLogging(log);
	    Scope scope = new Scope();
	    runner.setup(tc, scope, observers);
	    MaxTestsStoppingCondition.testExecuted();
	    //MaxStatementsStoppingCondition.statementsExecuted(tc.size());
	    
	    try {
	    	// Start the test.
	    	runner.start();

	    	// If test doesn't finish in time, suspend it.
	    	runner.join(timeout);

	    	if (!runner.runFinished) {
	    		logger.warn("Exceeded max wait ("+timeout+"ms): aborting test input:");
	    		logger.warn(tc.toCode());
	    		
	    		runner.interrupt();
	        
	    		if(runner.isAlive()) {
	    			// If test doesn't finish in time, suspend it.
	    			runner.join(timeout);
	    			if (!runner.runFinished) {
	    				runner.stop();// We use this deprecated method because it's the only way to
	    				// stop a thread no matter what it's doing.
	    				//return runner.exceptionsThrown;
	    			}
	    			ExecutionTracer.enable();
	    		}
		    	ExecutionTracer.getExecutionTracer().clear();
	    		runner.exceptionsThrown.put(tc.size(), new TestCaseExecutor.TimeoutExceeded());
	    		/*
	    		 * if(runner.exceptionThrown != null)
	    		 
	    			return runner.exceptionsThrown;
	    		else
	    			return new TestCaseExecutor.TimeoutExceeded();
	    			*/
	    	}
	    	return runner.exceptionsThrown;

	    } catch (java.lang.InterruptedException e) {
	      throw new IllegalStateException("A RunnerThread thread shouldn't be interrupted by anyone! "
	          + "(this may be a bug in the program; please report it.)");
	    }
		
	}

	
	//public Trace runWithTrace(TestCase e) {
	//	
	//}

}
