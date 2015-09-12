/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.regression;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author Gordon Fraser
 * 
 */
public class RegressionTestSuiteChromosome extends
        AbstractTestSuiteChromosome<RegressionTestChromosome> {

	private static final long serialVersionUID = 2279207996777829420L;

	public RegressionTestSuiteChromosome() {
		super();
	}

	/**
	 * <p>
	 * Constructor for RegressionTestSuiteChromosome.
	 * </p>
	 * 
	 * @param testChromosomeFactory
	 *            a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	public RegressionTestSuiteChromosome(
	        ChromosomeFactory<RegressionTestChromosome> testChromosomeFactory) {
		super(testChromosomeFactory);
	}

	/**
	 * <p>
	 * Constructor for TestSuiteChromosome.
	 * </p>
	 * 
	 * @param source
	 *            a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
	 */
	protected RegressionTestSuiteChromosome(RegressionTestSuiteChromosome source) {
		super(source);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testsuite.AbstractTestSuiteChromosome#localSearch(org.evosuite.ga.LocalSearchObjective)
	 */
	@Override
	public boolean localSearch(LocalSearchObjective objective) {
		// Ignore for now
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testsuite.AbstractTestSuiteChromosome#clone()
	 */
	@Override
	public Chromosome clone() {
		return new RegressionTestSuiteChromosome(this);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.Chromosome#compareSecondaryObjective(org.evosuite.ga.Chromosome)
	 */
	@Override
	public int compareSecondaryObjective(Chromosome o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
