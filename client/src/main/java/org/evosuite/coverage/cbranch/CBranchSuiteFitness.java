/**
 * 
 */
package org.evosuite.coverage.cbranch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.setup.CallContext;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * Context Branch criterion, force the generation of test cases that directly
 * invoke the method where the branch is, i.e., do not consider covered a branch
 * if it is covered by invoking other methods. 
 * @author Gordon Fraser, mattia
 * 
 */
public class CBranchSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -4745892521350308986L;

	private final List<CBranchTestFitness> branchGoals; 
	
	private final Map<Integer, Map<CallContext, Set<CBranchTestFitness>>> contextGoalsMap;

	private final Map<Integer, Set<CBranchTestFitness>> privateMethodsGoalsMap;

	
	private final Map<String, Map<CallContext, CBranchTestFitness>> methodsMap;
	
	private final Map<String, CBranchTestFitness> privateMethodsMethodsMap;
	
 
	public CBranchSuiteFitness() {
		contextGoalsMap = new HashMap<>();
		privateMethodsGoalsMap = new HashMap<>();
		methodsMap = new HashMap<>();
		privateMethodsMethodsMap = new HashMap<>();
		
		CBranchFitnessFactory factory = new CBranchFitnessFactory();
		branchGoals = factory.getCoverageGoals();
		for (CBranchTestFitness goal : branchGoals) {
			if (goal.getBranchGoal() != null && goal.getBranchGoal().getBranch() != null) {
				int branchId = goal.getBranchGoal().getBranch().getActualBranchId();

				//if private method do not consider context
				if (goal.getContext().isEmpty()) {
					Set<CBranchTestFitness> tempInSet = privateMethodsGoalsMap.get(branchId);
					if (tempInSet == null) {
						privateMethodsGoalsMap.put(branchId, tempInSet = new HashSet<>());
					}
					tempInSet.add(goal);
				} else {
				//if public method consider context
					Map<CallContext, Set<CBranchTestFitness>> innermap = contextGoalsMap
							.get(branchId);
					if (innermap == null) {
						contextGoalsMap.put(branchId, innermap = new HashMap<>());
					}
					Set<CBranchTestFitness> tempInSet = innermap.get(goal.getContext());
					if (tempInSet == null) {
						innermap.put(goal.getContext(), tempInSet = new HashSet<>());
					}
					tempInSet.add(goal);
				}
			} else {
				String methodName = goal.getTargetClass() + "." + goal.getTargetMethod();
				//if private method do not consider context
				if (goal.getContext().isEmpty()) {
					privateMethodsMethodsMap.put(methodName, goal);
				} else {
					// if public method consider context
					Map<CallContext, CBranchTestFitness> innermap = methodsMap.get(methodName);
					if (innermap == null) {
						methodsMap.put(methodName, innermap = new HashMap<>());
					}
					innermap.put(goal.getContext(), goal);
				}
			}
			logger.info("Context goal: " + goal.toString());
		}
	}

    private Map<CBranchTestFitness, Double> getDefaultDistanceMap() {
		Map<CBranchTestFitness, Double> distanceMap = new HashMap<CBranchTestFitness, Double>();
		for (CBranchTestFitness goal : branchGoals)
			distanceMap.put(goal, 1.0);
		return distanceMap;
	}

	private Map<CBranchTestFitness, Integer> getDefaultCallCountMap() {
		Map<CBranchTestFitness, Integer> distanceMap = new HashMap<CBranchTestFitness, Integer>();
		for (CBranchTestFitness goal : branchGoals)
			distanceMap.put(goal, 0);
		return distanceMap;
	}

	private CBranchTestFitness getContextGoal(String classAndMethodName, CallContext context) {
		if (privateMethodsMethodsMap.containsKey(classAndMethodName)) {
			return privateMethodsMethodsMap.get(classAndMethodName);
		} else if (methodsMap.get(classAndMethodName) == null
				|| methodsMap.get(classAndMethodName).get(context) == null)
			return null;
		else
			return methodsMap.get(classAndMethodName).get(context);
	}
	
	private CBranchTestFitness getContextGoal(Integer branchId, CallContext context, boolean value) {
		if (privateMethodsGoalsMap.containsKey(branchId)) {
			for (CBranchTestFitness cBranchTestFitness : privateMethodsGoalsMap.get(branchId)) {
				if (cBranchTestFitness.getValue() == value) {
					return cBranchTestFitness;
				}
			}
		} else if (contextGoalsMap.get(branchId) == null
				|| contextGoalsMap.get(branchId).get(context) == null)
			return null;
		else
			for (CBranchTestFitness cBranchTestFitness : contextGoalsMap.get(branchId).get(context)) {
				if (cBranchTestFitness.getValue() == value) {
					return cBranchTestFitness;
				}
			}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		double fitness = 0.0; //branchFitness.getFitness(suite);

		List<ExecutionResult> results = runTestSuite(suite);
		Map<CBranchTestFitness, Double> distanceMap = getDefaultDistanceMap();

		Map<CBranchTestFitness, Integer> callCount = getDefaultCallCountMap();

		for (ExecutionResult result : results) {
			// Determine minimum branch distance for each branch in each context
			for (Entry<Integer, Map<CallContext, Double>> entry : result.getTrace().getTrueDistancesContext().entrySet()) {
				for (Entry<CallContext, Double> value : entry.getValue().entrySet()) {
					
					CBranchTestFitness goal = getContextGoal(entry.getKey(),
					                                         value.getKey(), true);
					if(goal==null) continue;
					double distance = normalize(value.getValue());
					if (distanceMap.get(goal) > distance) {
						distanceMap.put(goal, distance);
					}
					if (Double.compare(distance, 0.0) == 0) {
						result.test.addCoveredGoal(goal);
					}
				}
			}
			for (Entry<Integer, Map<CallContext, Double>> entry : result.getTrace().getFalseDistancesContext().entrySet()) {
				for (Entry<CallContext, Double> value : entry.getValue().entrySet()) {
					
					CBranchTestFitness goal = getContextGoal(entry.getKey(),
					                                         value.getKey(), false);
					if(goal==null) continue;
					double distance = normalize(value.getValue());
					if (distanceMap.get(goal) > distance) {
						distanceMap.put(goal, distance);
					}
					if (Double.compare(distance, 0.0) == 0) {
						result.test.addCoveredGoal(goal);
					}
				}
			}

			// Determine maximum execution count for each branch in each context
			for (Entry<Integer, Map<CallContext, Integer>> entry : result.getTrace().getPredicateContextExecutionCount().entrySet()) {
				for (Entry<CallContext, Integer> value : entry.getValue().entrySet()) {
					int count = value.getValue();
					
					CBranchTestFitness goalT = getContextGoal(entry.getKey(), value.getKey(), true);
					if(goalT==null) continue;
					if (callCount.get(goalT) < count) {
						callCount.put(goalT, count);
					}
					CBranchTestFitness goalF = getContextGoal(entry.getKey(), value.getKey(), false);
					if(goalF==null) continue;
					if (callCount.get(goalF) < count) {
						callCount.put(goalF, count);
					}
				}
			}
			for (Entry<String, Map<CallContext, Integer>> entry : result.getTrace().getMethodContextCount().entrySet()) {
				for (Entry<CallContext, Integer> value : entry.getValue().entrySet()) {
					CBranchTestFitness goal = getContextGoal(entry.getKey(),
					                                         value.getKey());
					if (goal == null)
						continue;

					int count = value.getValue();
					if (callCount.get(goal) < count) {
						callCount.put(goal, count);
					}
					if (count > 0) {
						result.test.addCoveredGoal(goal);
					}
				}
			}
		}

		int numCoveredGoals = 0;
		for (CBranchTestFitness goal : branchGoals) {
			double distance = distanceMap.get(goal);
			int count = callCount.get(goal);

			if (goal.getBranch() == null) {
				if (count == 0) {
					fitness += 1;
				} else {
					numCoveredGoals++;
				}
			} else {
				if (count > 0 && Double.compare(distance, 0.0) ==0) {
					numCoveredGoals++;
				}
				// If branch is called exactly once in that context, 
				// then the sum of false and true distance must be 1
				 if (count > 0)
					fitness += distance;
				else
					fitness += 1;
			}
		}
		
		if (!branchGoals.isEmpty()){
			suite.setCoverage(this, (double) numCoveredGoals / (double) branchGoals.size());
		}
		suite.setNumOfCoveredGoals(this, numCoveredGoals);
		suite.setNumOfNotCoveredGoals(this, branchGoals.size()-numCoveredGoals);
		updateIndividual(this, suite, fitness);
		
		return fitness;
	}
}
