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
package org.evosuite.coverage.mutation;

import org.evosuite.assertion.*;
import org.evosuite.coverage.TestCoverageGoal;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.TestCaseExecutor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * <p>
 * StrongMutationTestFitness class.
 * </p>
 * 
 * @author fraser
 */
public class StrongMutationTestFitness extends MutationTestFitness {

	private static final long serialVersionUID = -262199037689935052L;

	/** Constant <code>observerClasses</code> */
	protected static Class<?>[] observerClasses = { PrimitiveTraceEntry.class,
	        ComparisonTraceEntry.class, InspectorTraceEntry.class,
	        PrimitiveFieldTraceEntry.class, NullTraceEntry.class, ArrayTraceEntry.class };

	/** Constant <code>observers</code> */
	protected static AssertionTraceObserver<?>[] observers = {
	        new PrimitiveTraceObserver(), new ComparisonTraceObserver(),
	        new InspectorTraceObserver(), new PrimitiveFieldTraceObserver(),
	        new NullTraceObserver(), new ArrayTraceObserver() };

	/**
	 * <p>
	 * Constructor for StrongMutationTestFitness.
	 * </p>
	 * 
	 * @param mutation
	 *            a {@link org.evosuite.coverage.mutation.Mutation} object.
	 */
	public StrongMutationTestFitness(Mutation mutation) {
		super(mutation);
		for (AssertionTraceObserver<?> observer : observers) {
			logger.debug("StrongMutation adding observer " + observer);
			TestCaseExecutor.getInstance().addObserver(observer);
		}
	}

	/** {@inheritDoc} */
	@Override
	public ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

	/** {@inheritDoc} */
	public static ExecutionResult runTest(TestCase test, Mutation mutant) {

		ExecutionResult result = new ExecutionResult(test, mutant);

		try {
			if (mutant != null)
				logger.debug("Executing test for mutant " + mutant.getId() + ": \n"
				        + test.toCode());
			else
				logger.debug("Executing test witout mutant");

			if (mutant != null)
				MutationObserver.activateMutation(mutant);
			result = TestCaseExecutor.getInstance().execute(test);
			if (mutant != null)
				MutationObserver.deactivateMutation(mutant);

			int num = test.size();
			if (!result.noThrownExceptions()) {
				num = result.getFirstPositionOfThrownException();
			}

			//if (mutant == null)
			MaxStatementsStoppingCondition.statementsExecuted(num);
			int i = 0;
			for (AssertionTraceObserver<?> observer : observers) {
				result.setTrace(observer.getTrace(), observerClasses[i++]);
			}

		} catch (Exception e) {
			throw new Error(e);
		}

		return result;
	}

	private MutationExecutionResult getMutationResult(ExecutionResult originalResult,
	        ExecutionResult mutationResult) {

		MutationExecutionResult result = new MutationExecutionResult();

		if (TestCoverageGoal.hasTimeout(mutationResult)) {
			logger.debug("Found timeout in mutant!");
			MutationTimeoutStoppingCondition.timeOut(mutation);
			result.setHasTimeout(true);
		}

		if (!originalResult.noThrownExceptions()) {
			if (mutationResult.noThrownExceptions())
				result.setHasTimeout(true);
		}

		int numAssertions = getNumAssertions(originalResult, mutationResult);
		result.setNumAssertions(numAssertions);

		if (numAssertions == 0) {
			double impact = getSumDistance(originalResult.getTrace(),
			                               mutationResult.getTrace());
			result.setImpact(impact);
		}
		return result;
	}

