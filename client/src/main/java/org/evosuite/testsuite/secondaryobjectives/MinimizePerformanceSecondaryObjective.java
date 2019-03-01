/**
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
package org.evosuite.testsuite.secondaryobjectives;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * <p>MinimizePerformanceSecondaryObjective class.</p>
 *
 * @author Annibale Panichella
 */
public class MinimizePerformanceSecondaryObjective extends SecondaryObjective<TestSuiteChromosome> {

	private static final long serialVersionUID = -6272641645062817112L;

	private double getOverallPerformanceScore(TestSuiteChromosome chromosome) {
		double sum = 0.0;
		for (TestChromosome tch : chromosome.getTestChromosomes()){
			sum += tch.getPerformanceScore();
		}
		return sum;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.evosuite.testcase.secondaryobjectives.SecondaryObjective#compareChromosomes(org.evosuite.ga.Chromosome,
	 * org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public int compareChromosomes(TestSuiteChromosome chromosome1, TestSuiteChromosome chromosome2) {
		return (int) Math.signum(getOverallPerformanceScore(chromosome1)
		        - getOverallPerformanceScore(chromosome2));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.evosuite.testcase.secondaryobjectives.SecondaryObjective#compareGenerations(org.evosuite.ga.Chromosome,
	 * org.evosuite.ga.Chromosome, org.evosuite.ga.Chromosome, org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public int compareGenerations(TestSuiteChromosome parent1, TestSuiteChromosome parent2,
			TestSuiteChromosome child1, TestSuiteChromosome child2) {
		return (int) Math.signum(Math.min(getOverallPerformanceScore(parent1),
		                                  getOverallPerformanceScore(parent2))
		        - Math.min(getOverallPerformanceScore(child1), getOverallPerformanceScore(child2)));
	}

}
