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
package org.evosuite.coverage.io.output;

import static org.evosuite.coverage.io.IOCoverageConstants.*;

import org.evosuite.Properties;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorManager;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.MethodStatement;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Jose Miguel Rojas
 */
public class OutputCoverageTestFitness extends TestFitnessFunction {

    private static final long serialVersionUID = 1383064944691491355L;

    protected static final Logger logger = LoggerFactory.getLogger(OutputCoverageTestFitness.class);

    /**
     * Target goal
     */
    private final OutputCoverageGoal goal;

    /**
     * Constructor - fitness is specific to a method
     *
     * @param goal the coverage goal
     * @throws IllegalArgumentException
     */
    public OutputCoverageTestFitness(OutputCoverageGoal goal) throws IllegalArgumentException {
        if (goal == null) {
            throw new IllegalArgumentException("goal cannot be null");
        }
        this.goal = goal;
    }

    public static HashSet<TestFitnessFunction> listCoveredGoals(Map<MethodStatement, Object> returnValues) {
        HashSet<TestFitnessFunction> results = new HashSet<>();

        for (Entry<MethodStatement, Object> entry : returnValues.entrySet()) {
            String className  = entry.getKey().getDeclaringClassName();
            String methodDesc = entry.getKey().getDescriptor();
            String methodName = entry.getKey().getMethodName() + methodDesc;

            // TODO: Wouldn't this exclude inner classes?
            if (! className.equals(Properties.TARGET_CLASS))
                continue;
            if (methodName.equals("hashCode()I"))
                continue;

            Type returnType = Type.getReturnType(methodDesc);
            Object returnValue = entry.getValue();
            switch (returnType.getSort()) {
                case Type.BOOLEAN:
                    String desc = ((boolean) returnValue) ? BOOL_TRUE : BOOL_FALSE;
                    results.add(OutputCoverageFactory.createGoal(className, methodName, returnType, desc));
                    break;
                case Type.CHAR:
                    char c = (char) returnValue;
                    if (Character.isAlphabetic(c))
                        results.add(OutputCoverageFactory.createGoal(className, methodName, returnType, CHAR_ALPHA));
                    else if (Character.isDigit(c))
                        results.add(OutputCoverageFactory.createGoal(className, methodName, returnType, CHAR_DIGIT));
                    else
                        results.add(OutputCoverageFactory.createGoal(className, methodName, returnType, CHAR_OTHER));
                    break;
                case Type.BYTE:
                case Type.SHORT:
                case Type.INT:
                case Type.FLOAT:
                case Type.LONG:
                case Type.DOUBLE:
                    assert (returnValue instanceof Number);
                    if(isJavaNumber(returnValue)) {
                        double value = ((Number) returnValue).doubleValue();
                        String numDesc = (value < 0) ? NUM_NEGATIVE : (value == 0) ? NUM_ZERO : NUM_POSITIVE;
                        results.add(OutputCoverageFactory.createGoal(className, methodName, returnType, numDesc));
                    }
                    break;
                case Type.ARRAY:
                    if (returnValue == null)
                        results.add(OutputCoverageFactory.createGoal(className, methodName, returnType, REF_NULL));
                    else {
                        String arrDesc = (Array.getLength(returnValue) == 0) ? ARRAY_EMPTY : ARRAY_NONEMPTY;
                        results.add(OutputCoverageFactory.createGoal(className, methodName, returnType, arrDesc));
                    }
                    break;
                case Type.OBJECT:
                    if (returnValue == null)
                        results.add(OutputCoverageFactory.createGoal(className, methodName, returnType, REF_NULL));
                    else {
                        results.add(OutputCoverageFactory.createGoal(className, methodName, returnType, REF_NONNULL));
                        if (returnType.getClassName().equals("java.lang.String")) {
                            String valDesc = ((String)returnValue).isEmpty() ? STRING_EMPTY : STRING_NONEMPTY;
                            results.add(OutputCoverageFactory.createGoal(className, methodName, returnType, valDesc));
                            break;
                        }
                        /*
                            NOTE: we cannot have this code. Calling SUT methods should only be done EXCLUSIVELY as part
                            of test execution, as they involve security manager checks, loop counter handling, etc.
                            Doing it as side effects of fitness evaluation could have many side effects

                            Note2: Re-enabled, as inspectors now properly check the security manager
                        */
                        for(Inspector inspector : InspectorManager.getInstance().getInspectors(returnValue.getClass())) {
                            String insp = inspector.getMethodCall() + Type.getMethodDescriptor(inspector.getMethod());
                            try {
                                Object val = inspector.getValue(returnValue);
                                if (val instanceof Boolean) {
                                    String valDesc = ((boolean)val) ? BOOL_TRUE : BOOL_FALSE;
                                    results.add(OutputCoverageFactory.createGoal(className, methodName, returnType, REF_NONNULL + ":" + returnType.getClassName() + ":" + insp + ":" + valDesc));
                                } else if (isJavaNumber(val)) {
                                    double dv = ((Number) val).doubleValue();
                                    String valDesc = (dv < 0) ? NUM_NEGATIVE : (dv == 0) ? NUM_ZERO : NUM_POSITIVE;
                                    results.add(OutputCoverageFactory.createGoal(className, methodName, returnType, REF_NONNULL + ":" + returnType.getClassName() + ":" + insp + ":" + valDesc));
                                }
                            } catch (InvocationTargetException | IllegalAccessException e) {
                                logger.warn(e.getMessage(), e);
                            }
                        }

                    }
                    break;
                default:
                    // IGNORE
                    // TODO: what to do with the sort for METHOD?
                    break;
            }
        }
        return results;
    }

