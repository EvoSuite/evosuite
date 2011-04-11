/*
 * Copyright (C) 2010 Saarland University
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
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.util.List;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;

/**
 * Abstract base class for fitness functions for test case chromosomes
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class TestFitnessFunction extends FitnessFunction {

	protected TestCaseExecutor executor = TestCaseExecutor.getInstance();

	/**
	 * Execute a test case
	 * 
	 * @param test
	 *            The test case to execute
	 * @param mutant
	 *            The mutation to active (null = no mutation)
	 * 
	 * @return Result of the execution
	 */
	public ExecutionResult runTest(TestCase test) {

		ExecutionResult result = new ExecutionResult(test, null);

		try {
			logger.debug("Executing test");
			result = executor.execute(test);
			executor.setLogging(true);

			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);

			// for(TestObserver observer : observers) {
			// observer.testResult(result);
			// }
		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		// System.out.println("TG: Killed "+result.getNumKilled()+" out of "+mutants.size());
		return result;
	}

	public abstract double getFitness(TestChromosome individual, ExecutionResult result);

	@Override
	public double getFitness(Chromosome individual) {
		logger.trace("Executing test case on original");
		TestChromosome c = (TestChromosome) individual;
		ExecutionResult orig_result = runTest(c.test);
		double fitness = getFitness(c, orig_result);

		updateIndividual(c, fitness);

		return c.getFitness();
	}

	/**
	 * Determine if there is an existing test case covering this goal
	 * 
	 * @return
	 */
	public boolean isCovered(List<TestCase> tests) {
		for (TestCase test : tests) {
			if (isCovered(test)) {
				return true;
			}
		}
		return false;
	}

	public boolean isCovered(TestCase test) {
		ExecutionResult result = runTest(test);
		TestChromosome c = new TestChromosome();
		c.test = test;
		boolean covered = getFitness(c, result) == 0.0;
		if (covered)
			test.addCoveredGoal(this);
		return covered;
	}
}
