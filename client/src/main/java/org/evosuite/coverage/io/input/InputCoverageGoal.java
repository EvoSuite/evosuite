/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.coverage.io.input;


import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.statements.EntityWithParametersStatement;
import org.objectweb.asm.Type;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

import static org.evosuite.coverage.io.IOCoverageConstants.*;
import static org.evosuite.coverage.io.IOCoverageConstants.REF_NONNULL;
import static org.evosuite.coverage.io.IOCoverageConstants.STRING_NONEMPTY;

/**
 * A single input coverage goal.
 * Evaluates the value depending on the type of the i-th input argument to a method.
 *
 * @author Gordon Fraser, Andre Mis, Jose Miguel Rojas
 */
public class InputCoverageGoal implements Serializable, Comparable<InputCoverageGoal> {

    private static final long serialVersionUID = -2917009638438833179L;

    private final String className;
    private final String methodName;
    private final int    argIndex;
    private final String type;
    private final String valueDescriptor;
    private final Number numericValue;

    /**
     * Can be used to create an arbitrary {@code InputCoverageGoal} trying to cover the
     * method such that it returns a given {@code value}
     * <p/>
     * <p/>
     * If the method returns a boolean, this goal will try to cover the method with either {@code true} or {@code false}
     * If the given branch is {@code null}, this goal will try to cover the root branch
     * of the method identified by the given name - meaning it will just try to
     * call the method at hand
     * <p/>
     * <p/>
     * Otherwise this goal will try to reach the given branch and if value is
     * true, make the branchInstruction jump and visa versa
     *
     * @param className       a {@link String} object.
     * @param methodName      a {@link String} object.
     * @param argIndex        an argument index.
     * @param type            a {@link Type} object.
     * @param valueDescriptor a value descriptor.
     */
    public InputCoverageGoal(String className, String methodName, int argIndex, Type type, String valueDescriptor) {
        this(className, methodName, argIndex, type, valueDescriptor, null);
    }

    public InputCoverageGoal(String className, String methodName, int argIndex, Type type, String valueDescriptor, Number numericValue) {
        if (className == null || methodName == null)
            throw new IllegalArgumentException("null given");

        this.className = className;
        this.methodName = methodName;
        this.argIndex = argIndex;
        this.type = type.toString();
        this.valueDescriptor = valueDescriptor;
        this.numericValue = numericValue;
    }



    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return the argument index
     */
    public int getArgIndex() {
        return argIndex;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return Type.getType(type);
    }

    /**
     * @return the value
     */
    public String getValueDescriptor() {
        return valueDescriptor;
    }

    public Number getNumericValue() { return numericValue; }

    // inherited from Object

    /**
     * {@inheritDoc}
     * <p/>
     * Readable representation
     */
    @Override
    public String toString() {
        return className + "." + methodName + "[" + argIndex + "]:" + valueDescriptor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + className.hashCode();
        result = prime * result + methodName.hashCode();
        result = prime * result + argIndex;
        result = prime * result + (type == null ? 0 : type.hashCode());
        result = prime * result + (valueDescriptor == null ? 0 : valueDescriptor.hashCode());
        return result;
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

        InputCoverageGoal other = (InputCoverageGoal) obj;

        if (this.argIndex != other.argIndex)
            return false;

        if (!this.methodName.equals(other.methodName) && this.className.equals(other.className))
            return false;

        if ((this.type == null && other.type != null) || (this.type != null && other.type == null))
            return false;

        if (this.type != null && !this.type.equals(other.type))
            return false;

        if ((this.valueDescriptor == null && other.valueDescriptor != null) || (this.valueDescriptor != null && other.valueDescriptor == null))
            return false;

        if (this.valueDescriptor != null && !this.valueDescriptor.equals(other.valueDescriptor))
            return false;

        return true;
    }

    @Override
    public int compareTo(InputCoverageGoal o) {

        int diff = className.compareTo(o.className);
        if (diff == 0) {
            int diff2 = methodName.compareTo(o.methodName);
            if (diff2 == 0) {
                if (argIndex == o.argIndex) {
                    int diff3 = type.compareTo(o.type);
                    if (diff3 == 0)
                        return this.valueDescriptor.compareTo(o.valueDescriptor);
                    else
                        return diff3;
                } else
                    return Integer.compare(argIndex, o.argIndex);
            } else
                return diff2;
        } else
            return diff;
    }

    public static Set<InputCoverageGoal> createCoveredGoalsFromParameters(String className, String methodName, String methodDesc, List<Object> argumentsValues) {
        Set<InputCoverageGoal> goals = new LinkedHashSet<>();

        Type[] argTypes = Type.getArgumentTypes(methodDesc);

        for (int i=0;i<argTypes.length;i++) {
            Type argType = argTypes[i];
            Object argValue = argumentsValues.get(i);
            String argValueDesc = "";
            Number numberValue = null;
            switch (argType.getSort()) {
                case Type.BOOLEAN:
                    argValueDesc = (((boolean) argValue)) ? BOOL_TRUE : BOOL_FALSE;
                    break;
                case Type.CHAR:
                    char c = (char) argValue;
                    if (Character.isAlphabetic(c))
                        argValueDesc = CHAR_ALPHA;
                    else if (Character.isDigit(c))
                        argValueDesc = CHAR_DIGIT;
                    else
                        argValueDesc = CHAR_OTHER;
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
                    numberValue = value;
                    argValueDesc = (value < 0) ? NUM_NEGATIVE : (value == 0) ? NUM_ZERO : NUM_POSITIVE;
                    break;
                case Type.ARRAY:
                    if (argValue == null)
                        argValueDesc = REF_NULL;
                    else
                        argValueDesc = (Array.getLength(argValue) == 0) ? ARRAY_EMPTY : ARRAY_NONEMPTY;
                    break;
                case Type.OBJECT:
                    if (argValue == null)
                        argValueDesc = REF_NULL;
                    else {
                        if (argType.getClassName().equals("java.lang.String")) {
                            argValueDesc = ((String) argValue).isEmpty() ? STRING_EMPTY : STRING_NONEMPTY;
                        }
                        else if(argValue instanceof List) {
                            argValueDesc = ((List) argValue).isEmpty() ? LIST_EMPTY : LIST_NONEMPTY;
                        }
                        else if(argValue instanceof Set) {
                            argValueDesc = ((Set) argValue).isEmpty() ? SET_EMPTY : SET_NONEMPTY;
                        }
                        else if(argValue instanceof Map) {
                            argValueDesc = ((Map) argValue).isEmpty() ? MAP_EMPTY : MAP_NONEMPTY;
                        }
                        else
                            argValueDesc = REF_NONNULL;
                    }
                    break;
                default:
                    break;
            }
            if (!argValueDesc.isEmpty())
                goals.add(new InputCoverageGoal(className, methodName+methodDesc, i, argType, argValueDesc, numberValue));
        }

        return goals;
    }
}