    /**
     * The SUT could have classes extending Number. Calling doubleValue()
     * on those would lead to many problems, like for example security and
     * loop counter checks.
     *
     * @param val
     * @return
     */
    private static boolean isJavaNumber(Object val){
        return val instanceof Number && val.getClass().getName().startsWith("java.");
    }

    /**
     * <p>
     * getClassName
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassName() {
        return goal.getClassName();
    }

    /**
     * <p>
     * getMethod
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMethod() {
        return goal.getMethodName();
    }

    /**
     * <p>
     * getValue
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public Type getType() {
        return goal.getType();
    }

    /**
     * <p>
     * getValue
     * </p>
     *
     * @return a {@link java.lang.String} object.
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
     * @param result     a {@link org.evosuite.testcase.execution.ExecutionResult} object.
     * @return a double.
     */
    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {
        double fitness = 1.0;

        HashSet<TestFitnessFunction> goals = listCoveredGoals(result.getReturnValues());
        for (TestFitnessFunction goal : goals) {
            if (this.toString().equals(goal.toString())) {
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
        OutputCoverageTestFitness other = (OutputCoverageTestFitness) obj;
        return this.goal.equals(other.goal);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
     */
    @Override
    public int compareTo(TestFitnessFunction other) {
        if (other instanceof OutputCoverageTestFitness) {
            OutputCoverageTestFitness otherOutputFitness = (OutputCoverageTestFitness) other;
            return goal.compareTo(otherOutputFitness.goal);
        }
        return compareClassName(other);
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

    /*
     * TODO: Move somewhere else into a utility class
     */
    private static final Class<?> getClassForName(String type)
    {
        try
        {
            if( type.equals("boolean"))
            {
                return Boolean.TYPE;
            }
            else if(type.equals("byte"))
            {
                return Byte.TYPE;
            }
            else if( type.equals("char"))
            {
                return Character.TYPE;
            }
            else if( type.equals("double"))
            {
                return Double.TYPE;
            }
            else if(type.equals("float"))
            {
                return Float.TYPE;
            }
            else if(type.equals("int"))
            {
                return Integer.TYPE;
            }
            else if( type.equals("long"))
            {
                return Long.TYPE;
            }
            else if(type.equals("short"))
            {
                return Short.TYPE;
            }
            else if(type.equals("String") ||type.equals("Boolean") || type.equals("Short") ||type.equals("Long") ||
                    type.equals("Integer") || type.equals("Float") || type.equals("Double") ||type.equals("Byte") ||
                    type.equals("Character") )
            {
                return Class.forName("java.lang." + type);
            }

//			if(type.endsWith(";") && ! type.startsWith("["))
//			{
//				type = type.replaceFirst("L", "");
//				type = type.replace(";", "");
//			}

            if(type.endsWith("[]"))
            {
                type = type.replace("[]", "");
                return Class.forName("[L" + type + ";");
            }
            else
            {
                return Class.forName(type);
            }
        }
        catch (final ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
}