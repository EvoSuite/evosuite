/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.unisb.cs.st.evosuite.assertion.ComparisonTraceObserver;
import de.unisb.cs.st.evosuite.assertion.InspectorTraceObserver;
import de.unisb.cs.st.evosuite.assertion.NullOutputObserver;
import de.unisb.cs.st.evosuite.assertion.PrimitiveFieldTraceObserver;
import de.unisb.cs.st.evosuite.assertion.PrimitiveOutputTraceObserver;
import de.unisb.cs.st.evosuite.coverage.ControlFlowDistance;
import de.unisb.cs.st.evosuite.coverage.TestCoverageGoal;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageGoal;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class MutationTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 596930765039928708L;

	private final Mutation mutation;

	private final Set<BranchCoverageGoal> controlDependencies = new HashSet<BranchCoverageGoal>();

	protected List<ExecutionObserver> observers;

	protected PrimitiveOutputTraceObserver primitiveObserver = new PrimitiveOutputTraceObserver();
	protected ComparisonTraceObserver comparisonObserver = new ComparisonTraceObserver();
	protected InspectorTraceObserver inspectorObserver = new InspectorTraceObserver();
	protected PrimitiveFieldTraceObserver fieldObserver = new PrimitiveFieldTraceObserver();
	protected NullOutputObserver nullObserver = new NullOutputObserver();

	public MutationTestFitness(Mutation mutation) {
		this.mutation = mutation;
		controlDependencies.addAll(mutation.getControlDependencies());
		executor.addObserver(primitiveObserver);
		executor.addObserver(comparisonObserver);
		executor.addObserver(inspectorObserver);
		executor.addObserver(fieldObserver);
		executor.addObserver(nullObserver);
	}

	public Mutation getMutation() {
		return mutation;
	}

	@Override
	public ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

	public ExecutionResult runTest(TestCase test, Mutation mutant) {

		ExecutionResult result = new ExecutionResult(test, mutant);

		try {
			if (mutant != null)
				logger.info("Executing test for mutant " + mutant.getId());
			else
				logger.info("Executing test witout mutant");

			if (mutant != null)
				MutationObserver.activateMutation(mutant);
			result = executor.execute(test);
			if (mutant != null)
				MutationObserver.deactivateMutation(mutant);

			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);

			result.comparison_trace = comparisonObserver.getTrace();
			result.primitive_trace = primitiveObserver.getTrace();
			result.inspector_trace = inspectorObserver.getTrace();
			result.field_trace = fieldObserver.getTrace();
			result.null_trace = nullObserver.getTrace();

		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			System.exit(1);
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

		// double sum = getCoverageDifference(getCoverage(orig_trace),
		// getCoverage(mutant_trace));
		double coverage_impact = getCoverageDifference(orig_trace.coverage,
		                                               mutant_trace.coverage);
		logger.debug("Coverage impact: " + coverage_impact);
		double data_impact = getCoverageDifference(orig_trace.return_data,
		                                           mutant_trace.return_data);
		logger.debug("Data impact: " + data_impact);

		return coverage_impact + data_impact;
	}

	private int getNumAssertions(ExecutionResult orig_result,
	        ExecutionResult mutant_result) {
		int num = 0;
		if (orig_result.test.size() == 0)
			return 0;

		int last_num = 0;
		// num +=
		// orig_result.output_trace.numDiffer(mutant_result.output_trace);
		// if (num > last_num)
		// logger.debug("Found " + (num - last_num) + " output assertions!");
		// last_num = num;
		num += orig_result.comparison_trace.numDiffer(mutant_result.comparison_trace);
		if (num > last_num)
			logger.debug("Found " + (num - last_num) + " comparison assertions!");
		last_num = num;
		num += orig_result.primitive_trace.numDiffer(mutant_result.primitive_trace);
		if (num > last_num)
			logger.debug("Found " + (num - last_num) + " primitive assertions!");
		last_num = num;
		num += orig_result.inspector_trace.numDiffer(mutant_result.inspector_trace);
		if (num > last_num)
			logger.debug("Found " + (num - last_num) + " inspector assertions!");
		last_num = num;
		num += orig_result.field_trace.numDiffer(mutant_result.field_trace);
		if (num > last_num)
			logger.debug("Found " + (num - last_num) + " field assertions!");
		last_num = num;
		num += orig_result.null_trace.numDiffer(mutant_result.null_trace);
		if (num > last_num)
			logger.debug("Found " + (num - last_num) + " null assertions!");

		logger.debug("Found " + num + " assertions!");
		return num;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestFitnessFunction#getFitness(de.unisb.cs.st.evosuite.testcase.TestChromosome, de.unisb.cs.st.evosuite.testcase.ExecutionResult)
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		// If not touched, fitness = branchcoveragefitnesses + 2

		// If executed, fitness = normalize(constraint distance) + asserted_yes_no

		// If infected, check impact?

		double fitness = 0.0;

		// Get control flow distance
		if (controlDependencies.isEmpty()) {
			String key = mutation.getClassName() + "." + mutation.getMethodName();
			if (result.getTrace().covered_methods.containsKey(key)) {
				logger.info("Target method " + key + " was executed");
			} else {
				logger.info("Target method " + key + " was not executed");
				fitness += 1.0;
			}
		} else {
			ControlFlowDistance cfgDistance = null;
			for (BranchCoverageGoal dependency : controlDependencies) {
				logger.info("Checking dependency...");
				ControlFlowDistance distance = dependency.getDistance(result);
				if (cfgDistance == null)
					cfgDistance = distance;
				else {
					if (distance.compareTo(cfgDistance) < 0)
						cfgDistance = distance;
				}
			}
			if (cfgDistance != null) {
				logger.info("Found control dependency");
				fitness = cfgDistance.getResultingBranchFitness();
			}
		}

		logger.info("Control flow distance to mutation = " + fitness);
		// If executed...
		if (fitness <= 0) {

			assert (result.getTrace().touchedMutants.contains(mutation.getId()));

			// Add infection distance
			if (!result.getTrace().mutant_distances.containsKey(mutation.getId())) {
				logger.info("Have no distance information for " + mutation.getId());
				for (Integer id : result.getTrace().mutant_distances.keySet()) {
					logger.info("Mutation " + id + ": "
					        + result.getTrace().mutant_distances.get(id));
				}
			}
			fitness += FitnessFunction.normalize(result.getTrace().mutant_distances.get(mutation.getId()));

			logger.info("Infection distance for mutation = " + fitness);

			// If infected check if it is also killed
			if (fitness <= 0) {
				ExecutionResult mutationResult = runTest(individual.test, mutation);

				if (TestCoverageGoal.hasTimeout(mutationResult)) {
					logger.debug("Found timeout in mutant!");
					MutationTimeoutStoppingCondition.timeOut(mutation);
				}

				if (getNumAssertions(result, mutationResult) == 0) {
					double impact = getSumDistance(result.getTrace(),
					                               mutationResult.getTrace());
					logger.info("Impact is " + impact + " (" + (1.0 / (1.0 + impact))
					        + ")");
					fitness += 1.0 / (1.0 + impact);
				} else {
					logger.info("Mutant is asserted!");
					//return 0.0; // This mutant is asserted
				}
			}

		}

		updateIndividual(individual, fitness);
		return fitness;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#updateIndividual(de.unisb.cs.st.evosuite.ga.Chromosome, double)
	 */
	@Override
	protected void updateIndividual(Chromosome individual, double fitness) {
		individual.setFitness(fitness);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return mutation.toString();
	}
}
