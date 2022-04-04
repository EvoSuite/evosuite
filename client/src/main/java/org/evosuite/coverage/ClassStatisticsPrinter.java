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

package org.evosuite.coverage;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.line.LineCoverageFactory;
import org.evosuite.coverage.line.LineCoverageTestFitness;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.evosuite.instrumentation.LinePool;
import org.evosuite.runtime.classhandling.ClassResetter;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.utils.FileIOUtils;
import org.evosuite.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import static java.util.Comparator.comparingInt;

/**
 * @author Gordon Fraser
 */
public class ClassStatisticsPrinter {

    private static final Logger logger = LoggerFactory.getLogger(ClassStatisticsPrinter.class);

    /**
     * Identify all JUnit tests starting with the given name prefix, instrument
     * and run tests
     */
    public static void printClassStatistics() {
        ExecutionTracer.disable();
        ExecutionTracer.setCheckCallerThread(false);
        try {
            DependencyAnalysis.analyzeClass(Properties.TARGET_CLASS,
                    Arrays.asList(ClassPathHandler.getInstance().getClassPathElementsForTargetProject()));

            Sandbox.goingToExecuteSUTCode();
            TestGenerationContext.getInstance().goingToExecuteSUTCode();
            Sandbox.goingToExecuteUnsafeCodeOnSameThread();
            // Load SUT without initialising it
            Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
            if (targetClass != null) {
                LoggingUtils.getEvoLogger().info("* Finished analyzing classpath");
            } else {
                LoggingUtils.getEvoLogger().info("* Error while initializing target class, not continuing");
                return;
            }
            int publicMethods = 0;
            int nonpublicMethods = 0;
            int staticMethods = 0;
            int staticFields = 0;
            for (Method method : targetClass.getDeclaredMethods()) {
                if (method.getName().equals(ClassResetter.STATIC_RESET))
                    continue;
                if (Modifier.isPublic(method.getModifiers())) {
                    publicMethods++;
                } else {
                    nonpublicMethods++;
                }
                if (Modifier.isStatic(method.getModifiers())) {
                    LoggingUtils.getEvoLogger().info("Static: " + method);
                    staticMethods++;
                }

            }
            for (Constructor<?> constructor : targetClass.getDeclaredConstructors()) {
                if (Modifier.isPublic(constructor.getModifiers())) {
                    publicMethods++;
                } else {
                    nonpublicMethods++;
                }
            }
            for (Field field : targetClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    staticFields++;
                }
            }
            LoggingUtils.getEvoLogger().info("* Abstract: " + Modifier.isAbstract(targetClass.getModifiers()));
            LoggingUtils.getEvoLogger().info("* Public methods/constructors: " + publicMethods);
            LoggingUtils.getEvoLogger().info("* Non-Public methods/constructors: " + nonpublicMethods);
            LoggingUtils.getEvoLogger().info("* Static methods: " + staticMethods);
            LoggingUtils.getEvoLogger().info("* Inner classes: " + targetClass.getDeclaredClasses().length);
            LoggingUtils.getEvoLogger().info("* Total fields: " + targetClass.getDeclaredFields().length);
            LoggingUtils.getEvoLogger().info("* Static fields: " + staticFields);
            LoggingUtils.getEvoLogger().info("* Type parameters: " + targetClass.getTypeParameters().length);

        } catch (Throwable e) {
            LoggingUtils.getEvoLogger().error("* Error while initializing target class: "
                    + (e.getMessage() != null ? e.getMessage()
                    : e.toString()));
            return;
        } finally {
            Sandbox.doneWithExecutingUnsafeCodeOnSameThread();
            Sandbox.doneWithExecutingSUTCode();
            TestGenerationContext.getInstance().doneWithExecutingSUTCode();
        }

        LoggingUtils.getEvoLogger().info("* Subclasses: " + (TestCluster.getInheritanceTree().getSubclasses(Properties.TARGET_CLASS).size() - 1));
        LoggingUtils.getEvoLogger().info("* Superclasses/interfaces: " + (TestCluster.getInheritanceTree().getSuperclasses(Properties.TARGET_CLASS).size() - 1));
        LoggingUtils.getEvoLogger().info("* Lines of code: " + LinePool.getNumLines());
        LoggingUtils.getEvoLogger().info("* Methods without branches: " + BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getNumBranchlessMethods());
        LoggingUtils.getEvoLogger().info("* Total branch predicates: " + BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchCounter());


        double complexity = 0.0;
        int maxComplexity = 0;
        for (Entry<String, RawControlFlowGraph> entry : GraphPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getRawCFGs(Properties.TARGET_CLASS).entrySet()) {
            int c = entry.getValue().getCyclomaticComplexity();
            if (c > maxComplexity)
                maxComplexity = c;
            complexity += c;
            // LoggingUtils.getEvoLogger().info("* Complexity of method "+entry.getKey()+": "+entry.getValue().getCyclomaticComplexity());
        }
        LoggingUtils.getEvoLogger().info("* Average cyclomatic complexity: " + (complexity / CFGMethodAdapter.getNumMethods(TestGenerationContext.getInstance().getClassLoaderForSUT())));
        LoggingUtils.getEvoLogger().info("* Maximum cyclomatic complexity: " + maxComplexity);

        StringBuilder allGoals = new StringBuilder();

        List<TestFitnessFactory<? extends TestFitnessFunction>> factories =
                TestGenerationStrategy.getFitnessFactories();

        int numCriterion = 0;
        for (TestFitnessFactory<? extends TestFitnessFunction> factory : factories) {
            List<? extends TestFitnessFunction> goals = factory.getCoverageGoals();
            LoggingUtils.getEvoLogger().info("* Criterion " + Properties.CRITERION[numCriterion++] + ": " + goals.size());
            if (Properties.PRINT_GOALS) {
                if (factory instanceof LineCoverageFactory) {
                    goals.sort(comparingInt(l -> ((LineCoverageTestFitness) l).getLine()));
                }
                for (TestFitnessFunction goal : goals) {
                    allGoals.append(goal.toString() + java.lang.System.getProperty("line.separator"));
                }
            }
        }

        if (allGoals.length() > 0 && Properties.PRINT_GOALS) {
            if (Properties.WRITE_ALL_GOALS_FILE) {
                FileIOUtils.writeFile(allGoals.toString(), Properties.ALL_GOALS_FILE);
            } else {
                LoggingUtils.getEvoLogger().info(allGoals.toString());
            }
        }
    }
}
