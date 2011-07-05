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

package de.unisb.cs.st.evosuite.mutation;

import java.util.List;

import de.unisb.cs.st.evosuite.assertion.ComparisonTraceObserver;
import de.unisb.cs.st.evosuite.assertion.InspectorTraceObserver;
import de.unisb.cs.st.evosuite.assertion.NullOutputObserver;
import de.unisb.cs.st.evosuite.assertion.PrimitiveFieldTraceObserver;
import de.unisb.cs.st.evosuite.assertion.PrimitiveOutputTraceObserver;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.mutation.HOM.HOMObserver;
import de.unisb.cs.st.evosuite.mutation.HOM.HOMSwitcher;
import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

/**
 * @author Gordon Fraser
 * 
 */
public class MutationSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 8198712273315903693L;

	private final List<TestFitnessFunction> goals;

	private final HOMSwitcher hom_switcher = new HOMSwitcher();

	protected List<ExecutionObserver> observers;

	protected PrimitiveOutputTraceObserver primitive_observer = new PrimitiveOutputTraceObserver();
	protected ComparisonTraceObserver comparison_observer = new ComparisonTraceObserver();
	protected InspectorTraceObserver inspector_observer = new InspectorTraceObserver();
	protected PrimitiveFieldTraceObserver field_observer = new PrimitiveFieldTraceObserver();
	protected NullOutputObserver null_observer = new NullOutputObserver();

	public MutationSuiteFitness() {
		MutationGoalFactory factory = new MutationGoalFactory();
		goals = factory.getCoverageGoals();
		executor.addObserver(primitive_observer);
		executor.addObserver(comparison_observer);
		executor.addObserver(inspector_observer);
		executor.addObserver(field_observer);
		executor.addObserver(null_observer);
	}

	public int getNumGoals() {
		return goals.size();
	}

	@Override
	public ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

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
	public ExecutionResult runTest(TestCase test, Mutation mutant) {

		ExecutionResult result = new ExecutionResult(test, mutant);
		try {
			logger.debug("Executing test");
			HOMObserver.resetTouched(); // TODO - is this the right place?
			if (mutant != null) {
				hom_switcher.switchOn(mutant);
			}

			result = executor.execute(test);

			if (mutant != null)
				hom_switcher.switchOff(mutant);

			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);
			result.touched.addAll(HOMObserver.getTouched());

			result.comparison_trace = comparison_observer.getTrace();
			result.primitive_trace = primitive_observer.getTrace();
			result.inspector_trace = inspector_observer.getTrace();
			result.field_trace = field_observer.getTrace();
			result.null_trace = null_observer.getTrace();

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.FitnessFunction#getFitness(de.unisb.cs.st.
	 * evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(Chromosome individual) {

		double fitness = 0.0;
		// execute test on original
		TestSuiteChromosome suite = (TestSuiteChromosome) individual;
		List<ExecutionResult> results = runTestSuite(suite);

		for (ExecutionResult result : results) {
			TestChromosome chromosome = new TestChromosome();
			chromosome.setTestCase(result.test);
			for (TestFitnessFunction goal : goals) {
				// TODO: Only execute test again if mutant is covered
				if (!MutationTimeoutStoppingCondition.isDisabled(((MutationTestFitness) goal).getTargetMutation()))
					fitness += goal.getFitness(chromosome, result);
				else
					logger.debug("Skipping timed out mutation");
			}
		}

		return fitness;
	}
}
