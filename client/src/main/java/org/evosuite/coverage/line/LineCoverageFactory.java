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
package org.evosuite.coverage.line;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.instrumentation.LinePool;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * MethodCoverageFactory class.
 * </p>
 *
 * @author Gordon Fraser, Andre Mis, Jose Miguel Rojas
 */
public class LineCoverageFactory extends
        AbstractFitnessFactory<LineCoverageTestFitness> {

    private static final Logger logger = LoggerFactory.getLogger(LineCoverageFactory.class);
    private final MethodNameMatcher matcher = new MethodNameMatcher();

    private boolean isEnumDefaultConstructor(String className, String methodName) {
        if (!methodName.equals("<init>(Ljava/lang/String;I)V")) {
            return false;
        }
        try {
            Class<?> targetClass = Class.forName(className, false, TestGenerationContext.getInstance().getClassLoaderForSUT());
            if (!targetClass.isEnum()) {
                logger.debug("Class is not enum");
                return false;
            }
            return Modifier.isPrivate(targetClass.getDeclaredConstructor(String.class, int.class).getModifiers());
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            logger.debug("Exception " + e);
            return false;
        }
    }

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
    public List<LineCoverageTestFitness> getCoverageGoals() {
        List<LineCoverageTestFitness> goals = new ArrayList<>();

        long start = System.currentTimeMillis();

        for (String className : LinePool.getKnownClasses()) {
            // Only lines in CUT
            if (!isCUT(className))
                continue;

            for (String methodName : LinePool.getKnownMethodsFor(className)) {
                if (isEnumDefaultConstructor(className, methodName)) {
                    continue;
                }
                if (!matcher.methodMatches(methodName)) {
                    logger.info("Method {} does not match criteria. ", methodName);
                    continue;
                }
                Set<Integer> lines = LinePool.getLines(className, methodName);
                for (Integer line : lines) {
                    logger.info("Adding goal for method " + className + "." + methodName + ", Line " + line + ".");
                    goals.add(new LineCoverageTestFitness(className, methodName, line));
                }
            }
        }
        goalComputationTime = System.currentTimeMillis() - start;
        return goals;
    }


    /**
     * Create a fitness function for branch coverage aimed at covering the root
     * branch of the given method in the given class. Covering a root branch
     * means entering the method.
     *
     * @param className a {@link java.lang.String} object.
     * @param method    a {@link java.lang.String} object.
     * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
     * object.
     */
    public static LineCoverageTestFitness createLineTestFitness(
            String className, String method, Integer line) {

        return new LineCoverageTestFitness(className,
                method.substring(method.lastIndexOf(".") + 1), line);
    }

    /**
     * Convenience method calling createMethodTestFitness(class,method) with
     * the respective class and method of the given BytecodeInstruction.
     *
     * @param instruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
     * object.
     */
    public static LineCoverageTestFitness createLineTestFitness(
            BytecodeInstruction instruction) {
        if (instruction == null)
            throw new IllegalArgumentException("null given");

        return createLineTestFitness(instruction.getClassName(),
                instruction.getMethodName(), instruction.getLineNumber());
    }
}
