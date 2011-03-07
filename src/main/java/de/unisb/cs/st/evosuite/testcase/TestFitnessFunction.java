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
			//			result.exceptions = executor.run(test);
			executor.setLogging(true);
			//			result.trace = ExecutionTracer.getExecutionTracer().getTrace();
			// result.output_trace = executor.getTrace();
			/*
			 * result.comparison_trace = comparison_observer.getTrace();
			 * result.primitive_trace = primitive_observer.getTrace();
			 * result.inspector_trace = inspector_observer.getTrace();
			 * result.field_trace = field_observer.getTrace(); result.null_trace
			 * = null_observer.getTrace();
			 */

			int num = test.size();
			/*
			 * if(ex != null) { result.exception = ex;
			 * result.exception_statement = test.exception_statement; num =
			 * test.size() - test.exception_statement;
			 * 
			 * if(ex instanceof TestCaseExecutor.TimeoutExceeded) { if(mutant !=
			 * null) logger.info("Mutant timed out!"); else
			 * logger.info("Program timed out!"); resetObservers();
			 * 
			 * } }
			 */
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

		/*
		 * if(orig_result.exception != null && !ignore(orig_result.exception)) {
		 * logger.info("Test case yielded exception: "+orig_result.exception);
		 * if(c.test.statements.size() > orig_result.exception_statement)
		 * logger.
		 * info("Exception thrown in statement "+c.test.statements.get(orig_result
		 * .exception_statement).getCode()); else
		 * logger.info("Exception thrown in statement outside code ("
		 * +orig_result.exception_statement+")");
		 * c.test.chop(orig_result.exception_statement + 1); // TODO: Minimize
		 * failing test // !!!!! ONLY TEMPORALLY COMMENTED
		 * failed_tests.addTest(c.test,
		 * orig_result.exception.toString()+" in statement "
		 * +c.test.statements.get(orig_result.exception_statement).getCode());
		 * return Double.NEGATIVE_INFINITY; }
		 */

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
