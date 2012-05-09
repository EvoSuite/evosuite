/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.unisb.cs.st.evosuite.assertion.AssertionTraceObserver;
import de.unisb.cs.st.evosuite.assertion.ComparisonTraceEntry;
import de.unisb.cs.st.evosuite.assertion.ComparisonTraceObserver;
import de.unisb.cs.st.evosuite.assertion.InspectorTraceEntry;
import de.unisb.cs.st.evosuite.assertion.InspectorTraceObserver;
import de.unisb.cs.st.evosuite.assertion.NullTraceEntry;
import de.unisb.cs.st.evosuite.assertion.NullTraceObserver;
import de.unisb.cs.st.evosuite.assertion.OutputTrace;
import de.unisb.cs.st.evosuite.assertion.PrimitiveFieldTraceEntry;
import de.unisb.cs.st.evosuite.assertion.PrimitiveFieldTraceObserver;
import de.unisb.cs.st.evosuite.assertion.PrimitiveTraceEntry;
import de.unisb.cs.st.evosuite.assertion.PrimitiveTraceObserver;
import de.unisb.cs.st.evosuite.coverage.TestCoverageGoal;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;

/**
 * @author fraser
 * 
 */
public class StrongMutationTestFitness extends MutationTestFitness {

	private static final long serialVersionUID = -262199037689935052L;

	protected static Class<?>[] observerClasses = { PrimitiveTraceEntry.class,
	        ComparisonTraceEntry.class, InspectorTraceEntry.class,
	        PrimitiveFieldTraceEntry.class, NullTraceEntry.class };

	protected static AssertionTraceObserver<?>[] observers = {
	        new PrimitiveTraceObserver(), new ComparisonTraceObserver(),
	        new InspectorTraceObserver(), new PrimitiveFieldTraceObserver(),
	        new NullTraceObserver() };

	public StrongMutationTestFitness(Mutation mutation) {
		super(mutation);
		for (AssertionTraceObserver<?> observer : observers) {
			logger.debug("StrongMutation adding observer " + observer);
			executor.addObserver(observer);
		}
	}

	@Override
	public ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

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
			result = executor.execute(test);
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
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			System.exit(1);
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
		double coverage_impact = getCoverageDifference(orig_trace.coverage,
		                                               mutant_trace.coverage);
		logger.debug("Coverage impact: " + coverage_impact);
		logger.debug("Calculating data impact");
		double data_impact = getCoverageDifference(orig_trace.returnData,
		                                           mutant_trace.returnData);
		logger.debug("Data impact: " + data_impact);

		double branch_impact = 0.0;
		for (Integer predicate : orig_trace.coveredPredicates.keySet()) {
			if (mutant_trace.trueDistances.containsKey(predicate)) {
				branch_impact += normalize(Math.abs(orig_trace.trueDistances.get(predicate)
				        - mutant_trace.trueDistances.get(predicate)));
			} else {
				branch_impact += 1.0;
			}
			if (mutant_trace.falseDistances.containsKey(predicate)) {
				branch_impact += normalize(Math.abs(orig_trace.falseDistances.get(predicate)
				        - mutant_trace.falseDistances.get(predicate)));
			} else {
				branch_impact += 1.0;
			}
		}
		logger.debug("Branch impact: " + branch_impact);

		return normalize(coverage_impact) + normalize(data_impact) + branch_impact;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private int getNumAssertions(ExecutionResult orig_result,
	        ExecutionResult mutant_result) {
		int num = 0;
		if (orig_result.test.size() == 0)
			return 0;

		for (Class<?> observerClass : observerClasses) {
			OutputTrace trace = mutant_result.getTrace(observerClass);
			OutputTrace orig = orig_result.getTrace(observerClass);
			num += orig.numDiffer(trace);
		}

		logger.debug("Found " + num + " assertions!");
		return num;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestFitnessFunction#getFitness(de.unisb.cs.st.evosuite.testcase.TestChromosome, de.unisb.cs.st.evosuite.testcase.ExecutionResult)
	 */
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
		if (!result.getTrace().touchedMutants.contains(mutation.getId()))
			executionDistance = getExecutionDistance(result);
		else
			executionDistance = 0.0;

		double infectionDistance = 1.0;

		double impactDistance = 1.0;

		// If executed...
		if (executionDistance <= 0) {
			// Add infection distance
			assert (result.getTrace() != null);
			assert (result.getTrace().mutantDistances != null);
			assert (mutation != null);
			assert (result.getTrace().touchedMutants.contains(mutation.getId()));
			infectionDistance = normalize(result.getTrace().mutantDistances.get(mutation.getId()));
			logger.debug("Infection distance for mutation = " + infectionDistance);

			// If infected check if it is also killed
			if (infectionDistance <= 0) {
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

		updateIndividual(individual, fitness);
		if (fitness == 0.0) {
			individual.getTestCase().addCoveredGoal(this);
		}
		return fitness;
	}
}
