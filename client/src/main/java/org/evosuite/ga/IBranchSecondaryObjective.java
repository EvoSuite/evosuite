/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * <p>
 * IBranchSecondaryObjective class.
 * </p>
 *
 * @author mattia
 */
public class IBranchSecondaryObjective extends
		SecondaryObjective<AbstractTestSuiteChromosome<? extends ExecutableChromosome>> {

	//Ibranch fitness
	private TestSuiteFitnessFunction ff;
	private static final long serialVersionUID = 7211557650429998223L;

	public IBranchSecondaryObjective(TestSuiteFitnessFunction fitness) {
		ff = fitness;
	}
	 
	@Override
	public int compareChromosomes(
			AbstractTestSuiteChromosome<? extends ExecutableChromosome> chromosome1,
			AbstractTestSuiteChromosome<? extends ExecutableChromosome> chromosome2) {
		
		if (!chromosome1.hasExecutedFitness(ff) || chromosome1.isChanged())
			ff.getFitness(chromosome1);
		if (!chromosome1.hasExecutedFitness(ff) || chromosome2.isChanged())
			ff.getFitness(chromosome2);

		logger.debug("Comparing sizes: " + chromosome1.getFitness(ff) + " vs "
				+ chromosome2.getFitness(ff));
		int i = (int) Math.signum(chromosome1.getFitness(ff) - chromosome2.getFitness(ff));
		ff.updateCoveredGoals();
		return i; 
	}

	@Override
	public int compareGenerations(
			AbstractTestSuiteChromosome<? extends ExecutableChromosome> parent1,
			AbstractTestSuiteChromosome<? extends ExecutableChromosome> parent2,
			AbstractTestSuiteChromosome<? extends ExecutableChromosome> child1,
			AbstractTestSuiteChromosome<? extends ExecutableChromosome> child2) {
		logger.debug("Comparing sizes: " + parent1.size() + ", " + parent1.size() + " vs "
				+ child1.size() + ", " + child2.size());
		if (!parent1.hasExecutedFitness(ff) ||parent1.isChanged())
			ff.getFitness(parent1);
		if (!parent2.hasExecutedFitness(ff) ||parent2.isChanged())
			ff.getFitness(parent2);
		if (!child1.hasExecutedFitness(ff) ||child1.isChanged())
			ff.getFitness(child1);
		if (!child2.hasExecutedFitness(ff) ||child2.isChanged())
			ff.getFitness(child2);

		double minParents = Math.min(parent1.getFitness(ff), parent2.getFitness(ff));
		double minChildren = Math.min(child1.getFitness(ff), child2.getFitness(ff));
		if (minParents < minChildren) {
			return -1;
		}
		if (minParents > minChildren) {
			return 1;
		}
		return 0;
	}

}
