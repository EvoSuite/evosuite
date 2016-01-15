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


import java.io.Serializable;

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
     * @param type            a {@link String} object.
     * @param valueDescriptor a value descriptor.
     */
    public InputCoverageGoal(String className, String methodName, int argIndex, String type, String valueDescriptor) {
        if (className == null || methodName == null)
            throw new IllegalArgumentException("null given");

        this.className = className;
        this.methodName = methodName;
        this.argIndex = argIndex;
        this.type = type;
        this.valueDescriptor = valueDescriptor;
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
    public String getType() {
        return type;
    }

    /**
     * @return the value
     */
    public String getValueDescriptor() {
        return valueDescriptor;
    }

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

        if (type != null && !this.type.equals(other.type))
            return false;

        if ((this.valueDescriptor == null && other.valueDescriptor != null) || (this.valueDescriptor != null && other.valueDescriptor == null))
            return false;

        if (valueDescriptor != null && !this.valueDescriptor.equals(other.valueDescriptor))
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
}