	private Set<String> getDifference(
	        Map<String, Map<String, Map<Integer, Integer>>> orig,
	        Map<String, Map<String, Map<Integer, Integer>>> mutant) {
		Map<String, Set<String>> handled = new HashMap<String, Set<String>>();
		Set<String> differ = new HashSet<String>();

		for (Entry<String, Map<String, Map<Integer, Integer>>> entry : orig.entrySet()) {
			if (!handled.containsKey(entry.getKey()))
				handled.put(entry.getKey(), new HashSet<String>());

			for (Entry<String, Map<Integer, Integer>> method_entry : entry.getValue().entrySet()) {
				if (!mutant.containsKey(entry.getKey())) {
					// Class was not executed on mutant, so add method
					logger.debug("Found class difference: " + entry.getKey());
					differ.add(entry.getKey());
				} else {
					// Class was also executed on mutant

					if (!mutant.get(entry.getKey()).containsKey(method_entry.getKey())) {
						// Method was not executed on mutant, so add method
						logger.debug("Found method difference: " + method_entry.getKey());
						differ.add(entry.getKey() + "." + method_entry.getKey());
					} else {
						// Method was executed on mutant
						for (Entry<Integer, Integer> line_entry : method_entry.getValue().entrySet()) {
							if (!mutant.get(entry.getKey()).get(method_entry.getKey()).containsKey(line_entry.getKey())) {
								// Line was not executed on mutant, so add
								logger.debug("Found line difference: "
								        + line_entry.getKey() + ": "
								        + line_entry.getValue());
								differ.add(entry.getKey() + "." + method_entry.getKey()
								        + ":" + line_entry.getKey());
							} else {
								if (!mutant.get(entry.getKey()).get(method_entry.getKey()).get(line_entry.getKey()).equals(line_entry.getValue())) {
									// Line coverage differs, so add
									differ.add(entry.getKey() + "."
									        + method_entry.getKey() + ":"
									        + line_entry.getKey());
									logger.debug("Found line difference: "
									        + line_entry.getKey() + ": "
									        + line_entry.getValue());
								}
							}
						}
						if (!method_entry.getValue().equals(mutant.get(entry.getKey()).get(method_entry.getKey()))) {
							differ.add(entry.getKey() + "." + method_entry.getKey());

							logger.debug("Found other difference: " + entry.getKey());
							// logger.info("Coverage difference on : "+entry.getKey()+":"+method_entry.getKey());
						}
					}
				}
			}
		}

		return differ;
	}

	/**
	 * Compare two coverage maps
	 * 
	 * @param orig
	 * @param mutant
	 * @return unique number of methods with coverage difference
	 */
	private int getCoverageDifference(
	        Map<String, Map<String, Map<Integer, Integer>>> orig,
	        Map<String, Map<String, Map<Integer, Integer>>> mutant) {
		Set<String> differ = getDifference(orig, mutant);
		differ.addAll(getDifference(mutant, orig));
		return differ.size();
	}

