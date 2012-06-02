/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
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
package de.unisb.cs.st.evosuite.testsuite;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.SecondaryObjective;

/**
 * @author Gordon Fraser
 * 
 */
public class MinimizeTotalLengthSecondaryObjective extends SecondaryObjective {

	private static final long serialVersionUID = 1974099736891048617L;

	private int getLengthSum(TestSuiteChromosome chromosome1,
	        TestSuiteChromosome chromosome2) {
		return chromosome1.totalLengthOfTestCases() + chromosome2.totalLengthOfTestCases();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.SecondaryObjective#compareChromosomes(de.unisb
	 * .cs.st.evosuite.ga.Chromosome, de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public int compareChromosomes(Chromosome chromosome1, Chromosome chromosome2) {
		logger.debug("Comparing lengths: " + ((AbstractTestSuiteChromosome<?>) chromosome1).totalLengthOfTestCases()
		        + " vs " + ((AbstractTestSuiteChromosome<?>) chromosome2).totalLengthOfTestCases());

		return ((AbstractTestSuiteChromosome<?>) chromosome1).totalLengthOfTestCases()
		        - ((AbstractTestSuiteChromosome<?>) chromosome2).totalLengthOfTestCases();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.SecondaryObjective#compareGenerations(de.unisb
	 * .cs.st.evosuite.ga.Chromosome, de.unisb.cs.st.evosuite.ga.Chromosome,
	 * de.unisb.cs.st.evosuite.ga.Chromosome,
	 * de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public int compareGenerations(Chromosome parent1, Chromosome parent2,
	        Chromosome child1, Chromosome child2) {
		logger.debug("Comparing lengths: " + ((TestSuiteChromosome) parent1).totalLengthOfTestCases()
		        + ", " + ((TestSuiteChromosome) parent2).totalLengthOfTestCases() + " vs "
		        + ((TestSuiteChromosome) child1).totalLengthOfTestCases() + ", "
		        + ((TestSuiteChromosome) child2).totalLengthOfTestCases());
		return getLengthSum((TestSuiteChromosome) parent1, (TestSuiteChromosome) parent2)
		        - getLengthSum((TestSuiteChromosome) child1, (TestSuiteChromosome) child2);
	}

}
