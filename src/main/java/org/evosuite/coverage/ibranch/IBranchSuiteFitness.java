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

	private Map<IBranchTestFitness, Integer> getDefaultCallCountMap() {
		Map<IBranchTestFitness, Integer> distanceMap = new HashMap<IBranchTestFitness, Integer>();
		for (IBranchTestFitness goal : branchGoals)
			distanceMap.put(goal, 0);
		return distanceMap;
	}

	private IBranchTestFitness getContextGoal(Integer branchId, CallContext context) {
		for (IBranchTestFitness goal : branchGoals) {
			if (goal.getBranch().getActualBranchId() == branchId) {
				if (goal.getContext().equals(context))
					return goal;
			}
		}

		throw new RuntimeException("Could not find goal for " + branchId + ", context "
		        + context);
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

		Map<IBranchTestFitness, Integer> callCount = getDefaultCallCountMap();

		for (ExecutionResult result : results) {
			// Determine minimum branch distance for each branch in each context
			for (Entry<Integer, Map<CallContext, Double>> entry : result.getTrace().getTrueDistancesContext().entrySet()) {
				for (Entry<CallContext, Double> value : entry.getValue().entrySet()) {
					IBranchTestFitness goal = getContextGoal(entry.getKey(),
					                                         value.getKey());
					double distance = value.getValue();
					if (distanceMap.get(goal) > distance) {
						distanceMap.put(goal, distance);
					}
				}
			}

			// Determine maximum execution count for each branch in each context
			for (Entry<Integer, Map<CallContext, Integer>> entry : result.getTrace().getPredicateContextExecutionCount().entrySet()) {
				for (Entry<CallContext, Integer> value : entry.getValue().entrySet()) {
					IBranchTestFitness goal = getContextGoal(entry.getKey(),
					                                         value.getKey());
					int count = value.getValue();
					if (callCount.get(goal) < count) {
						callCount.put(goal, count);
					}
				}
			}
		}

		for (IBranchTestFitness goal : branchGoals) {
			double distance = distanceMap.get(goal);
			int count = callCount.get(goal);

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
