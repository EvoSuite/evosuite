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
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.utils.LoggingUtils;

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
		StringBuilder str = new StringBuilder();
		for (ExecutionResult result : results) {
			for (TestFitnessFunction goal : goals) {
			    if (goal.isCovered(result)) {
					n_ones++;
					str.append("1 ");
			    }
			    else
			        str.append("0 ");
			}
			str.append("\n");
		}

		double n_tests = ((double) suite.size()) + ((double) RhoCoverageFactory.getNumberTestCases());

		fitness = n_ones / n_tests / ((double) goals.size());
		fitness = Math.abs(0.5 - fitness);

		/*LoggingUtils.getEvoLogger().info("goals.size(): " + goals.size() + ", n_ones: " + n_ones + ", n_tests: " + n_tests + " | fit: " + fitness);
		LoggingUtils.getEvoLogger().info(str.toString());
		LoggingUtils.getEvoLogger().info("------");*/

		updateIndividual(this, suite, fitness);
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
