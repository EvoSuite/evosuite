/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import org.apache.log4j.Logger;

import de.unisb.cs.st.ga.GAProperties;
import de.unisb.cs.st.ga.StoppingCondition;

/**
 * @author Gordon Fraser
 *
 */
public class MaxStatementsStoppingCondition extends StoppingCondition {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MaxStatementsStoppingCondition.class);
	
	/** Maximum number of iterations */
	protected static int max_statements = GAProperties.generations;

	/** Maximum number of iterations */
	protected static int current_statement = 0;
	
	/**
	 * Add a given number of executed statements
	 * @param num
	 */
	public static void statementsExecuted(int num) {
		current_statement += num;
	}
	
	/**
	 * Finished, if the maximum number of statements has been reached
	 */
	@Override
	public boolean isFinished() {
		//logger.info("Current number of statements executed: "+current_statement+"/"+max_statements);
		return current_statement >= max_statements;
	}

	/**
	 * Reset counter
	 */
	public void reset() {
		//logger.info("Resetting current statements");
		current_statement = 0;
	}
	
	public static int getNumExecutedStatements() {
		return current_statement;
	}
	
	public void setMaxExecutedStatements(int max) {
		max_statements = max;
	}

}
