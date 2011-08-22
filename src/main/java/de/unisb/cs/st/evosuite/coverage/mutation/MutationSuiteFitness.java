/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unisb.cs.st.evosuite.assertion.ComparisonTraceObserver;
import de.unisb.cs.st.evosuite.assertion.InspectorTraceObserver;
import de.unisb.cs.st.evosuite.assertion.NullOutputObserver;
import de.unisb.cs.st.evosuite.assertion.PrimitiveFieldTraceObserver;
import de.unisb.cs.st.evosuite.assertion.PrimitiveOutputTraceObserver;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class MutationSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -8320078404661057113L;

	private final BranchCoverageSuiteFitness branchFitness;

	private final List<TestFitnessFunction> mutationGoals;

	protected PrimitiveOutputTraceObserver primitiveObserver = new PrimitiveOutputTraceObserver();
	protected ComparisonTraceObserver comparisonObserver = new ComparisonTraceObserver();
	protected InspectorTraceObserver inspectorObserver = new InspectorTraceObserver();
	protected PrimitiveFieldTraceObserver fieldObserver = new PrimitiveFieldTraceObserver();
	protected NullOutputObserver nullObserver = new NullOutputObserver();

	public MutationSuiteFitness() {
		MutationFactory factory = new MutationFactory();
		mutationGoals = factory.getCoverageGoals();
		logger.info("Mutation goals: " + mutationGoals.size());
		executor.addObserver(primitiveObserver);
		executor.addObserver(comparisonObserver);
		executor.addObserver(inspectorObserver);
		executor.addObserver(fieldObserver);
		executor.addObserver(nullObserver);
		branchFitness = new BranchCoverageSuiteFitness();
	}

	@Override
	public ExecutionResult runTest(TestCase test) {
		return runTest(test, null);
	}

	public ExecutionResult runTest(TestCase test, Mutation mutant) {

		ExecutionResult result = new ExecutionResult(test, mutant);

		try {
			if (mutant != null)
				logger.debug("Executing test for mutant " + mutant.getId());
			else
				logger.debug("Executing test without mutant");

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
			/*
						executor.removeObserver(primitiveObserver);
						executor.removeObserver(comparisonObserver);
						executor.removeObserver(inspectorObserver);
						executor.removeObserver(fieldObserver);
						executor.removeObserver(nullObserver);
						*/

		} catch (Exception e) {
			System.out.println("TG: Exception caught: " + e);
			e.printStackTrace();
			System.exit(1);
		}

		return result;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#getFitness(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(Chromosome individual) {
		// TODO Auto-generated method stub
		runTestSuite((TestSuiteChromosome) individual);

		// First objective: achieve branch coverage
		logger.debug("Calculating branch fitness: ");
		double fitness = 0.0; // branchFitness.getFitness(individual);
		//logger.info("Branch fitness: " + fitness);

		// Additional objective 1: all mutants need to be touched

		// Count number of touched mutations (ask MutationObserver)
		TestSuiteChromosome suite = (TestSuiteChromosome) individual;
		Set<Integer> touchedMutants = new HashSet<Integer>();
		//Map<Integer, Double> infectionDistance = new HashMap<Integer, Double>();

		for (TestChromosome test : suite.getTestChromosomes()) {
			ExecutionResult result = test.getLastExecutionResult();
			ExecutionTrace trace = result.getTrace();
			touchedMutants.addAll(trace.touchedMutants);

			for (TestFitnessFunction mutant : mutationGoals) {
				MutationTestFitness mutantFitness = (MutationTestFitness) mutant;
				if (trace.touchedMutants.contains(mutantFitness.getMutation().getId()))
					fitness += FitnessFunction.normalize(mutant.getFitness(test, result));
				else
					fitness += 1.0;
			}

			/*
						for (Entry<Integer, Double> mutation : trace.mutant_distances.entrySet()) {
							if (!infectionDistance.containsKey(mutation.getKey()))
								infectionDistance.put(mutation.getKey(), mutation.getValue());
							else
								infectionDistance.put(mutation.getKey(),
								                      Math.min(mutation.getValue(),
								                               infectionDistance.get(mutation.getKey())));
						}
						*/
		}

		/*
		logger.info("Touched " + touchedMutants.size() + " / " + numMutations
		        + " mutants");
		fitness += numMutations - touchedMutants.size();

		// TODO: Always execute on all mutants, or first optimize for branch coverage?
		// Idea: Always calculate infection distance, and only execute again if touched && infection distance = 0.0

		// Additional objective 2: minimize infection distance for all mutants
		double infection = 0.0;
		for (Integer mutationId : infectionDistance.keySet())
			infection += infectionDistance.get(mutationId);

		logger.info("Infection distance: " + infection);
		fitness += infection;

		// Additional objective 3: each mutant needs to propagate
		// fitness += total mutants - killed mutants

		// TODO: Each statement needs to be covered at least as often as it is mutated? No.
		*/
		updateIndividual(individual, fitness);

		return fitness;
	}

}
