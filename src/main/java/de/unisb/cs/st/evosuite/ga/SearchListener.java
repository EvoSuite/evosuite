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


/**
 * A listener that can be attached to the search
 * 
 * @author Gordon Fraser
 * 
 */
public interface SearchListener {

	/**
	 * Called when a new search is started
	 * 
	 * @param objective
	 *            - the fitness function of the search
	 */
	public void searchStarted(GeneticAlgorithm algorithm);

	/**
	 * Called after each iteration of the search
	 * 
	 * @param population
	 */
	public void iteration(GeneticAlgorithm algorithm);

	/**
	 * Called after the last iteration
	 * 
	 * @param population
	 */
	public void searchFinished(GeneticAlgorithm algorithm);

	/**
	 * Called after every single fitness evaluation
	 * 
	 * @param individual
	 */
	public void fitnessEvaluation(Chromosome individual);

	/**
	 * Called before a chromosome is mutated
	 * 
	 * @param individual
	 */
	public void modification(Chromosome individual);

}
