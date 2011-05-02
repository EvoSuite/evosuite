/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.BloatControlFunction;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.SearchListener;

/**
 * Bloat control that checks an individual against the best test case
 * 
 * @author Gordon Fraser
 * 
 */
public class RelativeLengthBloatControl implements BloatControlFunction, SearchListener {

	protected int current_max = 0;

	protected double best_fitness = Double.MAX_VALUE; // FIXXME: Assuming
	                                                  // minimizing fitness!

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.javalanche.ga.BloatControlFunction#isTooLong(de.unisb.
	 * cs.st.javalanche.ga.Chromosome)
	 */
	@Override
	public boolean isTooLong(Chromosome chromosome) {
		// Always accept if fitness is better
		if (chromosome.getFitness() < best_fitness)
			return false;

		// logger.debug("Current - max: "+((TestSuiteChromosome)chromosome).length()+" - "+current_max);
		if (current_max > 0) {
			// if(((TestSuiteChromosome)chromosome).length() > bloat_factor *
			// current_max)
			// logger.debug("Bloat control: "+((TestSuiteChromosome)chromosome).length()
			// +" > "+ bloat_factor * current_max);

			return ((TestChromosome) chromosome).size() > Properties.BLOAT_FACTOR
			        * current_max;
		} else
			return false; // Don't know max length so can't reject!
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.javalanche.ga.SearchListener#fitnessEvaluation(de.unisb
	 * .cs.st.javalanche.ga.Chromosome)
	 */
	@Override
	public void fitnessEvaluation(Chromosome result) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.javalanche.ga.SearchListener#iteration(de.unisb.cs.st.
	 * javalanche.ga.Chromosome)
	 */
	@Override
	public void iteration(List<Chromosome> population) {
		current_max = ((TestChromosome) population.get(0)).size();
		best_fitness = population.get(0).getFitness();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.javalanche.ga.SearchListener#searchFinished(de.unisb.cs
	 * .st.javalanche.ga.Chromosome)
	 */
	@Override
	public void searchFinished(List<Chromosome> best) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.javalanche.ga.SearchListener#searchStarted(de.unisb.cs
	 * .st.javalanche.ga.FitnessFunction)
	 */
	@Override
	public void searchStarted(FitnessFunction objective) {
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
}
