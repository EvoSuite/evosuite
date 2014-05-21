/**
 * Copyright (C) 2011,2012,2013,2014 Gordon Fraser, Andrea Arcuri, José Campos
 * and EvoSuite contributors
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
package org.evosuite.coverage.rho;

import java.util.List;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * <p>
 * RhoCoverageSuiteFitness class.
 * </p>
 * 
 * @author José Campos
 */
public class RhoCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 9062499323967883418L;

	/**
	 * 
	 */
	@Override
	public double getFitness(
			AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {

		List<ExecutionResult> results = runTestSuite(suite);
		List<? extends TestFitnessFunction> goals = (List<? extends TestFitnessFunction>) RhoCoverageFactory.retrieveCoverageGoals();
		double fitness = 0.0;

		double n_ones = (double) RhoCoverageFactory.getNumberOnes();
		for (ExecutionResult result : results) {
			TestChromosome tc = new TestChromosome();
			tc.setTestCase(result.test);

			for (TestFitnessFunction goal : goals) {
				if (goal.getFitness(tc, result) == 0.0)
					n_ones++;
			}
		}

		double n_tests = ((double) suite.size()) + ((double) RhoCoverageFactory.getNumberTestCases());

		fitness = n_ones / n_tests / ((double) RhoCoverageFactory.getNumberComponents());
		fitness = Math.abs(0.5 - fitness);

		updateIndividual(suite, fitness);
		return fitness;
	}

	/**
	 * 
	 */
	@Override
	public boolean isMaximizationFunction() {
		return false;
	}
}
