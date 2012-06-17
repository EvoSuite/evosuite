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
package de.unisb.cs.st.evosuite.testsuite;

import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * Historical concrete TestFitnessFactories only implement the getGoals() method of TestFitnessFactory.
 * Those old Factories can just extend these AstractFitnessFactory to support the new method getFitness()
 * @author Sebastian Steenbuck
 *
 */
public abstract class AbstractFitnessFactory implements TestFitnessFactory{

	/**
	 * A concrete factory can store the time consumed to initially compute all
	 * coverage goals in this field in order to track this information in
	 * SearchStatistics.
	 */
	public static long goalComputationTime = 0l;
	
	@Override
	public double getFitness(TestSuiteChromosome suite){
		
		ExecutionTrace.enableTraceCalls();

		int coveredGoals = 0;
		for (TestFitnessFunction goal : getCoverageGoals()) {
			for (TestChromosome test : suite.getTestChromosomes()) {
				if (goal.isCovered(test)) {
					coveredGoals++;
					break;
				}
			}
		}

		ExecutionTrace.disableTraceCalls();

		return getCoverageGoals().size()-coveredGoals;
	}
}
