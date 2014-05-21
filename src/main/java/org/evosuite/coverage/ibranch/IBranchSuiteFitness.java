/**
 * 
 */
package org.evosuite.coverage.ibranch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
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

	private static double bestFitness = Double.MAX_VALUE;

	private final BranchCoverageSuiteFitness branchFitness = new BranchCoverageSuiteFitness();

	public IBranchSuiteFitness() {
		IBranchFitnessFactory factory = new IBranchFitnessFactory();
		branchGoals = factory.getCoverageGoals();
		for (IBranchTestFitness goal : branchGoals) {
			logger.info("Context goal: " + goal.toString());
		}
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

	private IBranchTestFitness getContextGoal(String classAndMethodName,
	        CallContext context) {
		for (IBranchTestFitness goal : branchGoals) {
			if (goal.getBranch() != null)
				continue;

			String key = goal.getTargetClass() + "." + goal.getTargetMethod();
			if (key.equals(classAndMethodName)) {
				if (goal.getContext().matches(context)) {
					return goal;
				}
			}
		}

		return null;
	}

	private IBranchTestFitness getContextGoal(Integer branchId, CallContext context,
	        boolean value) {
		for (IBranchTestFitness goal : branchGoals) {
			if (goal.getBranch() == null)
				continue; // TODO

			if (goal.getBranch().getActualBranchId() == branchId) {
				//logger.info("Found matching branch id, checking context");
				//logger.info(goal.getContext().toString());
				//logger.info(context.toString());
				if (goal.getContext().matches(context)) {
					if (goal.getValue() == value)
						return goal;
				}
				//logger.info("No match");
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
		double fitness = 0.0; //branchFitness.getFitness(suite);

		List<ExecutionResult> results = runTestSuite(suite);
		Map<IBranchTestFitness, Double> distanceMap = getDefaultDistanceMap();

		Map<IBranchTestFitness, Integer> callCount = getDefaultCallCountMap();

		for (ExecutionResult result : results) {
			// Determine minimum branch distance for each branch in each context
			for (Entry<Integer, Map<CallContext, Double>> entry : result.getTrace().getTrueDistancesContext().entrySet()) {
				for (Entry<CallContext, Double> value : entry.getValue().entrySet()) {
					//logger.info("Got true distance of " + value.getValue() + " for "
					//        + entry.getKey() + " in context " + value.getKey());
					IBranchTestFitness goal = getContextGoal(entry.getKey(),
					                                         value.getKey(), true);
					double distance = normalize(value.getValue());
					//logger.info("True distance for goal " + goal + ": "
					//        + value.getValue());
					if (distanceMap.get(goal) > distance) {
						distanceMap.put(goal, distance);
					}
				}
			}
			for (Entry<Integer, Map<CallContext, Double>> entry : result.getTrace().getFalseDistancesContext().entrySet()) {
				for (Entry<CallContext, Double> value : entry.getValue().entrySet()) {
					//logger.info("Got false distance of " + value.getValue() + " for "
					//        + entry.getKey() + " in context " + value.getKey());
					IBranchTestFitness goal = getContextGoal(entry.getKey(),
					                                         value.getKey(), false);
					double distance = normalize(value.getValue());
					//logger.info("False distance for goal " + goal + ": "
					//        + value.getValue());
					if (distanceMap.get(goal) > distance) {
						distanceMap.put(goal, distance);
					}
				}
			}

			// Determine maximum execution count for each branch in each context
			for (Entry<Integer, Map<CallContext, Integer>> entry : result.getTrace().getPredicateContextExecutionCount().entrySet()) {
				for (Entry<CallContext, Integer> value : entry.getValue().entrySet()) {
					IBranchTestFitness goal = getContextGoal(entry.getKey(),
					                                         value.getKey(), true);
					int count = value.getValue();
					if (callCount.get(goal) < count) {
						callCount.put(goal, count);
					}
					goal = getContextGoal(entry.getKey(), value.getKey(), false);
					count = value.getValue();
					if (callCount.get(goal) < count) {
						callCount.put(goal, count);
					}
				}
			}
			for (Entry<String, Map<CallContext, Integer>> entry : result.getTrace().getMethodContextCount().entrySet()) {
				for (Entry<CallContext, Integer> value : entry.getValue().entrySet()) {
					IBranchTestFitness goal = getContextGoal(entry.getKey(),
					                                         value.getKey());
					if (goal == null)
						continue;

					int count = value.getValue();
					if (callCount.get(goal) < count) {
						callCount.put(goal, count);
					}
				}
			}
		}

		int numCoveredGoals = 0;
		for (IBranchTestFitness goal : branchGoals) {
			double distance = distanceMap.get(goal);
			int count = callCount.get(goal);

			if (goal.getBranch() == null) {
				if (count == 0)
					fitness += 1;
				else
					numCoveredGoals++;
			} else {
				if (count > 0 && distance == 0.0)
					numCoveredGoals++;

				// If branch is called exactly once in that context, 
				// then the sum of false and true distance must be 1
				if (count == 1)
					fitness += 0.5;
				else if (count > 1)
					fitness += distance;
				else
					fitness += 1;
			}
		}

		/*
		if (fitness < bestFitness) {
			bestFitness = fitness;
			logger.info("Best fitness: " + fitness);
			//logger.info(suite.toString());
			for (IBranchTestFitness goal : branchGoals) {
				double distance = distanceMap.get(goal);
				int count = callCount.get(goal);
				logger.info(count + ": " + distance);
			}
		}
		*/
		if (!branchGoals.isEmpty())
			suite.setCoverage((double) numCoveredGoals / (double) branchGoals.size());

		suite.setNumOfCoveredGoals(numCoveredGoals);
		updateIndividual(suite, fitness);

		return fitness;
	}
}
