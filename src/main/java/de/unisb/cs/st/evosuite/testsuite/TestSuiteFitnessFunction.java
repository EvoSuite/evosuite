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

package de.unisb.cs.st.evosuite.testsuite;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.dataflow.AllDefsCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.statement.StatementCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.ExecutableChromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;

/**
 * @author Gordon Fraser
 * 
 */
public abstract class TestSuiteFitnessFunction extends FitnessFunction {

	private static final long serialVersionUID = 7243635497292960457L;

	protected static Logger logger = LoggerFactory.getLogger(TestSuiteFitnessFunction.class);

	protected static TestCaseExecutor executor = TestCaseExecutor.getInstance();

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
			/*
			 * result.exceptions = executor.run(test);
			 * executor.setLogging(true); result.trace =
			 * ExecutionTracer.getExecutionTracer().getTrace();
			 */
			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);
		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			try {
				Thread.sleep(1000);
				result.setTrace(ExecutionTracer.getExecutionTracer().getTrace());
			} catch (Exception e1) {
				e.printStackTrace();
				// TODO: Do some error recovery?
				System.exit(1);
			}

		}

		// System.out.println("TG: Killed "+result.getNumKilled()+" out of "+mutants.size());
		return result;
	}

	protected static boolean hasTimeout(ExecutionResult result) {

		if (result == null) {
			return false;
		} else if (result.test == null) {
			return false;
		}
		int size = result.test.size();
		if (result.isThereAnExceptionAtPosition(size)) {
			if (result.getExceptionThrownAtPosition(size) instanceof TestCaseExecutor.TimeoutExceeded) {
				return true;
			}
		}

		return false;
	}

	protected List<ExecutionResult> runTestSuite(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		CurrentChromosomeTracker.getInstance().modification(suite);
		List<ExecutionResult> results = new ArrayList<ExecutionResult>();

		for (ExecutableChromosome chromosome : suite.getTestChromosomes()) {
			// Only execute test if it hasn't been changed
			if (chromosome.isChanged() || chromosome.getLastExecutionResult() == null) {
				ExecutionResult result = chromosome.executeForFitnessFunction(this);

				if (result != null) {
					results.add(result);

					chromosome.setLastExecutionResult(result); // .clone();
					chromosome.setChanged(false);
				}
			} else {
				results.add(chromosome.getLastExecutionResult());
			}
		}

		return results;
	}

	public static int getCoveredGoals() {

		// TODO could be done nicer for arbitrary criteria but tbh right now it
		// works for me

		switch (Properties.CRITERION) {
		case DEFUSE:
			return DefUseCoverageSuiteFitness.countMostCoveredGoals();
		case STATEMENT:
			return StatementCoverageSuiteFitness.mostCoveredGoals;
		case BRANCH:
		case EXCEPTION:
			return BranchCoverageSuiteFitness.mostCoveredGoals;
		case ALLDEFS:
			return AllDefsCoverageSuiteFitness.mostCoveredGoals;
		case MUTATION:
		case WEAKMUTATION:
		case STRONGMUTATION:
			return MutationSuiteFitness.mostCoveredGoals;
		default:
			return -1; // to indicate value is missing
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#isMaximizationFunction()
	 */
	@Override
	public boolean isMaximizationFunction() {
		return false;
	}
}
