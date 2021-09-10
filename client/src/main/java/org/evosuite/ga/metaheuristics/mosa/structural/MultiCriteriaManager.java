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
package org.evosuite.ga.metaheuristics.mosa.structural;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.cbranch.CBranchTestFitness;
import org.evosuite.coverage.exception.ExceptionCoverageFactory;
import org.evosuite.coverage.exception.ExceptionCoverageHelper;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.exception.TryCatchCoverageTestFitness;
import org.evosuite.coverage.io.input.InputCoverageTestFitness;
import org.evosuite.coverage.io.output.OutputCoverageTestFitness;
import org.evosuite.coverage.line.LineCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.method.MethodNoExceptionCoverageTestFitness;
import org.evosuite.coverage.mutation.StrongMutationTestFitness;
import org.evosuite.coverage.mutation.WeakMutationTestFitness;
import org.evosuite.coverage.statement.StatementCoverageTestFitness;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.setup.CallContext;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.callgraph.CallGraph;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * A class for managing multiple coverage targets simultaneously.
 */
public class MultiCriteriaManager extends StructuralGoalManager implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(MultiCriteriaManager.class);

    private static final long serialVersionUID = 8161137239404885564L;

    protected BranchFitnessGraph graph;

    protected Map<BranchCoverageTestFitness, Set<TestFitnessFunction>> dependencies;

    /**
     * Maps branch IDs to the corresponding fitness function, only considering branches we want to
     * take.
     */
    protected final Map<Integer, TestFitnessFunction> branchCoverageTrueMap = new LinkedHashMap<>();

    /**
     * Maps branch IDs to the corresponding fitness function, only considering the branches we do
     * <em>not</em> want to take.
     */
    protected final Map<Integer, TestFitnessFunction> branchCoverageFalseMap = new LinkedHashMap<>();

    /**
     * Maps branch IDs to the corresponding fitness function, only considering root branches of
     * methods (i.e. the goal is to just invoke the method).
     */
    private final Map<String, TestFitnessFunction> branchlessMethodCoverageMap = new LinkedHashMap<>();

    /**
     * Creates a new {@code MultiCriteriaManager} with the given list of targets. The targets are
     * encoded as fitness functions, which are expected to be minimization functions.
     *
     * @param targets The targets to cover encoded as minimization functions
     */
    public MultiCriteriaManager(List<TestFitnessFunction> targets) {
        super(targets);

        // initialize the dependency graph among branches
        this.graph = getControlDependencies4Branches(targets);

        // initialize the dependency graph between branches and other coverage targets (e.g., statements)
        // let's derive the dependency graph between branches and other coverage targets (e.g., statements)
        for (Criterion criterion : Properties.CRITERION) {
            switch (criterion) {
                case BRANCH:
                    break; // branches have been handled by getControlDepencies4Branches
                case EXCEPTION:
                    break; // exception coverage is handled by calculateFitness
                case LINE:
                    addDependencies4Line();
                    break;
                case STATEMENT:
                    addDependencies4Statement();
                    break;
                case WEAKMUTATION:
                    addDependencies4WeakMutation();
                    break;
                case STRONGMUTATION:
                    addDependencies4StrongMutation();
                    break;
                case METHOD:
                    addDependencies4Methods();
                    break;
                case INPUT:
                    addDependencies4Input();
                    break;
                case OUTPUT:
                    addDependencies4Output();
                    break;
                case TRYCATCH:
                    addDependencies4TryCatch();
                    break;
                case METHODNOEXCEPTION:
                    addDependencies4MethodsNoException();
                    break;
                case CBRANCH:
                    addDependencies4CBranch();
                    break;
                default:
                    LoggingUtils.getEvoLogger().error("The criterion {} is not currently supported in DynaMOSA", criterion.name());
            }
        }

        // initialize current goals
        this.currentGoals.addAll(graph.getRootBranches());
    }

    private void addDependencies4TryCatch() {
        logger.debug("Added dependencies for Try-Catch");
        for (FitnessFunction<TestChromosome> ff : this.getUncoveredGoals()) {
            if (ff instanceof TryCatchCoverageTestFitness) {
                TryCatchCoverageTestFitness stmt = (TryCatchCoverageTestFitness) ff;
                BranchCoverageTestFitness branch = new BranchCoverageTestFitness(stmt.getBranchGoal());
                this.dependencies.get(branch).add(stmt);
            }
        }
    }

    private void initializeMaps(Set<TestFitnessFunction> set) {
        for (TestFitnessFunction ff : set) {
            BranchCoverageTestFitness goal = (BranchCoverageTestFitness) ff;

            // Skip instrumented branches - we only want real branches
            if (goal.getBranch() != null && goal.getBranch().isInstrumented()) {
                continue;
            }

            if (goal.getBranch() == null) { // the goal is to call the method at hand
                branchlessMethodCoverageMap.put(goal.getClassName() + "." + goal.getMethod(), ff);
            } else if (goal.getBranchExpressionValue()) { // we want to take the given branch
                branchCoverageTrueMap.put(goal.getBranch().getActualBranchId(), ff);
            } else { // we don't want to take the given branch
                branchCoverageFalseMap.put(goal.getBranch().getActualBranchId(), ff);
            }
        }
    }

    private void addDependencies4Output() {
        logger.debug("Added dependencies for Output");
        for (TestFitnessFunction ff : this.getUncoveredGoals()) {
            if (ff instanceof OutputCoverageTestFitness) {
                OutputCoverageTestFitness output = (OutputCoverageTestFitness) ff;
                ClassLoader loader = TestGenerationContext.getInstance().getClassLoaderForSUT();
                BytecodeInstructionPool pool = BytecodeInstructionPool.getInstance(loader);
                if (pool.getInstructionsIn(output.getClassName(), output.getMethod()) == null) {
                    this.currentGoals.add(ff);
                    continue;
                }
                for (BytecodeInstruction instruction : pool.getInstructionsIn(output.getClassName(), output.getMethod())) {
                    if (instruction.getBasicBlock() != null) {
                        Set<ControlDependency> cds = instruction.getBasicBlock().getControlDependencies();
                        if (cds.size() == 0) {
                            this.currentGoals.add(ff);
                        } else {
                            for (ControlDependency cd : cds) {
                                BranchCoverageTestFitness fitness = BranchCoverageFactory.createBranchCoverageTestFitness(cd);
                                this.dependencies.get(fitness).add(ff);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This methods derive the dependencies between {@link InputCoverageTestFitness} and branches.
     * Therefore, it is used to update 'this.dependencies'
     */
    private void addDependencies4Input() {
        logger.debug("Added dependencies for Input");
        for (TestFitnessFunction ff : this.getUncoveredGoals()) {
            if (ff instanceof InputCoverageTestFitness) {
                InputCoverageTestFitness input = (InputCoverageTestFitness) ff;
                ClassLoader loader = TestGenerationContext.getInstance().getClassLoaderForSUT();
                BytecodeInstructionPool pool = BytecodeInstructionPool.getInstance(loader);
                if (pool.getInstructionsIn(input.getClassName(), input.getMethod()) == null) {
                    this.currentGoals.add(ff);
                    continue;
                }
                for (BytecodeInstruction instruction : pool.getInstructionsIn(input.getClassName(), input.getMethod())) {
                    if (instruction.getBasicBlock() != null) {
                        Set<ControlDependency> cds = instruction.getBasicBlock().getControlDependencies();
                        if (cds.size() == 0) {
                            this.currentGoals.add(ff);
                        } else {
                            for (ControlDependency cd : cds) {
                                BranchCoverageTestFitness fitness = BranchCoverageFactory.createBranchCoverageTestFitness(cd);
                                this.dependencies.get(fitness).add(ff);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This methods derive the dependencies between {@link MethodCoverageTestFitness} and branches.
     * Therefore, it is used to update 'this.dependencies'
     */
    private void addDependencies4Methods() {
        logger.debug("Added dependencies for Methods");
        for (BranchCoverageTestFitness branch : this.dependencies.keySet()) {
            MethodCoverageTestFitness method = new MethodCoverageTestFitness(branch.getClassName(), branch.getMethod());
            this.dependencies.get(branch).add(method);
        }
    }

    /**
     * This methods derive the dependencies between {@link MethodNoExceptionCoverageTestFitness} and branches.
     * Therefore, it is used to update 'this.dependencies'
     */
    private void addDependencies4MethodsNoException() {
        logger.debug("Added dependencies for MethodsNoException");
        for (BranchCoverageTestFitness branch : this.dependencies.keySet()) {
            MethodNoExceptionCoverageTestFitness method = new MethodNoExceptionCoverageTestFitness(branch.getClassName(), branch.getMethod());
            this.dependencies.get(branch).add(method);
        }
    }

    /**
     * This methods derive the dependencies between {@link CBranchTestFitness} and branches.
     * Therefore, it is used to update 'this.dependencies'
     */
    private void addDependencies4CBranch() {
        logger.debug("Added dependencies for CBranch");
        CallGraph callGraph = DependencyAnalysis.getCallGraph();
        for (BranchCoverageTestFitness branch : this.dependencies.keySet()) {
            for (CallContext context : callGraph.getMethodEntryPoint(branch.getClassName(), branch.getMethod())) {
                CBranchTestFitness cBranch = new CBranchTestFitness(branch.getBranchGoal(), context);
                this.dependencies.get(branch).add(cBranch);
                logger.debug("Added context branch: " + cBranch);
            }
        }
    }

    /**
     * This methods derive the dependencies between {@link WeakMutationTestFitness} and branches.
     * Therefore, it is used to update 'this.dependencies'
     */
    private void addDependencies4WeakMutation() {
        logger.debug("Added dependencies for Weak-Mutation");
        for (TestFitnessFunction ff : this.getUncoveredGoals()) {
            if (ff instanceof WeakMutationTestFitness) {
                WeakMutationTestFitness mutation = (WeakMutationTestFitness) ff;
                Set<BranchCoverageGoal> goals = mutation.getMutation().getControlDependencies();
                if (goals.size() == 0) {
                    this.currentGoals.add(ff);
                } else {
                    for (BranchCoverageGoal goal : goals) {
                        BranchCoverageTestFitness fitness = new BranchCoverageTestFitness(goal);
                        this.dependencies.get(fitness).add(ff);
                    }
                }
            }
        }
    }

    /**
     * This methods derive the dependencies between {@link org.evosuite.coverage.mutation.StrongMutationTestFitness} and branches.
     * Therefore, it is used to update 'this.dependencies'
     */
    private void addDependencies4StrongMutation() {
        logger.debug("Added dependencies for Strong-Mutation");
        for (TestFitnessFunction ff : this.getUncoveredGoals()) {
            if (ff instanceof StrongMutationTestFitness) {
                StrongMutationTestFitness mutation = (StrongMutationTestFitness) ff;
                Set<BranchCoverageGoal> goals = mutation.getMutation().getControlDependencies();
                if (goals.size() == 0) {
                    this.currentGoals.add(ff);
                } else {
                    for (BranchCoverageGoal goal : goals) {
                        BranchCoverageTestFitness fitness = new BranchCoverageTestFitness(goal);
                        this.dependencies.get(fitness).add(ff);
                    }
                }
            }
        }
    }

    /**
     * This methods derive the dependencies between  {@link LineCoverageTestFitness} and branches.
     * Therefore, it is used to update 'this.dependencies'
     */
    private void addDependencies4Line() {
        logger.debug("Added dependencies for Lines");
        for (TestFitnessFunction ff : this.getUncoveredGoals()) {
            if (ff instanceof LineCoverageTestFitness) {
                LineCoverageTestFitness line = (LineCoverageTestFitness) ff;
                ClassLoader loader = TestGenerationContext.getInstance().getClassLoaderForSUT();
                BytecodeInstructionPool pool = BytecodeInstructionPool.getInstance(loader);
                BytecodeInstruction instruction = pool.getFirstInstructionAtLineNumber(line.getClassName(), line.getMethod(), line.getLine());
                Set<ControlDependency> cds = instruction.getControlDependencies();
                if (cds.size() == 0)
                    this.currentGoals.add(ff);
                else {
                    for (ControlDependency cd : cds) {
                        BranchCoverageTestFitness fitness = BranchCoverageFactory.createBranchCoverageTestFitness(cd);
                        this.dependencies.get(fitness).add(ff);
                    }
                }
            }
        }
    }

    /**
     * This methods derive the dependencies between  {@link StatementCoverageTestFitness} and branches.
     * Therefore, it is used to update 'this.dependencies'
     */
    private void addDependencies4Statement() {
        logger.debug("Added dependencies for Statements");
        for (TestFitnessFunction ff : this.getUncoveredGoals()) {
            if (ff instanceof StatementCoverageTestFitness) {
                StatementCoverageTestFitness stmt = (StatementCoverageTestFitness) ff;
                if (stmt.getBranchFitnesses().size() == 0)
                    this.currentGoals.add(ff);
                else {
                    for (BranchCoverageTestFitness branch : stmt.getBranchFitnesses()) {
                        this.dependencies.get(branch).add(stmt);
                    }
                }
            }
        }
    }


    /**
     * Calculates the fitness of the given test chromosome w.r.t. the current set of goals. To this
     * end, the test chromosome is executed, it's execution trace recorded and the resulting
     * coverage analyzed. This information is further used to update the set of current
     * goals (as given by {@link MultiCriteriaManager#getCurrentGoals()} and the population of the
     * archive.
     *
     * @param c the chromosome whose fitness to calculate (must be a {@link TestChromosome})
     */
    @Override
    public void calculateFitness(TestChromosome c, GeneticAlgorithm<TestChromosome> ga) {
        // Run the test and record the execution result.
        TestCase test = c.getTestCase();
        ExecutionResult result = TestCaseExecutor.runTest(test);
        c.setLastExecutionResult(result);
        c.setChanged(false);

        // If the test failed to execute properly, or if the test does not cover anything,
        // it means none of the current gaols could be reached.
        if (result.hasTimeout() || result.hasTestException() || result.getTrace().getCoveredLines().size() == 0) {
            currentGoals.forEach(f -> c.setFitness(f, Double.MAX_VALUE)); // assume minimization
            return;
        }

        Set<TestFitnessFunction> visitedTargets = new LinkedHashSet<>(getUncoveredGoals().size() * 2);

        /*
         * The processing list of current targets. If it turns out that any such target has been
         * reached, we also enqueue its structural and control-dependent children. This is to
         * determine which of those children are already reached by control flow. Only the missed
         * children will be part of the currentGoals for the next generation (together with the
         * missed goals of the currentGoals of the current generation).
         */
        LinkedList<TestFitnessFunction> targets = new LinkedList<>(this.currentGoals);

        // 1) We update the set of current goals.
        while (targets.size() > 0 && !ga.isFinished()) {
            // We evaluate the given test case against all current targets.
            // (There might have been serendipitous coverage of other targets, though.)
            TestFitnessFunction target = targets.poll();

            int pastSize = visitedTargets.size();
            visitedTargets.add(target);
            if (pastSize == visitedTargets.size())
                continue;

            double fitness = target.getFitness(c);

            /*
             * Checks if the current test target has been reached and, in accordance, marks it as
             * covered or uncovered.
             */
            if (fitness == 0.0) { // assume minimization function
                updateCoveredGoals(target, c); // marks the current goal as covered

                /*
                 * If the coverage criterion is branch coverage, we also add structural children
                 * and control dependencies of the current target to the processing queue. This is
                 * to see which ones of those goals are already reached by control flow.
                 */
                if (target instanceof BranchCoverageTestFitness) {
                    for (TestFitnessFunction child : graph.getStructuralChildren(target)) {
                        targets.addLast(child);
                    }
                    for (TestFitnessFunction dependentTarget : dependencies.get(target)) {
                        targets.addLast(dependentTarget);
                    }
                }
            } else {
                currentGoals.add(target); // marks the goal as uncovered
            }
        }

        // Removes all newly covered goals from the list of currently uncovered goals.
        currentGoals.removeAll(this.getCoveredGoals());

        // 2) We update the archive.
        final ExecutionTrace trace = result.getTrace();
        for (int branchid : trace.getCoveredFalseBranches()) {
            TestFitnessFunction branch = this.branchCoverageFalseMap.get(branchid);
            if (branch == null)
                continue;
            updateCoveredGoals(branch, c);
        }
        for (int branchid : trace.getCoveredTrueBranches()) {
            TestFitnessFunction branch = this.branchCoverageTrueMap.get(branchid);
            if (branch == null)
                continue;
            updateCoveredGoals(branch, c);
        }
        for (String method : trace.getCoveredBranchlessMethods()) {
            TestFitnessFunction branch = this.branchlessMethodCoverageMap.get(method);
            if (branch == null)
                continue;
            updateCoveredGoals(branch, c);
        }

        // let's manage the exception coverage
        if (ArrayUtil.contains(Properties.CRITERION, Criterion.EXCEPTION)) {
            // if one of the coverage criterion is Criterion.EXCEPTION,
            // then we have to analyze the results of the execution do look
            // for generated exceptions
            Set<ExceptionCoverageTestFitness> set = deriveCoveredExceptions(c);
            for (ExceptionCoverageTestFitness exp : set) {
                // let's update the list of fitness functions
                updateCoveredGoals(exp, c);
                // new covered exceptions (goals) have to be added to the archive
                if (!ExceptionCoverageFactory.getGoals().containsKey(exp.getKey())) {
                    // let's update the newly discovered exceptions to ExceptionCoverageFactory
                    ExceptionCoverageFactory.getGoals().put(exp.getKey(), exp);
                }
            }
        }
    }

    /**
     * This method analyzes the execution results of a TestChromosome looking for generated exceptions.
     * Such exceptions are converted in instances of the class {@link ExceptionCoverageTestFitness},
     * which are additional covered goals when using as criterion {@link Properties.Criterion Exception}
     *
     * @param t TestChromosome to analyze
     * @return list of exception goals being covered by t
     */
    public Set<ExceptionCoverageTestFitness> deriveCoveredExceptions(TestChromosome t) {
        Set<ExceptionCoverageTestFitness> covered_exceptions = new LinkedHashSet<>();
        ExecutionResult result = t.getLastExecutionResult();

        if (result.calledReflection())
            return covered_exceptions;

        for (Integer i : result.getPositionsWhereExceptionsWereThrown()) {
            if (ExceptionCoverageHelper.shouldSkip(result, i)) {
                continue;
            }

            Class<?> exceptionClass = ExceptionCoverageHelper.getExceptionClass(result, i);
            String methodIdentifier = ExceptionCoverageHelper.getMethodIdentifier(result, i); //eg name+descriptor
            boolean sutException = ExceptionCoverageHelper.isSutException(result, i); // was the exception originated by a direct call on the SUT?

            /*
             * We only consider exceptions that were thrown by calling directly the SUT (not the other
             * used libraries). However, this would ignore cases in which the SUT is indirectly tested
             * through another class
             */

            if (sutException) {

                ExceptionCoverageTestFitness.ExceptionType type = ExceptionCoverageHelper.getType(result, i);
                /*
                 * Add goal to list of fitness functions to solve
                 */
                ExceptionCoverageTestFitness goal = new ExceptionCoverageTestFitness(Properties.TARGET_CLASS, methodIdentifier, exceptionClass, type);
                covered_exceptions.add(goal);
            }
        }
        return covered_exceptions;
    }

    public BranchFitnessGraph getControlDependencies4Branches(List<TestFitnessFunction> fitnessFunctions) {
        Set<TestFitnessFunction> setOfBranches = new LinkedHashSet<>();
        this.dependencies = new LinkedHashMap<>();

        List<BranchCoverageTestFitness> branches = new BranchCoverageFactory().getCoverageGoals();
        for (BranchCoverageTestFitness branch : branches) {
            setOfBranches.add(branch);
            this.dependencies.put(branch, new LinkedHashSet<>());
        }

        // initialize the maps
        this.initializeMaps(setOfBranches);

        return new BranchFitnessGraph(setOfBranches);
    }
}
