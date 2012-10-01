/**
 * 
 */
package org.evosuite.coverage.ibranch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.evosuite.coverage.branch.Branch;
import org.evosuite.setup.CallContext;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * @author Gordon Fraser
 * 
 */
public class IBranchSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -4745892521350308986L;

	private final List<IBranchTestFitness> branchGoals;

	private final Map<Branch, IBranchTestFitness> trueBranches = new HashMap<Branch, IBranchTestFitness>();

	private final Map<Branch, IBranchTestFitness> falseBranches = new HashMap<Branch, IBranchTestFitness>();

	public IBranchSuiteFitness() {
		IBranchFitnessFactory factory = new IBranchFitnessFactory();
		branchGoals = factory.getCoverageGoals();
	}

	private Map<IBranchTestFitness, Double> getDefaultDistanceMap() {
		Map<IBranchTestFitness, Double> distanceMap = new HashMap<IBranchTestFitness, Double>();
		for (IBranchTestFitness goal : branchGoals)
			distanceMap.put(goal, 1.0);
		return distanceMap;
	}

	private Map<Branch, Map<CallContext, Integer>> getDefaultCallCountMap() {
		Map<Branch, Map<CallContext, Integer>> distanceMap = new HashMap<Branch, Map<CallContext, Integer>>();
		for (IBranchTestFitness goal : branchGoals)
			distanceMap.put(goal.getBranch(), new HashMap<CallContext, Integer>());
		return distanceMap;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		double fitness = 0.0;

		List<ExecutionResult> results = runTestSuite(suite);
		Map<IBranchTestFitness, Double> distanceMap = getDefaultDistanceMap();

		Map<Branch, Map<CallContext, Integer>> callCount = getDefaultCallCountMap();

		// Determine minimum branch distance for each branch in each context
		for (ExecutionResult result : results) {
			for (Entry<Integer, Map<CallContext, Double>> entry : result.getTrace().getTrueDistancesContext().entrySet()) {
				for (Entry<CallContext, Double> value : entry.getValue().entrySet()) {
					IBranchTestFitness goal = null;
					double distance = value.getValue();
					if (distanceMap.get(goal) > distance) {
						distanceMap.put(goal, distance);
					}
				}
			}

			// TODO: Update call count
		}

		for (IBranchTestFitness goal : branchGoals) {
			double distance = distanceMap.get(goal);
			int count = 0;
			if (callCount.get(goal.getBranch()).containsKey(goal.getContext())) {
				count = callCount.get(goal.getBranch()).get(goal.getContext());
			}
			// If branch is called exactly once in that context, 
			// then the sum of false and true distance must be 1
			if (count == 1)
				fitness += 0.5;
			else if (count > 1)
				fitness += distance;
			else
				fitness += 1;
		}

		return fitness;
	}
}
