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
package org.evosuite.coverage.ibranch;

import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * <p>
 * IBranchSecondaryObjective class.
 * </p>
 *
 * @author mattia
 */
public class IBranchSecondaryObjective extends SecondaryObjective<TestSuiteChromosome> {

	//Ibranch fitness
	private IBranchSuiteFitness ff;
	private static final long serialVersionUID = 7211557650429998223L;

	public IBranchSecondaryObjective() {
		ff = new IBranchSuiteFitness();
	}
	 
	@Override
	public int compareChromosomes(
			TestSuiteChromosome chromosome1,
			TestSuiteChromosome chromosome2) {
				
		double fitness1 = ff.getFitness(chromosome1, false);
		double fitness2 = ff.getFitness(chromosome2, false);
		int i = (int) Math.signum(fitness1 - fitness2);
//		if (!chromosome1.hasExecutedFitness(ff) || chromosome1.isChanged())
//			ff.getFitness(chromosome1);
//		if (!chromosome2.hasExecutedFitness(ff) || chromosome2.isChanged())
//			ff.getFitness(chromosome2);
		ff.updateCoveredGoals();
		return i; 
	}

	@Override
	public int compareGenerations(
			TestSuiteChromosome parent1,
			TestSuiteChromosome parent2,
			TestSuiteChromosome child1,
			TestSuiteChromosome child2) {
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
