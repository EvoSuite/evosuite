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
package org.evosuite.symbolic;

import org.evosuite.classpath.ResourceList;
import org.evosuite.symbolic.expr.Constraint;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * BranchCondition class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class BranchCondition {
    /**
     * Class where the branch instruction is
     */
    private final String className;

    /**
     * Method where the branch instruction is
     */
    private final String methodName;

    /**
     * Position of the instruction in the method bytecode
     */
    private final int instructionIndex;

    private final Constraint<?> constraint;

    private final List<Constraint<?>> supportingConstraints;

    /**
     * A branch condition is identified by the className, methodName and branchIndex
     * belonging to the class in the SUT, the target constraint and all the
     * supporting constraint for that particular branch (zero checks, etc)
     *
     * @param className             a {@link java.lang.String} object
     * @param methodName            a {@link java.lang.String} object
     * @param instructionIndex      an {@link int} value
     * @param constraint            a {@link Constraint} object
     * @param supportingConstraints a {@link java.util.Set} object.
     */
    public BranchCondition(String className, String methodName, int instructionIndex, Constraint<?> constraint,
                           List<Constraint<?>> supportingConstraints) {

        this.className = ResourceList.getClassNameFromResourcePath(className);
        this.methodName = methodName;
        this.instructionIndex = instructionIndex;

        this.constraint = constraint;
        this.supportingConstraints = supportingConstraints;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String ret = "";
        for (Constraint<?> c : this.supportingConstraints) {
            ret += " " + c + "\n";
        }

        ret += this.constraint;
        return ret;
    }

    public String getClassName() {
        return className;
    }

    public int getInstructionIndex() {
        return instructionIndex;
    }

    public String getFullName() {
        return className + "." + methodName;
    }

    /**
     * Returns the constraint for actual branch. This constraint has to be negated
     * to take another path.
     *
     * @return
     */
    public Constraint<?> getConstraint() {
        return constraint;
    }

    /**
     * Returns a list of implicit constraints (nullity checks, zero division, index
     * within bounds, negative size array length, etc.) collected before the current
     * branch condition and after the last symbolic branch condition
     *
     * @return
     */
    public List<Constraint<?>> getSupportingConstraints() {
        return supportingConstraints;
    }

    public String getMethodName() {
        return methodName;
    }

    /**
     * For simplicity we create this construction of the object to handle path conditions easily.
     *
     * @return
     */
    public BranchCondition getNegatedVersion() {
        return new BranchCondition(className, methodName, instructionIndex, constraint.negate(), supportingConstraints);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BranchCondition that = (BranchCondition) o;
        return instructionIndex == that.instructionIndex &&
                className.equals(that.className) &&
                methodName.equals(that.methodName) &&
                constraint.equals(that.constraint) &&
                supportingConstraints.equals(that.supportingConstraints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                className,
                methodName,
                instructionIndex,
                constraint,
                supportingConstraints
        );
    }
}
