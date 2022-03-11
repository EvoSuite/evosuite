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

package org.evosuite;

import org.evosuite.assertion.InspectorManager;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.contracts.ContractChecker;
import org.evosuite.contracts.FailingTestSet;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import org.evosuite.ga.archive.Archive;
import org.evosuite.ga.stoppingconditions.GlobalTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.instrumentation.LinePool;
import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.classhandling.ModifiedTargetStaticFields;
import org.evosuite.runtime.instrumentation.MethodCallReplacementCache;
import org.evosuite.runtime.instrumentation.RemoveFinalClassAdapter;
import org.evosuite.runtime.util.JOptionPaneInputs;
import org.evosuite.runtime.util.SystemInUtil;
import org.evosuite.seeding.CastClassManager;
import org.evosuite.seeding.ConstantPoolManager;
import org.evosuite.seeding.ObjectPoolManager;
import org.evosuite.setup.ConcreteClassAnalyzer;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.setup.TestClusterGenerator;
import org.evosuite.symbolic.dse.DSEStatistics;
import org.evosuite.testcarver.extraction.CarvingManager;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.execution.reset.ClassReInitializer;
import org.evosuite.utils.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Gordon Fraser
 */
public class TestGenerationContext {

    private static final Logger logger = LoggerFactory.getLogger(TestGenerationContext.class);

    private static final TestGenerationContext singleton = new TestGenerationContext();

    /**
     * This is the classloader that does the instrumentation - it needs to be
     * used by all test code
     */
    private InstrumentingClassLoader classLoader;

    /**
     * The classloader used to load this class
     */
    private final ClassLoader originalClassLoader;

    /**
     * To avoid duplicate analyses we cache the cluster generator
     */
    private TestClusterGenerator testClusterGenerator;

    /**
     * Private singleton constructor
     */
    private TestGenerationContext() {
        originalClassLoader = this.getClass().getClassLoader();
        classLoader = new InstrumentingClassLoader();
    }

    public static TestGenerationContext getInstance() {
        return singleton;
    }

    /**
     * This is pretty important if the SUT use classloader of the running
     * thread. If we do not set this up, we will end up with cast exceptions.
     *
     * <p>
     * Note, an example in which this happens is in
     *
     * <p>
     * org.dom4j.bean.BeanAttribute
     *
     * <p>
     * in SF100 project 62_dom4j
     */
    public void goingToExecuteSUTCode() {

        Thread.currentThread().setContextClassLoader(classLoader);
    }

    public void doneWithExecutingSUTCode() {
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    public InstrumentingClassLoader getClassLoaderForSUT() {
        return classLoader;
    }

    public TestClusterGenerator getTestClusterGenerator() {
        return testClusterGenerator;
    }

    public void setTestClusterGenerator(TestClusterGenerator generator) {
        testClusterGenerator = generator;
    }

    /**
     * @return
     * @deprecated use {@code getInstance().getClassLoaderForSUT()}
     */
    public static ClassLoader getClassLoader() {
        return getInstance().classLoader;
    }

    public void resetContext() {
        logger.info("*** Resetting context");

        // A fresh context needs a fresh class loader to make sure we can
        // re-instrument classes
        classLoader = new InstrumentingClassLoader();

        TestCaseExecutor.pullDown();

        ExecutionTracer.getExecutionTracer().clear();

        // TODO: BranchPool should not be static
        BranchPool.getInstance(classLoader).reset();
        RemoveFinalClassAdapter.reset();
        LinePool.reset();
        MutationPool.getInstance(classLoader).clear();

        // TODO: Clear only pool of current classloader?
        GraphPool.clearAll();
        DefUsePool.clear();

        // TODO: This is not nice
        for (ClassLoader cl : CFGMethodAdapter.methods.keySet())
            CFGMethodAdapter.methods.get(cl).clear();

        // TODO: Clear only pool of current classloader?
        BytecodeInstructionPool.clearAll();

        // TODO: After this, the test cluster is empty until
        // DependencyAnalysis.analyse is called
        TestCluster.reset();
        CastClassManager.getInstance().clear();
        ConcreteClassAnalyzer.getInstance().clear();
        // This counts the current level of recursion during test generation
        org.evosuite.testcase.TestFactory.getInstance().reset();

        MaxStatementsStoppingCondition.setNumExecutedStatements(0);
        GlobalTimeStoppingCondition.forceReset();
        MutationTimeoutStoppingCondition.resetStatic();

        // Forget the old SUT
        Properties.resetTargetClass();

        TestCaseExecutor.initExecutor();

        Archive.getArchiveInstance().reset();

        // Constant pool
        ConstantPoolManager.getInstance().reset();
        ObjectPoolManager.getInstance().reset();
        CarvingManager.getInstance().clear();

        // TODO: Why are we doing this?
        if (Properties.INSTRUMENT_CONTEXT || ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.DEFUSE)
                || ArrayUtil.contains(Properties.CRITERION, Properties.Criterion.IBRANCH)) {
            // || ArrayUtil.contains(Properties.CRITERION,
            // Properties.Criterion.CBRANCH)) {
            try {
                // 1. Initialize the callGraph before using
                String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();
                DependencyAnalysis.analyzeClass(Properties.TARGET_CLASS, Arrays.asList(cp.split(File.pathSeparator)));
                testClusterGenerator = new TestClusterGenerator(
                        DependencyAnalysis.getInheritanceTree());
                // 2. Use the callGraph
                testClusterGenerator.generateCluster(DependencyAnalysis.getCallGraph());
            } catch (RuntimeException e) {
                logger.error(e.getMessage(), e);
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (Properties.CHECK_CONTRACTS) {
            FailingTestSet.changeClassLoader(classLoader);
        }
        ContractChecker.setActive(true);

        SystemInUtil.resetSingleton();
        JOptionPaneInputs.resetSingleton();
        Runtime.resetSingleton();
        MethodCallReplacementCache.resetSingleton();

        DSEStatistics.clear();

        // keep the list of initialized classes (clear them when needed in
        // the system test cases)
        final List<String> initializedClasses = ClassReInitializer.getInstance().getInitializedClasses();
        ClassReInitializer.resetSingleton();
        ClassReInitializer.getInstance().addInitializedClasses(initializedClasses);

        InspectorManager.resetSingleton();
        ModifiedTargetStaticFields.resetSingleton();
    }
}
