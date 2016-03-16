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
package org.evosuite.coverage.statement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

public class StatementCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -4479582777935260157L;


	/** {@inheritDoc} */
	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		List<ExecutionResult> results = runTestSuite(suite);
		double fitness = 0.0;

		// first simple and naive idea: 
		//  just take each goal, calculate the minimal fitness over all results in the suite
		//  once a goal is covered don't check for it again
		//  in the end sum up all those fitness and it's the resulting suite-fitness

		// guess this is horribly inefficient but it's a start
		List<? extends TestFitnessFunction> totalGoals = StatementCoverageFactory.retrieveCoverageGoals();
		Set<TestFitnessFunction> coveredGoals = new HashSet<TestFitnessFunction>();

		for (TestFitnessFunction goal : totalGoals) {
			double goalFitness = Double.MAX_VALUE;
			for (ExecutionResult result : results) {
				TestChromosome tc = new TestChromosome();
				tc.setTestCase(result.test);
				double resultFitness = goal.getFitness(tc, result);
				if (resultFitness < goalFitness)
					goalFitness = resultFitness;
				if (goalFitness == 0.0) {
					//					result.test.addCoveredGoal(goal);
					coveredGoals.add(goal);
					break;
				}
			}
			fitness += goalFitness;
		}

		if (totalGoals.size() > 0)
			suite.setCoverage(this, coveredGoals.size() / (double) totalGoals.size());
		else
			suite.setCoverage(this, 1.0);

		suite.setNumOfCoveredGoals(this, coveredGoals.size());
		
		updateIndividual(this, suite, fitness);

		return fitness;
	}

}
