/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.stoppingconditions;

import java.io.Serializable;
import java.text.NumberFormat;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;


/**
 * Base class of decision functions that stop the search
 *
 * @author Gordon Fraser
 */
public abstract class StoppingConditionImpl implements StoppingCondition, Serializable {

	private static final long serialVersionUID = -8221978873140881671L;

	/**
	 * <p>Constructor for StoppingConditionImpl.</p>
	 */
	public StoppingConditionImpl() {
		reset();
	}

	/** {@inheritDoc} */
	@Override
	public void searchStarted(GeneticAlgorithm<?> algorithm) {

	}

	/** {@inheritDoc} */
	@Override
	public void fitnessEvaluation(Chromosome chromosome) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ga.SearchListener#iteration(java.util.List)
	 */
	/** {@inheritDoc} */
	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ga.SearchListener#searchFinished(java.util.List)
	 */
	/** {@inheritDoc} */
	@Override
	public void searchFinished(GeneticAlgorithm<?> algorithm) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.ga.SearchListener#mutation(org.evosuite
	 * .ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public void modification(Chromosome individual) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder r = new StringBuilder();
		String type = getType();
		type += " :";
		type = StringUtils.rightPad(type, 24);
		r.append(type);
		r.append(getValueString());
		if (isFinished())
			r.append(" Finished!");

		return r.toString();
	}

	/**
	 * <p>getType</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getType() {
		String type = getClass().toString();
		try { // just to make sure
			type = type.substring(type.lastIndexOf(".") + 1);
		} catch (Exception e) {
		}
		// cut away "StoppingCondition" suffix
		if (type.endsWith("StoppingCondition"))
			type = type.substring(0, type.length() - 17);

		return type;
	}

	/**
	 * <p>getValueString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getValueString() {
		String value = NumberFormat.getIntegerInstance().format(getCurrentValue());
		value = StringUtils.leftPad(value, 12);
		String limit = NumberFormat.getIntegerInstance().format(getLimit());
		limit = StringUtils.rightPad(limit, 12);
		return value + " / " + limit;
	}
}
