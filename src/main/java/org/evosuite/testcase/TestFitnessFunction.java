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
package org.evosuite.testcase;

import java.util.List;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeRecycler;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;


/**
 * Abstract base class for fitness functions for test case chromosomes
 * 
 * @author Gordon Fraser
 * 
 */
public abstract class TestFitnessFunction extends FitnessFunction implements
        Comparable<TestFitnessFunction> {

	private static final long serialVersionUID = 5602125855207061901L;

	protected static TestCaseExecutor executor = TestCaseExecutor.getInstance();

	static boolean warnedAboutIsSimilarTo = false;

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
			result = executor.execute(test);

			int num = test.size();
			if (!result.noThrownExceptions()) {
				num = result.getFirstPositionOfThrownException();
			}
			MaxStatementsStoppingCondition.statementsExecuted(num);
			// for(TestObserver observer : observers) {
			// observer.testResult(result);
			// }
		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			logger.error("TG: Exception caught: ", e);
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
		ExecutionResult orig_result = c.getLastExecutionResult();
		if (orig_result == null || c.isChanged()) {
			orig_result = runTest(c.test);
			c.setLastExecutionResult(orig_result);
			c.setChanged(false);
		}

		double fitness = getFitness(c, orig_result);

		updateIndividual(c, fitness);

		return c.getFitness();
	}

	/**
	 * This function is used by the ChromosomeRecycler to determine whether an
	 * older TestChromosome that covered the given goal should be added to the
	 * initial population for this TestFitnessFunction
	 * 
	 * Each CoverageTestFitness can override this method in order to define when
	 * two goals are similar to each other in a way that tests covering one of
	 * them is likely to cover the other one too or is at least expected to
	 * provide a good fitness for it
	 * 
	 * If this method does not get overwritten ChromosomeRecycling obviously
	 * won't work and disabling it using Properties.recycle_chromosomes is
	 * encouraged in order to avoid unnecessary performance loss
	 */
	public boolean isSimilarTo(TestFitnessFunction goal) {
		//		if (!warnedAboutIsSimilarTo && Properties.RECYCLE_CHROMOSOMES) {
		//			logger.warn("called default TestFitness.isSimilarTo() though recycling is enabled. "
		//			        + "possible performance loss. set property recycle_chromosomes to false");
		//			warnedAboutIsSimilarTo = true;
		//		}
		return false;
	}

	/**
	 * This function is used for initial preordering of goals in the individual
	 * test case generation in TestSuiteGenerator
	 * 
	 * The idea is to search for easy goals first and reuse their
	 * TestChromosomes later when looking for harder goals that depend for
	 * example on Branches that were already covered by the easier goal.
	 * 
	 * So the general idea is that a TestFitnessFunction with a higher
	 * difficulty is concerned with CFGVertices deep down in the CDG one with a
	 * lower difficulty with vertices near the root-branch of their method.
	 * 
	 * Each CoverageTestFitness can override this method to define which goals
	 * should be searched for first (low difficulty) and which goals should be
	 * postponed initially
	 * 
	 * Disclaimer:
	 * 
	 * If this method does not get overwritten preordering of goals by
	 * difficulty obviously won't work and disabling it using
	 * Properties.preorder_goals_by_difficulty is encouraged in order to avoid
	 * unnecessary performance loss
	 * 
	 * Also the whole idea of the difficulty value is to boost the performance
	 * gain in terms of GA-evolutions the ChromosomeRecycler is supposed to
	 * achieve. So if recycling is disabled or not implemented preordering
	 * should be disabled too.
	 */
	public int getDifficulty() {
		//		if (Properties.PREORDER_GOALS_BY_DIFFICULTY)
		//			logger.warn("called default TestFitness.getDifficulty() though preordering is enabled. "
		//			        + "possible performance loss. set property preorder_goals_by_difficulty to false");
		return 0;
	}

	/**
	 * Used to preorder goals by difficulty
	 */
	@Override
	public int compareTo(TestFitnessFunction other) {
		return new Integer(getDifficulty()).compareTo(other.getDifficulty());
	}

	/**
	 * Determine if there is an existing test case covering this goal
	 * 
	 * @return
	 */
	public boolean isCovered(List<TestCase> tests) {
		for (TestCase test : tests) {
			if (isCovered(test))
				return true;
		}
		return false;
	}

	public boolean isCovered(TestCase test) {
		TestChromosome c = new TestChromosome();
		c.test = test;
		return isCovered(c);
	}

	public boolean isCovered(TestChromosome tc) {
		ExecutionResult result = tc.getLastExecutionResult();
		if (result == null || tc.isChanged()) {
			result = runTest(tc.test);
			tc.setLastExecutionResult(result);
			tc.setChanged(false);
		}

		return isCovered(tc, result);
	}

	public boolean isCovered(TestChromosome individual, ExecutionResult result) {
		boolean covered = getFitness(individual, result) == 0.0;
		if (covered) {
			ChromosomeRecycler.getInstance().testIsInterestingForGoal(individual, this);
			individual.test.addCoveredGoal(this);
		}
		return covered;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#isMaximizationFunction()
	 */
	@Override
	public boolean isMaximizationFunction() {
		return false;
	}
}
