/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Sebastian Steenbuck
 *
 */
public interface InterfaceTestRunnable extends Callable<ExecutionResult>{
	//#TODO steenbuck add javadoc
	public Map<Integer, Throwable> getExceptionsThrown(); 
	
	public boolean isRunFinished();
}
