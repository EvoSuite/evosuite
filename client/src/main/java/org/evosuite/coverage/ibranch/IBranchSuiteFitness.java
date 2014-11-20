/**
 * 
 */
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
 * We don't remember what the I of IBranch stands for. Anyway, this fitness function targets all
 * the branches (of all classes) that is possible to reach from the class under test.
 * @author Gordon Fraser, mattia
 * 
 */
public class IBranchSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = -4745892521350308986L;

	private final List<IBranchTestFitness> branchGoals; 
	
	private final Map<Integer, Map<CallContext, Set<IBranchTestFitness>>> goalsMap;

	private final Map<String, Map<CallContext, Set<IBranchTestFitness>>> methodsMap;
 
	public IBranchSuiteFitness() {
		goalsMap = new HashMap<>();
		methodsMap = new HashMap<>();
		IBranchFitnessFactory factory = new IBranchFitnessFactory();
		branchGoals = factory.getCoverageGoals();
        countGoals(branchGoals);
		for (IBranchTestFitness goal : branchGoals) {
			if (goal.getBranchGoal() != null&&goal.getBranchGoal().getBranch()!=null) {
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
			}else{ 
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
	}

    private void countGoals(List<IBranchTestFitness> branchGoals) {
        int totalGoals = branchGoals.size();
        int goalsInTarget = 0;
        for (IBranchTestFitness g : branchGoals) {
            boolean flag = true;
            for (Call call : g.getContext().getContext()) {
                if (! call.getClassName().equals(Properties.TARGET_CLASS)) {
                    flag = false;
                    break;
                }
            }
            if (flag)
                goalsInTarget++;
        }
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.IBranchInitialGoals, totalGoals);
        ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.IBranchInitialGoalsInTargetClass, goalsInTarget);

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
		if(methodsMap.get(classAndMethodName)==null) return null;
		if(methodsMap.get(classAndMethodName).get(context)==null)return null;
		
		for (IBranchTestFitness iBranchTestFitness : methodsMap.get(classAndMethodName).get(context)) {
			return iBranchTestFitness;
		}
		return null;
	}
	
	private IBranchTestFitness getContextGoal(Integer branchId, CallContext context,
	        boolean value) {
		if(goalsMap.get(branchId)==null) return null;
		if(goalsMap.get(branchId).get(context)==null) return null;
		for (IBranchTestFitness iBranchTestFitness : goalsMap.get(branchId).get(context)) {
			if(iBranchTestFitness.getValue()==value){
				return iBranchTestFitness;
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
		Map<IBranchTestFitness, Double> distanceMap = getDefaultDistanceMap();

		Map<IBranchTestFitness, Integer> callCount = getDefaultCallCountMap();

		for (ExecutionResult result : results) {
			// Determine minimum branch distance for each branch in each context
			for (Entry<Integer, Map<CallContext, Double>> entry : result.getTrace().getTrueDistancesContext().entrySet()) {
				for (Entry<CallContext, Double> value : entry.getValue().entrySet()) {
					
					IBranchTestFitness goal = getContextGoal(entry.getKey(),
					                                         value.getKey(), true);
					if(goal==null) continue;
					double distance = normalize(value.getValue());
					if (distanceMap.get(goal) > distance) {
						distanceMap.put(goal, distance);
					}
				}
			}
			for (Entry<Integer, Map<CallContext, Double>> entry : result.getTrace().getFalseDistancesContext().entrySet()) {
				for (Entry<CallContext, Double> value : entry.getValue().entrySet()) {
					
					IBranchTestFitness goal = getContextGoal(entry.getKey(),
					                                         value.getKey(), false);
					if(goal==null) continue;
					double distance = normalize(value.getValue());
					
					if (distanceMap.get(goal) > distance) {
						distanceMap.put(goal, distance);
					}
				}
			}
//
			// Determine maximum execution count for each branch in each context
			for (Entry<Integer, Map<CallContext, Integer>> entry : result.getTrace().getPredicateContextExecutionCount().entrySet()) {
				for (Entry<CallContext, Integer> value : entry.getValue().entrySet()) {
					IBranchTestFitness goal = getContextGoal(entry.getKey(),
					                                         value.getKey(), true);
					if(goal==null) continue;
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
