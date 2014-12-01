/**
 * 
 */
package org.evosuite.coverage.ibranch.archive;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.coverage.ibranch.IBranchFitnessFactory;
import org.evosuite.coverage.ibranch.IBranchTestFitness;
import org.evosuite.rmi.ClientServices;
import org.evosuite.setup.Call;
import org.evosuite.setup.CallContext;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * We don't remember what the I of IBranch stands for. Anyway, this fitness
 * function targets all the branches (of all classes) that is possible to reach
 * from the class under test. During the evolution, this fitness updates the
 * list of goals by removing the covered ones, storing at the same time the
 * tests that covered them in an archive.
 * 
 * @author Gordon Fraser, mattia
 * 
 */
public class ArchiveIBranchSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -4745892521350308986L;

	private final List<IBranchTestFitness> branchGoals;

	private int totGoals;
	
	private final Map<Integer, Map<CallContext, Set<IBranchTestFitness>>> goalsMap;

	private final Map<String, Map<CallContext, Set<IBranchTestFitness>>> methodsMap;

//	private final StoredTestPool savedTests;
	private final TestsArchive bestChromoBuilder;
	
	private final Set<IBranchTestFitness> toRemoveBranchesT = new HashSet<>();
	private final Set<IBranchTestFitness> toRemoveBranchesF = new HashSet<>();
	private final Set<IBranchTestFitness> toRemoveRootBranches = new HashSet<>();

	private final Set<IBranchTestFitness> removedBranchesT = new HashSet<>();
	private final Set<IBranchTestFitness> removedBranchesF = new HashSet<>();
	private final Set<IBranchTestFitness> removedRootBranches = new HashSet<>();

	public ArchiveIBranchSuiteFitness() {
		bestChromoBuilder = new TestsArchive();
		goalsMap = new HashMap<>();
		methodsMap = new HashMap<>();
		IBranchFitnessFactory factory = new IBranchFitnessFactory();
		branchGoals = factory.getCoverageGoals();
		countGoals(branchGoals);
	
		for (IBranchTestFitness goal : branchGoals) {
			if (goal.getBranchGoal() != null && goal.getBranchGoal().getBranch() != null) {
				int branchId = goal.getBranchGoal().getBranch().getActualBranchId();

				Map<CallContext, Set<IBranchTestFitness>> innermap = goalsMap.get(branchId);
				if (innermap == null) {
					goalsMap.put(branchId, innermap = new HashMap<>());
				}
				Set<IBranchTestFitness> tempInSet = innermap.get(goal.getContext());
				if (tempInSet == null) {
					innermap.put(goal.getContext(), tempInSet = new HashSet<>());
				}
				tempInSet.add(goal);
			} else {
				String methodName = goal.getTargetClass() + "." + goal.getTargetMethod();
				Map<CallContext, Set<IBranchTestFitness>> innermap = methodsMap.get(methodName);
				if (innermap == null) {
					methodsMap.put(methodName, innermap = new HashMap<>());
				}
				Set<IBranchTestFitness> tempInSet = innermap.get(goal.getContext());
				if (tempInSet == null) {
					innermap.put(goal.getContext(), tempInSet = new HashSet<>());
				}
				tempInSet.add(goal);

			}
			logger.info("Context goal: " + goal.toString());
		}
		totGoals = branchGoals.size();
	}
	
	public ArchiveIBranchSuiteFitness(TestsArchive bestChromoBuilder) {
		this.bestChromoBuilder = bestChromoBuilder;
		goalsMap = new HashMap<>();
		methodsMap = new HashMap<>();
		IBranchFitnessFactory factory = new IBranchFitnessFactory();
		branchGoals = factory.getCoverageGoals();
		countGoals(branchGoals);
	
		for (IBranchTestFitness goal : branchGoals) {
			if (goal.getBranchGoal() != null && goal.getBranchGoal().getBranch() != null) {
				int branchId = goal.getBranchGoal().getBranch().getActualBranchId();

				Map<CallContext, Set<IBranchTestFitness>> innermap = goalsMap.get(branchId);
				if (innermap == null) {
					goalsMap.put(branchId, innermap = new HashMap<>());
				}
				Set<IBranchTestFitness> tempInSet = innermap.get(goal.getContext());
				if (tempInSet == null) {
					innermap.put(goal.getContext(), tempInSet = new HashSet<>());
				}
				tempInSet.add(goal);
			} else {
				String methodName = goal.getTargetClass() + "." + goal.getTargetMethod();
				Map<CallContext, Set<IBranchTestFitness>> innermap = methodsMap.get(methodName);
				if (innermap == null) {
					methodsMap.put(methodName, innermap = new HashMap<>());
				}
				Set<IBranchTestFitness> tempInSet = innermap.get(goal.getContext());
				if (tempInSet == null) {
					innermap.put(goal.getContext(), tempInSet = new HashSet<>());
				}
				tempInSet.add(goal);

			}
			logger.info("Context goal: " + goal.toString());
		}
		totGoals = branchGoals.size();
	}

	private void countGoals(List<IBranchTestFitness> branchGoals) {
		int totalGoals = branchGoals.size();
		int goalsInTarget = 0;
		for (IBranchTestFitness g : branchGoals) {
			boolean flag = true;
			for (Call call : g.getContext().getContext()) {
				if (!call.getClassName().equals(Properties.TARGET_CLASS)) {
					flag = false;
					break;
				}
			}
			if (flag)
				goalsInTarget++;
		}
		ClientServices.getInstance().getClientNode()
				.trackOutputVariable(RuntimeVariable.IBranchInitialGoals, totalGoals);
		ClientServices.getInstance().getClientNode()
				.trackOutputVariable(RuntimeVariable.IBranchInitialGoalsInTargetClass, goalsInTarget);

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

	private IBranchTestFitness getContextGoal(String classAndMethodName, CallContext context) {
		if (methodsMap.get(classAndMethodName) == null)
			return null;
		if (methodsMap.get(classAndMethodName).get(context) == null)
			return null;

		for (IBranchTestFitness iBranchTestFitness : methodsMap.get(classAndMethodName)
				.get(context)) {
			return iBranchTestFitness;
		}
		return null;
	}

	private IBranchTestFitness getContextGoal(Integer branchId, CallContext context, boolean value) {
		if (goalsMap.get(branchId) == null)
			return null;
		if (goalsMap.get(branchId).get(context) == null)
			return null;
		for (IBranchTestFitness iBranchTestFitness : goalsMap.get(branchId).get(context)) {
			if (iBranchTestFitness.getValue() == value) {
				return iBranchTestFitness;
			}
		}
		return null;
	}
	
	public TestSuiteChromosome getBestStoredIndividual(){
		return bestChromoBuilder.getBestChromosome();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		double fitness = 0.0; // branchFitness.getFitness(suite);
		List<ExecutionResult> results = runTestSuite(suite);
		Map<IBranchTestFitness, Double> distanceMap = getDefaultDistanceMap();

		Map<IBranchTestFitness, Integer> callCount = getDefaultCallCountMap();

		for (ExecutionResult result : results) {
			for (Entry<Integer, Map<CallContext, Double>> entry : result.getTrace()
					.getTrueDistancesContext().entrySet()) {
				for (Entry<CallContext, Double> value : entry.getValue().entrySet()) {

					IBranchTestFitness goal = getContextGoal(entry.getKey(), value.getKey(), true);
					if (goal == null || removedBranchesT.contains(goal))
						continue;
					double distance = normalize(value.getValue());
					if (distanceMap.get(goal) > distance) {
						distanceMap.put(goal, distance);
					}
					if (Double.compare(distance, 0.0) == 0) {
						result.test.addCoveredGoal(goal);
						bestChromoBuilder.putTest(goal, result.test);
						toRemoveBranchesT.add(goal);
						suite.setToBeUpdated(true);
					}
				}
			}
			for (Entry<Integer, Map<CallContext, Double>> entry : result.getTrace()
					.getFalseDistancesContext().entrySet()) {
				for (Entry<CallContext, Double> value : entry.getValue().entrySet()) {
					IBranchTestFitness goal = getContextGoal(entry.getKey(), value.getKey(), false);
					if (goal == null || removedBranchesF.contains(goal))
						continue;
					double distance = normalize(value.getValue());
					if (distanceMap.get(goal) > distance) {
						distanceMap.put(goal, distance);
					}
					if (Double.compare(distance, 0.0) == 0) {
						result.test.addCoveredGoal(goal);
						bestChromoBuilder.putTest(goal, result.test);
						toRemoveBranchesF.add(goal);
						suite.setToBeUpdated(true);
					}
				}
			}

//			// Determine maximum execution count for each branch in each context
//			for (Entry<Integer, Map<CallContext, Integer>> entry : result.getTrace()
//					.getPredicateContextExecutionCount().entrySet()) {
//				for (Entry<CallContext, Integer> value : entry.getValue().entrySet()) {
//					IBranchTestFitness goalT = getContextGoal(entry.getKey(), value.getKey(), true);
//					IBranchTestFitness goalF = getContextGoal(entry.getKey(), value.getKey(), false);
//
//					if (goalT != null || removedBranchesT.contains(goalT)) {
//						int countT = value.getValue();
//						if (callCount.get(goalT) < countT) {
//							callCount.put(goalT, countT);
//						}
//					}
//					if (goalF != null || removedBranchesT.contains(goalF)) {
//						int countF = value.getValue();
//						if (callCount.get(goalF) < countF) {
//							callCount.put(goalF, countF);
//						}
//					}
//				}
//			}
			for (Entry<String, Map<CallContext, Integer>> entry : result.getTrace()
					.getMethodContextCount().entrySet()) {
				for (Entry<CallContext, Integer> value : entry.getValue().entrySet()) {
					IBranchTestFitness goal = getContextGoal(entry.getKey(), value.getKey());
					if (goal == null || removedRootBranches.contains(goal))
						continue;

					int count = value.getValue();
					if (callCount.get(goal) < count) {
						callCount.put(goal, count);
					}
					if (count > 0) {
						result.test.addCoveredGoal(goal);
						bestChromoBuilder.putTest(goal, result.test);
						toRemoveRootBranches.add(goal);
						suite.setToBeUpdated(true);
					}
				}
			}
		}

		int numCoveredGoals = 0;
		for (IBranchTestFitness goal : branchGoals) {
			double distance = distanceMap.get(goal);
			int count = callCount.get(goal);

			if (goal.getBranch() == null) {
				if (count == 0) {
					fitness += 1;
				} else {
					numCoveredGoals++;
				}
			} else {
				if (distance == 0.0) {
					numCoveredGoals++;
				}
				fitness += distance;
//				if (count > 0 && distance == 0.0) {
//					numCoveredGoals++;
//				}
//				if (count == 0)
//					fitness += 1;
//				else
//					fitness += distance;

				// if (count == 1)
				// fitness += 0.5;
				// else if (count > 1)
				// fitness += distance;
				// else
				// fitness += 1;
			}
		}
		numCoveredGoals += removedBranchesF.size();
		numCoveredGoals += removedBranchesT.size();
		numCoveredGoals += removedRootBranches.size();

		if (totGoals>0) {
			suite.setCoverage(this, (double) numCoveredGoals / (double) totGoals);
		}
		suite.setNumOfCoveredGoals(this, numCoveredGoals);
		suite.setNumOfNotCoveredGoals(this, totGoals-numCoveredGoals);
		updateIndividual(this, suite, fitness);

		return fitness;
	}

	// private final List<IBranchTestFitness> branchGoals;
	//
	// private final Map<Integer, Map<CallContext, Set<IBranchTestFitness>>>
	// goalsMap;
	//
	// private final Map<String, Map<CallContext, Set<IBranchTestFitness>>>
	// methodsMap;
	//
	// private final StoredTestPool savedTests;
	@Override
	public boolean updateCoveredGoals() {

		for (IBranchTestFitness method : toRemoveRootBranches) {
			boolean removed = branchGoals.remove(method);
			Map<CallContext, Set<IBranchTestFitness>> map = methodsMap.get(method.getTargetClass()+"."+method.getTargetMethod());
			Set<IBranchTestFitness> set = map.get(method.getContext());
			boolean f = set.remove(method);
			
			if (removed && f) { 
				removedRootBranches.add(method);
			} else {
				throw new IllegalStateException("goal to remove not found");
			}
		}

		for (IBranchTestFitness branch : toRemoveBranchesT) {
			boolean removed = branchGoals.remove(branch);
			Map<CallContext, Set<IBranchTestFitness>> map = goalsMap.get(branch.getBranch().getActualBranchId());
			Set<IBranchTestFitness> set = map.get(branch.getContext());
			boolean f = set.remove(branch);
			
			if (removed && f) {  
				removedBranchesT.add(branch); 
			} else {
				throw new IllegalStateException("goal to remove not found");
			}
		}
		for (IBranchTestFitness branch : toRemoveBranchesF) {
			boolean removed = branchGoals.remove(branch);
			Map<CallContext, Set<IBranchTestFitness>> map = goalsMap.get(branch.getBranch().getActualBranchId());
			Set<IBranchTestFitness> set = map.get(branch.getContext());
			boolean f = set.remove(branch);
			
			if (removed && f) {  
				removedBranchesF.add(branch); 
			} else {
				throw new IllegalStateException("goal to remove not found");
			}
		}

		toRemoveRootBranches.clear();
		toRemoveBranchesF.clear();
		toRemoveBranchesT.clear();
		
		return true;
	}

}
