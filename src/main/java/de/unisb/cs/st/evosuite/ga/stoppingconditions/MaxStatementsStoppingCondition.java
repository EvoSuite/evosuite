/**
 * 
 */
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


package de.unisb.cs.st.evosuite.ga.stoppingconditions;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;

/**
 * @author Gordon Fraser
 *
 */
public class MaxStatementsStoppingCondition extends StoppingCondition {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(MaxStatementsStoppingCondition.class);
	
	/** Maximum number of iterations */
	protected static int max_statements = Properties.GENERATIONS;

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
		current_statement = 0;
	}
	
	public static int getNumExecutedStatements() {
		return current_statement;
	}
	
	/**
	 * Set new upper limit
	 * @param max
	 */
	public void setMaxExecutedStatements(int max) {
		max_statements = max;
	}

	/* (non-Javadoc)
     * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#getCurrentValue()
     */
    @Override
    public int getCurrentValue() {
    	return current_statement;
    }

	/* (non-Javadoc)
     * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#setLimit(int)
     */
    @Override
    public void setLimit(int limit) {
    	max_statements = limit;
    }

    @Override
    public int getLimit() {
    	return max_statements;
    }

}
