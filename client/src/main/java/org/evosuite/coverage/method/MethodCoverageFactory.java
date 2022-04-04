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
package org.evosuite.coverage.method;

import org.evosuite.Properties;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.setup.TestUsageChecker;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * MethodCoverageFactory class.
 * </p>
 *
 * @author Gordon Fraser, Andre Mis, Jose Miguel Rojas
 */
public class MethodCoverageFactory extends
        AbstractFitnessFactory<MethodCoverageTestFitness> {

    private static final Logger logger = LoggerFactory.getLogger(MethodCoverageFactory.class);
    private final MethodNameMatcher matcher = new MethodNameMatcher();

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
    public List<MethodCoverageTestFitness> getCoverageGoals() {
        List<MethodCoverageTestFitness> goals = new ArrayList<>();

        long start = System.currentTimeMillis();

        String className = Properties.TARGET_CLASS;
        Class<?> clazz = Properties.getTargetClassAndDontInitialise();
        if (clazz != null) {
            goals.addAll(getCoverageGoals(clazz, className));
            Class<?>[] innerClasses = clazz.getDeclaredClasses();
            for (Class<?> innerClass : innerClasses) {
                String innerClassName = innerClass.getCanonicalName();
                goals.addAll(getCoverageGoals(innerClass, innerClassName));
            }
        }
        goalComputationTime = System.currentTimeMillis() - start;
        return goals;
    }

    private List<MethodCoverageTestFitness> getCoverageGoals(Class<?> clazz, String className) {
        List<MethodCoverageTestFitness> goals = new ArrayList<>();
        Constructor<?>[] allConstructors = clazz.getDeclaredConstructors();
        for (Constructor<?> c : allConstructors) {
            if (TestUsageChecker.canUse(c)) {
                String methodName = "<init>" + Type.getConstructorDescriptor(c);
                logger.info("Adding goal for constructor " + className + "." + methodName);
                goals.add(new MethodCoverageTestFitness(className, methodName));
            }
        }
        Method[] allMethods = clazz.getDeclaredMethods();
        for (Method m : allMethods) {
            if (TestUsageChecker.canUse(m)) {
                if (clazz.isEnum()) {
                    if (m.getName().equals("valueOf") || m.getName().equals("values")
                            || m.getName().equals("ordinal")) {
                        logger.debug("Excluding valueOf for Enum " + m);
                        continue;
                    }
                }
                if (clazz.isInterface() && Modifier.isAbstract(m.getModifiers())) {
                    // Don't count interface declarations as targets
                    continue;
                }
                String methodName = m.getName() + Type.getMethodDescriptor(m);
                if (!matcher.methodMatches(methodName)) {
                    logger.info("Method {} does not match criteria. ", methodName);
                    continue;
                }
                logger.info("Adding goal for method " + className + "." + methodName);
                goals.add(new MethodCoverageTestFitness(className, methodName));
            }
        }
        return goals;
    }

    /**
     * Create a fitness function for branch coverage aimed at covering the root
     * branch of the given method in the given class. Covering a root branch
     * means entering the method.
     *
     * @param className a {@link String} object.
     * @param method    a {@link String} object.
     * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
     * object.
     */
    public static MethodCoverageTestFitness createMethodTestFitness(
            String className, String method) {

        return new MethodCoverageTestFitness(className,
                method.substring(method.lastIndexOf(".") + 1));
    }

    /**
     * Convenience method calling createMethodTestFitness(class,method) with
     * the respective class and method of the given BytecodeInstruction.
     *
     * @param instruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
     * object.
     */
    public static MethodCoverageTestFitness createMethodTestFitness(
            BytecodeInstruction instruction) {
        if (instruction == null)
            throw new IllegalArgumentException("null given");

        return createMethodTestFitness(instruction.getClassName(),
                instruction.getMethodName());
    }
}
