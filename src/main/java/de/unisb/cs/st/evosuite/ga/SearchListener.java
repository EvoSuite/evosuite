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
 * A listener that can be attached to the search
 * 
 * @author Gordon Fraser
 *
 */
public interface SearchListener {

	/**
	 * Called when a new search is started
	 * @param objective - the fitness function of the search
	 */
	public void searchStarted(FitnessFunction objective);
	
	/**
	 * Called after each iteration of the search
	 * @param population
	 */
	public void iteration(List<Chromosome> population);
	
	/**
	 * Called after the last iteration
	 * @param population
	 */
	public void searchFinished(List<Chromosome> population);
	
	/**
	 * Called for every single fitness evaluation
	 * @param individual
	 */
	public void fitnessEvaluation(Chromosome individual);
}
