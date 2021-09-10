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
package org.evosuite.coverage.io.output;

import org.apache.commons.lang3.ClassUtils;
import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.assertion.CheapPurityAnalyzer;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorManager;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.setup.TestClusterUtils;
import org.evosuite.setup.TestUsageChecker;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.evosuite.coverage.io.IOCoverageConstants.*;

/**
 * @author Jose Miguel Rojas
 */
public class OutputCoverageFactory extends AbstractFitnessFactory<OutputCoverageTestFitness> {

    private static final Logger logger = LoggerFactory.getLogger(OutputCoverageFactory.class);

    /*
     * (non-Javadoc)
     *
     * @see
     * org.evosuite.coverage.TestCoverageFactory#getCoverageGoals()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OutputCoverageTestFitness> getCoverageGoals() {
        List<OutputCoverageTestFitness> goals = new ArrayList<>();

        long start = System.currentTimeMillis();
        String targetClass = Properties.TARGET_CLASS;

        for (String className : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownClasses()) {
            if (!(targetClass.equals("") || className.endsWith(targetClass)))
                continue;

            for (Method method : TestClusterUtils.getClass(className).getDeclaredMethods()) {
                String methodName = method.getName() + Type.getMethodDescriptor(method);
                if (!TestUsageChecker.canUse(method) || methodName.equals("hashCode()I"))
                    continue;
                logger.info("Adding goals for method " + className + "." + methodName);
                Type returnType = Type.getReturnType(method);

                int typeSort = returnType.getSort();
                if (typeSort == Type.OBJECT) {
                    Class<?> typeClass = method.getReturnType();
                    if (ClassUtils.isPrimitiveWrapper(typeClass)) {
                        typeSort = Type.getType(ClassUtils.wrapperToPrimitive(typeClass)).getSort();
                        goals.add(createGoal(className, methodName, returnType, REF_NULL));
                    }
                }

                switch (typeSort) {
                    case Type.BOOLEAN:
                        goals.add(createGoal(className, methodName, returnType, BOOL_TRUE));
                        goals.add(createGoal(className, methodName, returnType, BOOL_FALSE));
                        break;
                    case Type.CHAR:
                        goals.add(createGoal(className, methodName, returnType, CHAR_ALPHA));
                        goals.add(createGoal(className, methodName, returnType, CHAR_DIGIT));
                        goals.add(createGoal(className, methodName, returnType, CHAR_OTHER));
                        break;
                    case Type.BYTE:
                    case Type.SHORT:
                    case Type.INT:
                    case Type.FLOAT:
                    case Type.LONG:
                    case Type.DOUBLE:
                        goals.add(createGoal(className, methodName, returnType, NUM_NEGATIVE));
                        goals.add(createGoal(className, methodName, returnType, NUM_ZERO));
                        goals.add(createGoal(className, methodName, returnType, NUM_POSITIVE));
                        break;
                    case Type.ARRAY:
                        goals.add(createGoal(className, methodName, returnType, REF_NULL));
                        goals.add(createGoal(className, methodName, returnType, ARRAY_EMPTY));
                        goals.add(createGoal(className, methodName, returnType, ARRAY_NONEMPTY));
                        break;
                    case Type.OBJECT:
                        goals.add(createGoal(className, methodName, returnType, REF_NULL));
                        //goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), REF_NONNULL)));
                        if (returnType.getClassName().equals("java.lang.String")) {
                            goals.add(createGoal(className, methodName, returnType, STRING_EMPTY));
                            goals.add(createGoal(className, methodName, returnType, STRING_NONEMPTY));
                            break;
                        }
                        boolean observerGoalsAdded = false;
                        Class<?> returnClazz = method.getReturnType();
                        for (Inspector inspector : InspectorManager.getInstance().getInspectors(returnClazz)) {
                            String insp = inspector.getMethodCall() + Type.getMethodDescriptor(inspector.getMethod());
                            Type t = Type.getReturnType(inspector.getMethod());
                            if (t.getSort() == Type.BOOLEAN) {
                                goals.add(createGoal(className, methodName, returnType, REF_NONNULL + ":" + returnType.getClassName() + ":" + insp + ":" + BOOL_TRUE));
                                goals.add(createGoal(className, methodName, returnType, REF_NONNULL + ":" + returnType.getClassName() + ":" + insp + ":" + BOOL_FALSE));
                                observerGoalsAdded = true;
                            } else if (Arrays.asList(new Integer[]{Type.BYTE, Type.SHORT, Type.INT, Type.FLOAT, Type.LONG, Type.DOUBLE}).contains(t.getSort())) {
                                goals.add(createGoal(className, methodName, returnType, REF_NONNULL + ":" + returnType.getClassName() + ":" + insp + ":" + NUM_NEGATIVE));
                                goals.add(createGoal(className, methodName, returnType, REF_NONNULL + ":" + returnType.getClassName() + ":" + insp + ":" + NUM_ZERO));
                                goals.add(createGoal(className, methodName, returnType, REF_NONNULL + ":" + returnType.getClassName() + ":" + insp + ":" + NUM_POSITIVE));
                                observerGoalsAdded = true;
                            }
                        }
                        if (!observerGoalsAdded)
                            goals.add(createGoal(className, methodName, returnType, REF_NONNULL));
                        break;
                    default:
                        // Ignore
                        // TODO: what to do with the sort for METHOD?
                        break;
                }
            }
        }
        goalComputationTime = System.currentTimeMillis() - start;
        return goals;
    }

    public static OutputCoverageTestFitness createGoal(String className, String methodName, Type returnType, String suffix) {
        OutputCoverageGoal goal = new OutputCoverageGoal(className, methodName, returnType, suffix);
        logger.info("Created output coverage goal: {}", goal);
        return new OutputCoverageTestFitness(goal);
    }

    /**
     * Returns list of inspector methods in a given class.
     * An inspector is a cheap-pure method with no arguments.
     *
     * @param className A class name
     */
    public static List<String> getInspectors(String className) {
        List<String> pureMethods = CheapPurityAnalyzer.getInstance().getPureMethods(className);
        List<String> inspectors = new ArrayList<>();
        for (String pm : pureMethods) {
            if ((Type.getArgumentTypes(pm.substring(pm.indexOf('('))).length == 0) &&
                    !(pm.substring(0, pm.indexOf("(")).equals("<clinit>")))
                inspectors.add(pm);
        }
        return inspectors;
    }
}
