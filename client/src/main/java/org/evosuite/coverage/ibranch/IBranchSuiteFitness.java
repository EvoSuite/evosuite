package org.evosuite.coverage.ibranch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.rmi.ClientServices;
import org.evosuite.setup.Call;
import org.evosuite.setup.CallContext;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
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

	private static final long serialVersionUID = -4745892521350308986L;

	private final List<IBranchTestFitness> branchGoals;

	/** Branches map. */
	private final Map<Integer, Map<CallContext, Set<IBranchTestFitness>>> goalsMap;

	/** Branchless methods map. */
	private final Map<String, Map<CallContext, IBranchTestFitness>> methodsMap;

	public IBranchSuiteFitness() {
		goalsMap = new HashMap<>();
		methodsMap = new HashMap<>();
		IBranchFitnessFactory factory = new IBranchFitnessFactory();
		branchGoals = factory.getCoverageGoals();
		countGoals(branchGoals);
		for (IBranchTestFitness goal : branchGoals) {
			// getBranchGoal and getBranch returns null when the goals is a
			// method without branches
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
		}
	}

	/**
	 * count goals and set output variables.
	 * @param branchGoals
	 */
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
		if (goalsMap.get(branchId) == null || goalsMap.get(branchId).get(context) == null)
			return null;
		for (IBranchTestFitness iBranchTestFitness : goalsMap.get(branchId).get(context)) {
			if (iBranchTestFitness.getValue() == value) {
				return iBranchTestFitness;
			}
		}
		return null;
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

		Map<IBranchTestFitness, Double> distanceMap = new HashMap<>();
		Map<Integer, Integer> callCounter = new HashMap<>();
		Map<Integer, Integer> branchCounter = new HashMap<>();

		for (ExecutionResult result : results) {
			// Determine minimum branch distance for each branch in each context
			assert (result.getTrace().getTrueDistancesContext().keySet().size() != result
					.getTrace().getFalseDistancesContext().keySet().size());

			//update distance map for any value of any branch goal
			for (Integer branchId : result.getTrace().getTrueDistancesContext().keySet()) {
				Map<CallContext, Double> trueMap = result.getTrace().getTrueDistancesContext()
						.get(branchId);
				Map<CallContext, Double> falseMap = result.getTrace().getFalseDistancesContext()
						.get(branchId);

				for (CallContext context : trueMap.keySet()) {
					//update true goals
					IBranchTestFitness goalT = getContextGoal(branchId, context, true);
					if (goalT == null)
						continue;
					double distanceT = normalize(trueMap.get(context));
					if (distanceMap.get(goalT) == null || distanceMap.get(goalT) > distanceT) {
						distanceMap.put(goalT, distanceT);
					}
					if (Double.compare(distanceT, 0.0) == 0) {
						result.test.addCoveredGoal(goalT);
					}
					
					//update false goals
					IBranchTestFitness goalF = getContextGoal(branchId, context, false);
					if (goalF == null)
						continue;
					double distanceF = normalize(falseMap.get(context));
					if (distanceMap.get(goalF) == null || distanceMap.get(goalF) > distanceF) {
						distanceMap.put(goalF, distanceF);
					}
					if (Double.compare(distanceF, 0.0) == 0) {
						result.test.addCoveredGoal(goalF);
					}
				}

			}

			//Count how many time any branch has been executed
			for (Entry<Integer, Map<CallContext, Integer>> entry : result.getTrace()
					.getPredicateContextExecutionCount().entrySet()) {
				for (Entry<CallContext, Integer> value : entry.getValue().entrySet()) {
					int count = value.getValue();

					IBranchTestFitness goalT = getContextGoal(entry.getKey(), value.getKey(), true);
					if (goalT != null) {
						if (branchCounter.get(goalT.getGenericContextBranchIdentifier()) == null
								|| branchCounter.get(goalT.getGenericContextBranchIdentifier()) < count) {
							branchCounter.put(goalT.getGenericContextBranchIdentifier(), count);
						}
					} else {
						IBranchTestFitness goalF = getContextGoal(entry.getKey(), value.getKey(),
								false);
						if (goalF != null) {
							if (branchCounter.get(goalF.getGenericContextBranchIdentifier()) == null
									|| branchCounter.get(goalF.getGenericContextBranchIdentifier()) < count) {
								branchCounter.put(goalF.getGenericContextBranchIdentifier(), count);
							}
						} else
							continue;
					}
				}
			}

			//Count how many time any branchless method has been executed
			for (Entry<String, Map<CallContext, Integer>> entry : result.getTrace()
					.getMethodContextCount().entrySet()) {
				for (Entry<CallContext, Integer> value : entry.getValue().entrySet()) {
					IBranchTestFitness goal = getContextGoal(entry.getKey(), value.getKey());
					if (goal == null)
						continue;
					int count = value.getValue();
					if (callCounter.get(goal.hashCode()) == null
							|| callCounter.get(goal.hashCode()) < count) {
						callCounter.put(goal.hashCode(), count);
					}
					if (count > 0) {
						result.test.addCoveredGoal(goal);
					}
				}
			}
		}

		//Compute and return the fitness value
		int numCoveredGoals = 0;
		for (IBranchTestFitness goal : branchGoals) {

			Double distance = distanceMap.get(goal);
			if (distance == null)
				distance = 1.0;

			if (goal.getBranch() == null) {
				Integer count = callCounter.get(goal.hashCode());
				if (count == null || count == 0) {
					fitness += 1;
				} else {
					numCoveredGoals++;
				}
			} else {
				Integer count = branchCounter.get(goal.getGenericContextBranchIdentifier());
				if (count == null || count == 0)
					fitness += 1;
				else if (count == 1)
					fitness += 0.5;
				else {
					if (Double.compare(distance, 0.0) == 0) {
						numCoveredGoals++;
					}
					fitness += distance;
				}
			}
		}

		if (!branchGoals.isEmpty()) {
			suite.setCoverage(this, (double) numCoveredGoals / (double) branchGoals.size());
		}
		suite.setNumOfCoveredGoals(this, numCoveredGoals);
		suite.setNumOfNotCoveredGoals(this, branchGoals.size() - numCoveredGoals);
		updateIndividual(this, suite, fitness);
		return fitness;
	}
}
