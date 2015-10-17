/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.output;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.assertion.CheapPurityAnalyzer;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
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
    public static final String REF_NULL = "null";
    public static final String REF_NONNULL = "nonnull";
    public static final String EMPTY_ARRAY = "empty";
    public static final String NONEMPTY_ARRAY = "nonempty";

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
                        goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), REF_NULL)));
                        goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), EMPTY_ARRAY)));
                        goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), NONEMPTY_ARRAY)));
                        break;
                    case Type.OBJECT:
                        goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), REF_NULL)));
                        //goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), REF_NONNULL)));
                        boolean observerGoalsAdded = false;
                        List<String> inspectors = getInspectors(returnType.getClassName());
                        for (String insp : inspectors) {
                            Type t = Type.getReturnType(insp);
                            if (t.getSort() == Type.BOOLEAN) {
                                goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), REF_NONNULL + ":" + returnType.getClassName() + ":" + insp + ":" + BOOL_TRUE)));
                                goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), REF_NONNULL + ":" + returnType.getClassName() + ":" + insp + ":" + BOOL_FALSE)));
                                observerGoalsAdded = true;
                            } else if (Arrays.asList(new Integer[]{Type.BYTE, Type.SHORT, Type.INT, Type.FLOAT, Type.LONG, Type.DOUBLE}).contains(t.getSort())) {
                                goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), REF_NONNULL + ":" + returnType.getClassName() + ":" + insp + ":" + NUM_NEGATIVE)));
                                goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), REF_NONNULL + ":" + returnType.getClassName() + ":" + insp + ":" + NUM_ZERO)));
                                goals.add(new OutputCoverageTestFitness(new OutputCoverageGoal(className, methodName, returnType.toString(), REF_NONNULL + ":" + returnType.getClassName() + ":" + insp + ":" + NUM_POSITIVE)));
                                observerGoalsAdded = true;
                            }
                        }
                        if (!observerGoalsAdded)
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
                    ! (pm.substring(0, pm.indexOf("(")).equals("<clinit>")))
                inspectors.add(pm);
        }
        return inspectors;
    }
}
