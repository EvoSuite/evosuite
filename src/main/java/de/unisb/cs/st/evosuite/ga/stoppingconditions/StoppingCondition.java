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

package de.unisb.cs.st.evosuite.ga.stoppingconditions;

import java.io.Serializable;
import java.text.NumberFormat;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.SearchListener;

/**
 * Base class of decision functions that stop the search
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class StoppingCondition implements SearchListener, Serializable {

	private static final long serialVersionUID = -8221978873140881671L;

	protected static Logger logger = LoggerFactory.getLogger(StoppingCondition.class);

	public StoppingCondition() {
		reset();
	}

	public abstract boolean isFinished();

	@Override
	public void searchStarted(GeneticAlgorithm algorithm) {

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
	public void iteration(GeneticAlgorithm algorithm) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.ga.SearchListener#searchFinished(java.util.List)
	 */
	@Override
	public void searchFinished(GeneticAlgorithm algorithm) {
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
	 * Get upper limit of resources
	 * 
	 * Mainly used for toString()
	 * 
	 * @return limit
	 */
	public abstract int getLimit();

	/**
	 * How much of the budget have we used up
	 * 
	 * @return
	 */
	public abstract int getCurrentValue();

	@Override
	public String toString() {
		StringBuilder r = new StringBuilder();
		String type = getClass().toString();
		try { // just to make sure
			type = type.substring(type.lastIndexOf(".") + 1);
		} catch (Exception e) {
		}
		// cut away "StoppingCondition" suffix
		if(type.endsWith("StoppingCondition"))
			type = type.substring(0, type.length() - 17);
		type += " :";
		type = StringUtils.rightPad(type, 24);
		r.append(type);
		String value = NumberFormat.getIntegerInstance().format(getCurrentValue());
		value = StringUtils.leftPad(value, 12);
		String limit = NumberFormat.getIntegerInstance().format(getLimit());
		limit = StringUtils.rightPad(limit, 12);
		r.append(value + " / " + limit);
		if (isFinished())
			r.append(" Finished!");

		return r.toString();
	}
}
