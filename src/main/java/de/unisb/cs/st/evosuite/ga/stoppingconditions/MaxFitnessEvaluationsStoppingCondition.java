/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of the GA library.
 * 
 * GA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with GA.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.ga.stoppingconditions;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.Chromosome;

/**
 * Stop search after a predefined maximum search depth
 * 
 * @author Gordon Fraser
 *
 */
public class MaxFitnessEvaluationsStoppingCondition extends StoppingCondition {

	/** Maximum number of evaluations */
	protected int max_evaluations = Properties.getPropertyOrDefault("generations", 100);

	/** Maximum number of iterations */
	protected static int current_evaluation = 0;
	
	/**
	 * Stop when maximum number of fitness evaluations has been reached
	 */
	@Override
	public boolean isFinished() {
		logger.info("Current number of fitness_evaluations: "+current_evaluation);
		return current_evaluation >= max_evaluations;
	}
	
	/**
	 * Keep track of the number of fitness evaluations
	 */
	public void fitnessEvaluation(List<Chromosome> population) {
		current_evaluation++;
	}

	/**
	 * Static getter method
	 */
	public static int getNumFitnessEvaluations() {
		return current_evaluation;
	}
	
	/**
	 * At the end, reset
	 */
	public void reset() {
		current_evaluation = 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.ga.SearchListener#iteration(java.util.List)
	 */
	@Override
	public void iteration(List<Chromosome> population) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.ga.SearchListener#searchFinished(java.util.List)
	 */
	@Override
	public void searchFinished(List<Chromosome> population) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
     * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#setLimit(int)
     */
    @Override
    public void setLimit(int limit) {
    	max_evaluations = limit;
    }
    
	@Override
	public int getLimit() {
		return max_evaluations;
	}

	/* (non-Javadoc)
     * @see de.unisb.cs.st.evosuite.ga.StoppingCondition#getCurrentValue()
     */
    @Override
    public int getCurrentValue() {
    	return current_evaluation;
    }

}
