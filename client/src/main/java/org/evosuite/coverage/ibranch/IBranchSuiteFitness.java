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
package org.evosuite.coverage.ibranch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.coverage.archive.TestsArchive;
import org.evosuite.rmi.ClientServices;
import org.evosuite.setup.Call;
import org.evosuite.setup.CallContext;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * We don't remember what the I of IBranch stands for. Anyway, this fitness
 * function targets all the branches (of all classes) that is possible to reach
 * from the class under test.
 * 
 * @author Gordon Fraser, mattia
 * 
 */
public class IBranchSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 5836092966704859022L;

	private final List<IBranchTestFitness> branchGoals;

	private int totGoals;

	private final Map<Integer, Map<CallContext, Set<IBranchTestFitness>>> goalsMap;

	/** Branchless methods map. */
	private final Map<String, Map<CallContext, IBranchTestFitness>> methodsMap;

	private final Set<IBranchTestFitness> toRemoveBranchesT = new HashSet<>();
	private final Set<IBranchTestFitness> toRemoveBranchesF = new HashSet<>();
	private final Set<IBranchTestFitness> toRemoveRootBranches = new HashSet<>();

	private final Set<IBranchTestFitness> removedBranchesT = new HashSet<>();
	private final Set<IBranchTestFitness> removedBranchesF = new HashSet<>();
	private final Set<IBranchTestFitness> removedRootBranches = new HashSet<>();

	public IBranchSuiteFitness() {
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
				Map<CallContext, IBranchTestFitness> innermap = methodsMap.get(methodName);
				if (innermap == null) {
					methodsMap.put(methodName, innermap = new HashMap<>());
				}
				innermap.put(goal.getContext(), goal);
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
		ClientServices
				.getInstance()
				.getClientNode()
				.trackOutputVariable(RuntimeVariable.IBranchInitialGoalsInTargetClass,
						goalsInTarget);

	}

	private IBranchTestFitness getContextGoal(String classAndMethodName, CallContext context) {
		if (methodsMap.get(classAndMethodName) == null
				|| methodsMap.get(classAndMethodName).get(context) == null)
			return null;
		return methodsMap.get(classAndMethodName).get(context);
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

	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite, boolean updateChromosome) {
		double fitness = 0.0; // branchFitness.getFitness(suite);
		List<ExecutionResult> results = runTestSuite(suite);

		Map<IBranchTestFitness, Double> distanceMap = new HashMap<>();
		Map<IBranchTestFitness, Integer> callCount = new HashMap<>();

		for (ExecutionResult result : results) {

			for (Integer branchId : result.getTrace().getTrueDistancesContext().keySet()) {
				Map<CallContext, Double> trueMap = result.getTrace().getTrueDistancesContext()
						.get(branchId);

				for (CallContext context : trueMap.keySet()) {
					IBranchTestFitness goalT = getContextGoal(branchId, context, true);
					if (goalT == null || removedBranchesT.contains(goalT))
						continue;
					double distanceT = normalize(trueMap.get(context));
					if (distanceMap.get(goalT) == null || distanceMap.get(goalT) > distanceT) {
						distanceMap.put(goalT, distanceT);
					}
					if (Double.compare(distanceT, 0.0) == 0) {
						if(updateChromosome)
						result.test.addCoveredGoal(goalT);
						if(Properties.TEST_ARCHIVE) {
							TestsArchive.instance.putTest(this, goalT, result);
							toRemoveBranchesT.add(goalT);
							suite.isToBeUpdated(true);
						}
					}
				}
			}

			for (Integer branchId : result.getTrace().getFalseDistancesContext().keySet()) {

				Map<CallContext, Double> falseMap = result.getTrace().getFalseDistancesContext()
						.get(branchId);

				for (CallContext context : falseMap.keySet()) {
					IBranchTestFitness goalF = getContextGoal(branchId, context, false);
					if (goalF == null || removedBranchesF.contains(goalF))
						continue;
					double distanceF = normalize(falseMap.get(context));
					if (distanceMap.get(goalF) == null || distanceMap.get(goalF) > distanceF) {
						distanceMap.put(goalF, distanceF);
					}
					if (Double.compare(distanceF, 0.0) == 0) {
						if(updateChromosome)
							result.test.addCoveredGoal(goalF);
						if(Properties.TEST_ARCHIVE) {
							TestsArchive.instance.putTest(this, goalF, result);
							toRemoveBranchesF.add(goalF);
							suite.isToBeUpdated(true);
						}
					}
				}
			}

			for (Entry<String, Map<CallContext, Integer>> entry : result.getTrace()
					.getMethodContextCount().entrySet()) {
				for (Entry<CallContext, Integer> value : entry.getValue().entrySet()) {
					IBranchTestFitness goal = getContextGoal(entry.getKey(), value.getKey());
					if (goal == null || removedRootBranches.contains(goal))
						continue;
					int count = value.getValue();
					if (callCount.get(goal) == null || callCount.get(goal) < count) {
						callCount.put(goal, count);
					}
					if (count > 0) {
						if(updateChromosome)
							result.test.addCoveredGoal(goal);
						if(Properties.TEST_ARCHIVE) {
							TestsArchive.instance.putTest(this, goal, result);
							toRemoveRootBranches.add(goal);
							suite.isToBeUpdated(true);
						}
					}
				}
			}
		}

		int numCoveredGoals = 0;
		for (IBranchTestFitness goal : branchGoals) {
			Double distance = distanceMap.get(goal);
			if (distance == null)
				distance = 1.0;

			if (goal.getBranch() == null) {
				Integer count = callCount.get(goal);
				if (count == null || count == 0) {
					fitness += 1;
				} else {
					numCoveredGoals++;
				}
			} else {
				if (distance == 0.0) {
					numCoveredGoals++;
				}
				fitness += distance;
			}
		}

		if(updateChromosome) {
			numCoveredGoals += removedBranchesF.size();
			numCoveredGoals += removedBranchesT.size();
			numCoveredGoals += removedRootBranches.size();
			if (totGoals > 0) {
				suite.setCoverage(this, (double) numCoveredGoals / (double) totGoals);
			}
			suite.setNumOfCoveredGoals(this, numCoveredGoals);
			suite.setNumOfNotCoveredGoals(this, totGoals - numCoveredGoals);
			updateIndividual(this, suite, fitness);
		}
		return fitness;
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.ga.FitnessFunction#getFitness(org.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {
		return getFitness(suite, true);
	}

	@Override
	public boolean updateCoveredGoals() {

		if(!Properties.TEST_ARCHIVE)
			return false;
		
		for (IBranchTestFitness method : toRemoveRootBranches) {
			boolean removed = branchGoals.remove(method);

			Map<CallContext, IBranchTestFitness> map = methodsMap.get(method.getTargetClass() + "."
					+ method.getTargetMethod());

			IBranchTestFitness f = map.remove(method.getContext());

			if (removed && f != null) {
				removedRootBranches.add(method);
			} else {
				throw new IllegalStateException("goal to remove not found");
			}
		}

		for (IBranchTestFitness branch : toRemoveBranchesT) {
			boolean removed = branchGoals.remove(branch);
			Map<CallContext, Set<IBranchTestFitness>> map = goalsMap.get(branch.getBranch()
					.getActualBranchId());
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
			Map<CallContext, Set<IBranchTestFitness>> map = goalsMap.get(branch.getBranch()
					.getActualBranchId());
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
