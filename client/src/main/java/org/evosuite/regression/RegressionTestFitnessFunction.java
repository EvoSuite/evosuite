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
package org.evosuite.regression;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;

/**
 * @author Gordon Fraser
 * 
 */
public class RegressionTestFitnessFunction extends
        FitnessFunction<RegressionTestChromosome> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1230123206788990137L;

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(RegressionTestChromosome regressionTest) {
		// Measure something based on the two results!
		assert false : "Should not be reaching here...";
		return 0;
	}

	/**
	 * Execute a test case
	 * 
	 * @param test
	 *            The test case to execute
	 * @return Result of the execution
	 */
	public ExecutionResult runTest(TestChromosome testChromosome) {
		if (testChromosome.getLastExecutionResult() != null
		        && !testChromosome.isChanged()) {
			return testChromosome.getLastExecutionResult();
		}

		TestCase test = testChromosome.getTestCase();
		ExecutionResult result = new ExecutionResult(test, null);

		try {
			result = TestCaseExecutor.getInstance().execute(test);

			int num = test.size();
			if (!result.noThrownExceptions()) {
				num = result.getFirstPositionOfThrownException();
			}
			MaxStatementsStoppingCondition.statementsExecuted(num);
			// for(TestObserver observer : observers) {
			// observer.testResult(result);
			// }
		} catch (Exception e) {
			logger.error("TG: Exception caught: ", e);
			throw new RuntimeException(e);
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#isMaximizationFunction()
	 */
	@Override
	public boolean isMaximizationFunction() {
		return false;
	}

}
