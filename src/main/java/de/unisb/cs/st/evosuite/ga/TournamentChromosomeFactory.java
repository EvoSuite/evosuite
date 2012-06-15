/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
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
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.ga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * @param <T>
 * 
 */
public class TournamentChromosomeFactory<T extends Chromosome> implements
        ChromosomeFactory<T> {

	private static final long serialVersionUID = -2493386206236363431L;

	private static Logger logger = LoggerFactory.getLogger(TournamentChromosomeFactory.class);

	private final FitnessFunction fitnessFunction;

	private final ChromosomeFactory<T> factory;

	private final int tournamentSize = 10;

	public TournamentChromosomeFactory(FitnessFunction fitness,
	        ChromosomeFactory<T> factory) {
		this.fitnessFunction = fitness;
		this.factory = factory;
	}

	/**
	 * This factory produces <i>tournamentSize</i> individuals, and returns the
	 * best one
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T getChromosome() {
		Chromosome bestIndividual = null;
		logger.debug("Starting random generation");
		for (int i = 0; i < tournamentSize; i++) {
			Chromosome candidate = factory.getChromosome();
			fitnessFunction.getFitness(candidate);
			if (bestIndividual == null) {
				bestIndividual = candidate;
			} else if (candidate.compareTo(bestIndividual) <= 0) {
				logger.debug("Old individual has fitness " + bestIndividual.getFitness()
				        + ", replacing with fitness " + candidate.getFitness());
				bestIndividual = candidate;
			}
		}
		if (bestIndividual != null)
			logger.debug("Resulting fitness: " + bestIndividual.getFitness());

		assert (bestIndividual != null);

		return (T) bestIndividual;
	}
}
