/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import de.unisb.cs.st.ga.GAProperties;
import de.unisb.cs.st.ga.StoppingCondition;


/**
 * @author Gordon Fraser
 *
 */
public class MaxTestsStoppingCondition extends StoppingCondition {

	/** Current number of tests */
	protected static int num_tests = 0;
	
	/** Maximum number of evaluations */
	protected int max_tests = GAProperties.generations;

	public static int getNumExecutedTests() {
		return num_tests;
	}
	
	public static void testExecuted() {
		num_tests++;
	}
	
	public void reset() {
		num_tests = 0;
	}

	@Override
	public boolean isFinished() {
		//logger.info("Current number of tests executed: "+num_tests);
		//System.out.println("Current number of tests executed: "+num_tests);
		return num_tests >= max_tests;
	}

}
