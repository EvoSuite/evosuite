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


/**
 * Check individuals against current best
 * 
 * @author Gordon Fraser
 *
 */
public class RelativeSizeBloatControl implements BloatControlFunction, SearchListener {

	/**
	 * Longest individual in current generation
	 */
	protected int current_max = 0;

	/**
	 * Reject individuals that are larger than twice the length of the current longest individual
	 */
	public boolean isTooLong(Chromosome chromosome) {
		if(current_max > 0)
			return chromosome.size() > 2 * current_max;
		else
			return false; // Don't know max length!
				
	}

	/**
	 * Set current max length to max of best chromosome
	 */
	public void iteration(List<Chromosome> population) {
		current_max = population.get(0).size(); // FIXME: 2 Assumptions: Population is sorted and population is non-empty...
	}

	public void searchFinished(List<Chromosome> population) {
	}

	public void searchStarted(FitnessFunction function) {
	}

	public void fitnessEvaluation(Chromosome individual) {
		// ignore
	}
}
