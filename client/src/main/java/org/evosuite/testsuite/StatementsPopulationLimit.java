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
package org.evosuite.testsuite;

import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.populationlimit.PopulationLimit;


/**
 * <p>StatementsPopulationLimit class.</p>
 *
 * @author fraser
 */
public class StatementsPopulationLimit implements PopulationLimit {

	private static final long serialVersionUID = 4794704248615412859L;

	/* (non-Javadoc)
	 * @see org.evosuite.ga.PopulationLimit#isPopulationFull(java.util.List)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isPopulationFull(List<? extends Chromosome> population) {
		int numStatements = 0;
		for (Chromosome chromosome : population) {
			TestSuiteChromosome suite = (TestSuiteChromosome) chromosome;
			numStatements += suite.totalLengthOfTestCases();
		}
		return numStatements >= Properties.POPULATION;
	}

}
