/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.Properties;
import org.evosuite.ga.archive.Archive;
import org.evosuite.rmi.ClientServices;
import org.evosuite.setup.Call;
import org.evosuite.setup.CallContext;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

import java.util.*;
import java.util.Map.Entry;

/**
 * We don't remember what the I of IBranch stands for. Anyway, this fitness
 * function targets all the branches (of all classes) that is possible to reach
 * from the class under test.
 *
 * @author Gordon Fraser, mattia
 */
public class IBranchSuiteFitness extends TestSuiteFitnessFunction {

    private static final long serialVersionUID = 5836092966704859022L;

    private final List<IBranchTestFitness> branchGoals;

    private final int totGoals;

    private final Map<Integer, Map<CallContext, Set<IBranchTestFitness>>> goalsMap;

    /**
     * Branchless methods map.
     */
    private final Map<String, Map<CallContext, IBranchTestFitness>> methodsMap;

    private final Set<IBranchTestFitness> toRemoveBranchesT = new LinkedHashSet<>();
    private final Set<IBranchTestFitness> toRemoveBranchesF = new LinkedHashSet<>();
    private final Set<IBranchTestFitness> toRemoveRootBranches = new LinkedHashSet<>();

    private final Set<IBranchTestFitness> removedBranchesT = new LinkedHashSet<>();
    private final Set<IBranchTestFitness> removedBranchesF = new LinkedHashSet<>();
    private final Set<IBranchTestFitness> removedRootBranches = new LinkedHashSet<>();

    public IBranchSuiteFitness() {
        goalsMap = new LinkedHashMap<>();
        methodsMap = new LinkedHashMap<>();
        IBranchFitnessFactory factory = new IBranchFitnessFactory();
        branchGoals = factory.getCoverageGoals();
        countGoals(branchGoals);

        for (IBranchTestFitness goal : branchGoals) {
            if (goal.getBranchGoal() != null && goal.getBranchGoal().getBranch() != null) {
                int branchId = goal.getBranchGoal().getBranch().getActualBranchId();

                Map<CallContext, Set<IBranchTestFitness>> innermap =
                        goalsMap.computeIfAbsent(branchId, k -> new LinkedHashMap<>());
                Set<IBranchTestFitness> tempInSet =
                        innermap.computeIfAbsent(goal.getContext(), k -> new LinkedHashSet<>());
                tempInSet.add(goal);
            } else {
                String methodName = goal.getTargetClass() + "." + goal.getTargetMethod();
                Map<CallContext, IBranchTestFitness> innermap =
                        methodsMap.computeIfAbsent(methodName, k -> new LinkedHashMap<>());
                innermap.put(goal.getContext(), goal);
            }
            if (Properties.TEST_ARCHIVE) {
                Archive.getArchiveInstance().addTarget(goal);
            }
            logger.info("Context goal: " + goal);
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

    public double getFitness(TestSuiteChromosome suite, boolean updateChromosome) {
        double fitness = 0.0; // branchFitness.getFitness(suite);
        List<ExecutionResult> results = runTestSuite(suite);

        Map<IBranchTestFitness, Double> distanceMap = new LinkedHashMap<>();
        Map<IBranchTestFitness, Integer> callCount = new LinkedHashMap<>();

        for (ExecutionResult result : results) {
            if (result.hasTimeout() || result.hasTestException()) {
                continue;
            }

            TestChromosome test = new TestChromosome();
            test.setTestCase(result.test);
            test.setLastExecutionResult(result);
            test.setChanged(false);

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
                        if (updateChromosome)
                            test.getTestCase().addCoveredGoal(goalT);
                        toRemoveBranchesT.add(goalT);
                    }

                    if (Properties.TEST_ARCHIVE) {
                        Archive.getArchiveInstance().updateArchive(goalT, test, distanceT);
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
                        if (updateChromosome)
                            test.getTestCase().addCoveredGoal(goalF);
                        toRemoveBranchesF.add(goalF);
                    }

                    if (Properties.TEST_ARCHIVE) {
                        Archive.getArchiveInstance().updateArchive(goalF, test, distanceF);
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
                        if (updateChromosome)
                            result.test.addCoveredGoal(goal);
                        toRemoveRootBranches.add(goal);
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

        if (updateChromosome) {
            numCoveredGoals += removedBranchesF.size();
            numCoveredGoals += removedBranchesT.size();
            numCoveredGoals += removedRootBranches.size();
            if (totGoals > 0) {
                suite.setCoverage(this, (double) numCoveredGoals / (double) totGoals);
            }
            suite.setNumOfCoveredGoals(this, numCoveredGoals);
            suite.setNumOfNotCoveredGoals(this, totGoals - numCoveredGoals);
            updateIndividual(suite, fitness);
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
    public double getFitness(TestSuiteChromosome suite) {
        return getFitness(suite, true);
    }

    @Override
    public boolean updateCoveredGoals() {
        if (!Properties.TEST_ARCHIVE) {
            return false;
        }

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
