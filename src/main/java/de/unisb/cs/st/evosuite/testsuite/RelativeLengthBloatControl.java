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

package de.unisb.cs.st.evosuite.testsuite;

import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.BloatControlFunction;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.SearchListener;

/**
 * @author Gordon Fraser
 * 
 */
public class RelativeLengthBloatControl implements BloatControlFunction, SearchListener {

	Logger logger = Logger.getLogger(BloatControlFunction.class);

	/**
	 * Longest individual in current generation
	 */
	protected int current_max = 0;

	protected double best_fitness = Double.MAX_VALUE; // FIXXME: Assuming
	                                                  // minimizing fitness!

	/**
	 * Reject individuals that are larger than twice the length of the current
	 * best individual
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

			return ((TestSuiteChromosome) chromosome).length() > Properties.BLOAT_FACTOR
			        * current_max;
		} else
			return false; // Don't know max length so can't reject!

	}

	/**
	 * Set current max length to max of best chromosome
	 */
	@Override
	public void iteration(List<Chromosome> population) {
		Chromosome best = population.get(0);
		current_max = ((TestSuiteChromosome) best).length();
		best_fitness = best.getFitness();
	}

	@Override
	public void searchFinished(List<Chromosome> result) {
	}

	@Override
	public void searchStarted(FitnessFunction objective) {
	}

	@Override
	public void fitnessEvaluation(Chromosome result) {
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
