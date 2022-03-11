/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.ga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * TournamentChromosomeFactory class.
 * </p>
 *
 * @param <T>
 * @author Gordon Fraser
 */
public class TournamentChromosomeFactory<T extends Chromosome<T>> implements
        ChromosomeFactory<T> {

    private static final long serialVersionUID = -2493386206236363431L;

    private static final Logger logger = LoggerFactory.getLogger(TournamentChromosomeFactory.class);

    private final FitnessFunction<T> fitnessFunction;

    private final ChromosomeFactory<T> factory;

    private final int tournamentSize = 10;

    /**
     * <p>
     * Constructor for TournamentChromosomeFactory.
     * </p>
     *
     * @param fitness a {@link org.evosuite.ga.FitnessFunction} object.
     * @param factory a {@link org.evosuite.ga.ChromosomeFactory} object.
     */
    public TournamentChromosomeFactory(FitnessFunction<T> fitness,
                                       ChromosomeFactory<T> factory) {
        this.fitnessFunction = fitness;
        this.factory = factory;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This factory produces <i>tournamentSize</i> individuals, and returns the
     * best one
     */
    @Override
    public T getChromosome() {
        T bestIndividual = null;
        logger.debug("Starting random generation");
        for (int i = 0; i < tournamentSize; i++) {
            T candidate = factory.getChromosome();
            fitnessFunction.getFitness(candidate);
            if (bestIndividual == null) {
                bestIndividual = candidate;
            } else if (candidate.compareTo(bestIndividual) <= 0) {
                logger.debug("Old individual has fitness " + bestIndividual.getFitness(this.fitnessFunction)
                        + ", replacing with fitness " + candidate.getFitness(this.fitnessFunction));
                bestIndividual = candidate;
            }
        }
        if (bestIndividual != null)
            logger.debug("Resulting fitness: " + bestIndividual.getFitness(this.fitnessFunction));

        assert (bestIndividual != null);

        return bestIndividual;
    }
}
