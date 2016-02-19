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
package org.evosuite.coverage.path;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * <p>
 * PrimePathSuiteFitness class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class PrimePathSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 8301900778876171653L;

	List<PrimePathTestFitness> goals;

	/**
	 * <p>
	 * Constructor for PrimePathSuiteFitness.
	 * </p>
	 */
	public PrimePathSuiteFitness() {
		PrimePathCoverageFactory factory = new PrimePathCoverageFactory();
		goals = factory.getCoverageGoals();
		ExecutionTracer.enableTraceCalls();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		List<ExecutionResult> results = runTestSuite(suite);
		List<TestFitnessFunction> coveredGoals = new ArrayList<TestFitnessFunction>();
		double fitness = 0.0;

		for (TestFitnessFunction goal : goals) {
			double goalFitness = Double.MAX_VALUE;
			for (ExecutionResult result : results) {
				TestChromosome tc = new TestChromosome();
				tc.setTestCase(result.test);
				double resultFitness = goal.getFitness(tc, result);
				if (resultFitness < goalFitness)
					goalFitness = resultFitness;
				if (goalFitness == 0.0) {
					result.test.addCoveredGoal(goal);
					coveredGoals.add(goal);
					break;
				}
			}
			fitness += goalFitness;
		}
		suite.setCoverage(this, coveredGoals.size() / (double) goals.size());
		updateIndividual(this, suite, fitness);
		return fitness;
	}
}
