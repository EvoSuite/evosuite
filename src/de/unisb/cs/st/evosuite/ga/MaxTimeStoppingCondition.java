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

package de.unisb.cs.st.evosuite.ga;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;

/**
 * Stop search after a predefined amount of time
 * 
 * @author Gordon Fraser
 *
 */
public class MaxTimeStoppingCondition extends StoppingCondition {

	/** Maximum number of seconds */
	protected int max_seconds = Properties.getPropertyOrDefault("generations", 1000);

	
	protected long start_time = 0L;
	
	public void searchStarted(FitnessFunction objective) {
		start_time = System.currentTimeMillis();
	}
	
	/**
	 * We are finished when the time is up
	 */
	@Override
	public boolean isFinished() {
		long current_time = System.currentTimeMillis();
		return (current_time - start_time) > max_seconds;
	}

	/**
	 * Reset
	 */
	@Override
	public void reset() {
		start_time = System.currentTimeMillis();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.ga.SearchListener#fitnessEvaluation(java.util.List)
	 */
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		// TODO Auto-generated method stub
		
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

}
