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


import org.objectweb.asm.Type;

import java.io.Serializable;

/**
 * A single output coverage goal.
 * Evaluates the value depending on the return type of the method.
 *
 * @author Gordon Fraser, Andre Mis, Jose Miguel Rojas
 */
public class OutputCoverageGoal implements Serializable, Comparable<OutputCoverageGoal> {

    private static final long serialVersionUID = 3539419075883329059L;


    private final String className;
    private final String methodName;
    private final String type;
    private final String valueDescriptor;

    /**
     * Can be used to create an arbitrary {@code OutputCoverageGoal} trying to cover the
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
     * @param className       a {@link java.lang.String} object.
     * @param methodName      a {@link java.lang.String} object.
     * @param type            a {@link java.lang.String} object.
     * @param valueDescriptor a value descriptor.
     */
    public OutputCoverageGoal(String className, String methodName, Type type, String valueDescriptor) {
        if (className == null || methodName == null)
            throw new IllegalArgumentException("null given");

        this.className = className;
        this.methodName = methodName;
        this.type = type.toString();
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

    // inherited from Object

    /**
     * {@inheritDoc}
     * <p/>
     * Readable representation
     */
    @Override
    public String toString() {
        return className + "." + methodName + ":" + valueDescriptor;
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

        OutputCoverageGoal other = (OutputCoverageGoal) obj;

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
    public int compareTo(OutputCoverageGoal o) {

        int diff = className.compareTo(o.className);
        if (diff == 0) {
            int diff2 = methodName.compareTo(o.methodName);
            if (diff2 == 0) {
                int diff3 = type.toString().compareTo(o.type.toString());
                if (diff3 == 0)
                    return this.valueDescriptor.compareTo(o.valueDescriptor);
                else
                    return diff3;
            } else
                return diff2;
        } else
            return diff;
    }

//	private void writeObject(ObjectOutputStream oos) throws IOException {
//		oos.defaultWriteObject();
//		// Write/save additional fields
//		if (branch != null)
//			oos.writeInt(branch.getActualBranchId());
//		else
//			oos.writeInt(-1);
//	}
//
//	// assumes "static java.util.Date aDate;" declared
//	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
//	        IOException {
//		ois.defaultReadObject();
//
//		int branchId = ois.readInt();
//		if (branchId >= 0)
//			this.branch = BranchPool.getBranch(branchId);
//		else
//			this.branch = null;
//	}

}
