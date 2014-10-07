/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.output;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jose Miguel Rojas
 */
public class OutputCoverageFactory extends AbstractFitnessFactory<OutputCoverageTestFitness> {

    private static final Logger logger = LoggerFactory.getLogger(OutputCoverageFactory.class);

    public static final String CHAR_ALPHA = "alpha";
    public static final String CHAR_DIGIT = "digit";
    public static final String CHAR_OTHER = "other";
    public static final String BOOL_TRUE = "true";
    public static final String BOOL_FALSE = "false";
    public static final String NUM_POSITIVE = "positive";
    public static final String NUM_ZERO = "zero";
    public static final String NUM_NEGATIVE = "negative";
    public static final String DEFAULT = "default";
    public static final String REF_NULL = "null";
    public static final String REF_NONNULL = "nonnull";
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
        List<OutputCoverageTestFitness> goals = new ArrayList<OutputCoverageTestFitness>();

        long start = System.currentTimeMillis();
        String targetClass = Properties.TARGET_CLASS;

        final MethodNameMatcher matcher = new MethodNameMatcher();
        for (String className : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownClasses()) {
            if (!(targetClass.equals("") || className.endsWith(targetClass)))
                continue;
            for (String methodName : BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).knownMethods(className)) {
                if (!matcher.methodMatches(methodName))
                    continue;
                logger.info("Adding goals for method " + className + "." + methodName);
                Type returnType = Type.getReturnType(methodName);
                switch (returnType.getSort()) {
                    case Type.BOOLEAN:
                        goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), BOOL_TRUE)));
                        goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), BOOL_FALSE)));
                        break;
                    case Type.CHAR:
                        goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), CHAR_ALPHA)));
                        goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), CHAR_DIGIT)));
                        goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), CHAR_OTHER)));
                        break;
                    case Type.BYTE:
                    case Type.SHORT:
                    case Type.INT:
                    case Type.FLOAT:
                    case Type.LONG:
                    case Type.DOUBLE:
                        goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), NUM_NEGATIVE)));
                        goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), NUM_ZERO)));
                        goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), NUM_POSITIVE)));
                        break;
                    case Type.ARRAY:
                    case Type.OBJECT:
                        goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), REF_NULL)));
                        goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), REF_NONNULL)));
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

    public static String goalString(String className, String methodName, String suffix) {
        return new String(className + "." + methodName + ":" + suffix);
    }
}
