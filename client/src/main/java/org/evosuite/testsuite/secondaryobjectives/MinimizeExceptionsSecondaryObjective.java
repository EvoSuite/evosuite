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
package org.evosuite.testsuite.secondaryobjectives;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;


/**
 * <p>MinimizeExceptionsSecondaryObjective class.</p>
 *
 * @author Gordon Fraser
 */
public class MinimizeExceptionsSecondaryObjective extends SecondaryObjective<TestSuiteChromosome> {

	private static final long serialVersionUID = -4405276303273532040L;

	private int getNumExceptions(TestSuiteChromosome chromosome) {
		int sum = 0;
		for (ExecutableChromosome test : chromosome.getTestChromosomes()) {
			if (test.getLastExecutionResult() != null)
				sum += test.getLastExecutionResult().getNumberOfThrownExceptions();
		}
		return sum;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SecondaryObjective#compareChromosomes(org.evosuite.ga.Chromosome, org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public int compareChromosomes(TestSuiteChromosome chromosome1, TestSuiteChromosome chromosome2) {
		return getNumExceptions(chromosome1) - getNumExceptions(chromosome2);
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.SecondaryObjective#compareGenerations(org.evosuite.ga.Chromosome, org.evosuite.ga.Chromosome, org.evosuite.ga.Chromosome, org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public int compareGenerations(TestSuiteChromosome parent1, TestSuiteChromosome parent2,
			TestSuiteChromosome child1, TestSuiteChromosome child2) {
		return Math.min(getNumExceptions(parent1), getNumExceptions(parent2))
		        - Math.min(getNumExceptions(child1), getNumExceptions(child2));
	}

}