	private double getSumDistance(ExecutionTrace orig_trace, ExecutionTrace mutant_trace) {

		// TODO: Also sum up differences in branch distances as part of impact!

		// double sum = getCoverageDifference(getCoverage(orig_trace),
		// getCoverage(mutant_trace));
		logger.debug("Calculating coverage impact");
		double coverage_impact = getCoverageDifference(orig_trace.getCoverageData(),
		                                               mutant_trace.getCoverageData());
		logger.debug("Coverage impact: " + coverage_impact);
		logger.debug("Calculating data impact");
		double data_impact = getCoverageDifference(orig_trace.getReturnData(),
		                                           mutant_trace.getReturnData());
		logger.debug("Data impact: " + data_impact);

		double branch_impact = 0.0;
		for (Integer predicate : orig_trace.getCoveredPredicates()) {
			if (mutant_trace.hasTrueDistance(predicate)) {
				branch_impact += normalize(Math.abs(orig_trace.getTrueDistance(predicate)
				        - mutant_trace.getTrueDistance(predicate)));
			} else {
				branch_impact += 1.0;
			}
			if (mutant_trace.hasFalseDistance(predicate)) {
				branch_impact += normalize(Math.abs(orig_trace.getFalseDistance(predicate)
				        - mutant_trace.getFalseDistance(predicate)));
			} else {
				branch_impact += 1.0;
			}
		}
		logger.debug("Branch impact: " + branch_impact);

		return normalize(coverage_impact) + normalize(data_impact) + branch_impact;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private int getNumAssertions(ExecutionResult origResult,
	        ExecutionResult mutant_result) {
		int num = 0;
		if (origResult.test.size() == 0) {
			logger.debug("Orig test is empty?");
			return 0;
		}

		for (Class<?> observerClass : observerClasses) {
			OutputTrace trace = mutant_result.getTrace(observerClass);
			OutputTrace orig = origResult.getTrace(observerClass);

			if (orig == null) {
				String msg = "No trace for " + observerClass + ". Traces: ";
				for (OutputTrace t : origResult.getTraces())
					msg += " " + t.toString();
				logger.error(msg);
			} else {
				num += orig.numDiffer(trace);
			}
		}

		logger.debug("Found " + num + " assertions!");
		return num;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getFitness(org.evosuite.testcase.TestChromosome, org.evosuite.testcase.ExecutionResult)
	 */
	/** {@inheritDoc} */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {

		// impact (0..1)
		// asserted: 0/1
		//

		// If not touched, fitness = branchcoveragefitnesses + 2

		// If executed, fitness = normalize(constraint distance) + asserted_yes_no

		// If infected, check impact?

		double fitness = 0.0;

		double executionDistance = diameter;

		// Get control flow distance
		if (!result.getTrace().getTouchedMutants().contains(mutation.getId()))
			executionDistance = getExecutionDistance(result);
		else
			executionDistance = 0.0;

		double infectionDistance = 1.0;

		double impactDistance = 1.0;

		// If executed...but not with reflection
		if (executionDistance <= 0 && !result.calledReflection()) {
			// Add infection distance
			assert (result.getTrace() != null);
			// assert (result.getTrace().mutantDistances != null);
			assert (result.getTrace().getTouchedMutants().contains(mutation.getId()));
			infectionDistance = normalize(result.getTrace().getMutationDistance(mutation.getId()));
			logger.debug("Infection distance for mutation = " + infectionDistance);

			// Don't re-execute on the mutant if we believe the mutant causes timeouts
			if (MutationTimeoutStoppingCondition.isDisabled(mutation) && infectionDistance <= 0) { 
				impactDistance = 0.0;
			}
			// If infected check if it is also killed
			else if (infectionDistance <= 0) {

				
				logger.debug("Running test on mutant " + mutation.getId());
				MutationExecutionResult mutationResult = individual.getLastExecutionResult(mutation);

				if (mutationResult == null) {
					ExecutionResult exResult = runTest(individual.getTestCase(), mutation);
					mutationResult = getMutationResult(result, exResult);
					individual.setLastExecutionResult(mutationResult, mutation);
				}
				if (mutationResult.hasTimeout()) {
					logger.debug("Found timeout in mutant!");
					MutationTimeoutStoppingCondition.timeOut(mutation);
				}

				if (mutationResult.hasException()) {
					logger.debug("Mutant raises exception");
				}

				if (mutationResult.getNumAssertions() == 0) {
					double impact = mutationResult.getImpact();
					logger.debug("Impact is " + impact + " (" + (1.0 / (1.0 + impact))
					        + ")");
					impactDistance = 1.0 / (1.0 + impact);
				} else {
					logger.debug("Assertions: " + mutationResult.getNumAssertions());
					impactDistance = 0.0;
				}
				logger.debug("Impact distance for mutation = " + fitness);

			}
		}

		fitness = impactDistance + infectionDistance + executionDistance;
		logger.debug("Individual fitness: " + impactDistance + " + " + infectionDistance
		        + " + " + executionDistance + " = " + fitness);
		//if (fitness == 0.0) {
		//	assert (getNumAssertions(individual.getLastExecutionResult(),
		//	                         individual.getLastExecutionResult(mutation)) > 0);
		//}

		updateIndividual(this, individual, fitness);
		if (fitness == 0.0) {
			individual.getTestCase().addCoveredGoal(this);
		}
		return fitness;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Strong " + mutation.toString();
	}

	@Override
	public boolean isCovered(TestChromosome individual, ExecutionResult result) {
		boolean covered = false;

		if (individual.getLastExecutionResult(mutation) == null) {
			covered = getFitness(individual, result) == 0.0;
		}

		if (!covered && individual.getLastExecutionResult(mutation) != null) {
			MutationExecutionResult mutantResult = individual.getLastExecutionResult(mutation);
			if (mutantResult.hasTimeout())
				covered = true;
			else if (mutantResult.hasException() && result.noThrownExceptions())
				covered = true;
		}

		return covered;
	}
}
