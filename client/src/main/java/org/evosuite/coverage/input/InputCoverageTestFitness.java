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
package org.evosuite.coverage.input;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.MethodStatement;
import org.objectweb.asm.Type;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Jose Miguel Rojas
 */
public class InputCoverageTestFitness extends TestFitnessFunction {

    private static final long serialVersionUID = 6630097528288524492L;

    /**
     * Target goal
     */
    private final InputCoverageGoal goal;

    /**
     * Constructor - fitness is specific to a method
     *
     * @param goal the coverage goal
     * @throws IllegalArgumentException
     */
    public InputCoverageTestFitness(InputCoverageGoal goal) throws IllegalArgumentException {
        if (goal == null) {
            throw new IllegalArgumentException("goal cannot be null");
        }
        this.goal = goal;
    }

    public static HashSet<String> listCoveredGoals(Map<MethodStatement, List<Object>> argumentsValues) {
        HashSet<String> results = new HashSet<String>();

        for (Entry<MethodStatement, List<Object>> entry : argumentsValues.entrySet()) {
            String className = entry.getKey().getMethod().getMethod().getDeclaringClass().getName();
            String methodDesc = Type.getMethodDescriptor(entry.getKey().getMethod().getMethod());
            String methodName = entry.getKey().getMethod().getName() + methodDesc;
            Type[] argTypes = Type.getArgumentTypes(methodDesc);

            for (int i=0;i<argTypes.length;i++) {
                Type argType = argTypes[i];
                Object argValue = entry.getValue().get(i);
                String goalSuffix = "";
                switch (argType.getSort()) {
                    case Type.BOOLEAN:
                        if (((boolean) argValue))
                            goalSuffix = InputCoverageFactory.BOOL_TRUE;
                        else
                            goalSuffix = InputCoverageFactory.BOOL_FALSE;
                        break;
                    case Type.CHAR:
                        char c = (char) argValue;
                        if (Character.isAlphabetic(c))
                            goalSuffix = InputCoverageFactory.CHAR_ALPHA;
                        else if (Character.isDigit(c))
                            goalSuffix = InputCoverageFactory.CHAR_DIGIT;
                        else
                            goalSuffix = InputCoverageFactory.CHAR_OTHER;
                        break;
                    case Type.BYTE:
                    case Type.SHORT:
                    case Type.INT:
                    case Type.FLOAT:
                    case Type.LONG:
                    case Type.DOUBLE:
                        // assert (argValue instanceof Number); // not always true: char can be assigned to integers
                        double value;

                        if (argValue instanceof Character) {
                            value = ((Number) ((int) (char) argValue)).doubleValue();
                        } else {
                            value = ((Number) argValue).doubleValue();
                        }

                        if (value < 0)
                            goalSuffix = InputCoverageFactory.NUM_NEGATIVE;
                        else if (value == 0)
                            goalSuffix = InputCoverageFactory.NUM_ZERO;
                        else
                            goalSuffix = InputCoverageFactory.NUM_POSITIVE;
                        break;
                    case Type.ARRAY:
                    case Type.OBJECT:
                        if (argValue == null)
                            goalSuffix = InputCoverageFactory.REF_NULL;
                        else
                            goalSuffix = InputCoverageFactory.REF_NONNULL;
                        break;
                    default:
                        break;
                }
                if (!goalSuffix.isEmpty())
                    results.add(InputCoverageFactory.goalString(className, methodName, i, goalSuffix));
            }
        }
        return results;
    }

    /**
     * <p>
     * getClassName
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getClassName() {
        return goal.getClassName();
    }

    /**
     * <p>
     * getMethod
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getMethod() {
        return goal.getMethodName();
    }

    /**
     * <p>
     * getValue
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getType() {
        return goal.getType();
    }

    /**
     * <p>
     * getValue
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getValueDescriptor() {
        return goal.getValueDescriptor();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Calculate fitness
     *
     * @param individual a {@link org.evosuite.testcase.ExecutableChromosome} object.
     * @param result     a {@link ExecutionResult} object.
     * @return a double.
     */
    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {
        double fitness = 1.0;

        HashSet<String> strGoals = listCoveredGoals(result.getArgumentsValues());
        for (String strGoal : strGoals) {
            if (strGoal.equals(goal.toString())) {
                fitness = 0.0;
                break;
            }
        }
        updateIndividual(this, individual, fitness);
        return fitness;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return goal.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int iConst = 13;
        return 51 * iConst + goal.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InputCoverageTestFitness other = (InputCoverageTestFitness) obj;
        return this.goal.equals(other.goal);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
     */
    @Override
    public int compareTo(TestFitnessFunction other) {
        if (other instanceof InputCoverageTestFitness) {
            InputCoverageTestFitness otherInputFitness = (InputCoverageTestFitness) other;
            return goal.compareTo(otherInputFitness.goal);
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
     */
    @Override
    public String getTargetClass() {
        return getClassName();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
     */
    @Override
    public String getTargetMethod() {
        return getMethod();
    }

}