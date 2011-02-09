/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of the GA library.
 * 
 * GA is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * GA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * GA. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.ga;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * Base class of decision functions that stop the search
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class StoppingCondition implements SearchListener {

	protected static Logger logger = Logger.getLogger(StoppingCondition.class);

	public abstract boolean isFinished();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.javalanche.ga.SearchListener#searchStarted(de.unisb.cs
	 * .st.javalanche.ga.FitnessFunction)
	 */
	@Override
	public void searchStarted(FitnessFunction objective) {

	}

	@Override
	public void fitnessEvaluation(Chromosome chromosome) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.ga.SearchListener#iteration(java.util.List)
	 */
	@Override
	public void iteration(List<Chromosome> population) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.ga.SearchListener#searchFinished(java.util.List)
	 */
	@Override
	public void searchFinished(List<Chromosome> population) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.SearchListener#mutation(de.unisb.cs.st.evosuite
	 * .ga.Chromosome)
	 */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}

	/**
	 * Reset everything
	 */
	public abstract void reset();

	/**
	 * Set new upper limit of resources
	 * 
	 * @param limit
	 */
	public abstract void setLimit(int limit);

	/**
	 * How much of the budget have we used up
	 * 
	 * @return
	 */
	public abstract int getCurrentValue();

}
