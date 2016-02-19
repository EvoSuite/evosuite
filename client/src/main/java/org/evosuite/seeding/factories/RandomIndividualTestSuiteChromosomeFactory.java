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
/**
 * 
 */
package org.evosuite.seeding.factories;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.Randomness;

/**
 * @author Thomas White
 */
public class RandomIndividualTestSuiteChromosomeFactory implements
		ChromosomeFactory<TestSuiteChromosome> {

	private static final long serialVersionUID = 1L;

	private final ChromosomeFactory<TestSuiteChromosome> defaultFactory;
	private final GeneticAlgorithm<TestSuiteChromosome> geneticAlgorithm;

	/**
	 * <p>
	 * Constructor for JUnitTestSuiteChromosomeFactory.
	 * </p>
	 * 
	 * @param defaultFactory
	 *            a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	public RandomIndividualTestSuiteChromosomeFactory(
			ChromosomeFactory<TestSuiteChromosome> defaultFactory,
			GeneticAlgorithm<TestSuiteChromosome> geneticAlgorithm) {
		this.defaultFactory = defaultFactory;
		this.geneticAlgorithm = geneticAlgorithm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.evosuite.ga.ChromosomeFactory#getChromosome()
	 */
	/** {@inheritDoc} */
	@Override
	public TestSuiteChromosome getChromosome() {
		/*
		 * double P_delta = 0.1d; double P_clone = 0.1d; int MAX_CHANGES = 10;
		 */
		if (geneticAlgorithm != null && Randomness.nextDouble() < Properties.SEED_PROBABILITY) {
			int populationSize = geneticAlgorithm.getPopulation().size();
			TestSuiteChromosome ri = geneticAlgorithm.getPopulation().get(Randomness.nextInt(populationSize));
			if (ri != null){
				return ri;
			}
		}

		return defaultFactory.getChromosome();
	}

}
