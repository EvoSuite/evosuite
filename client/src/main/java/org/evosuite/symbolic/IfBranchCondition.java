/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic;

import org.evosuite.symbolic.expr.Constraint;

import java.util.List;

/**
 * Represents a branch condition originated from the execution of an IF
 * instruction at the bytecode level.
 *
 * @author jgaleotti
 */
public final class IfBranchCondition extends BranchCondition {

    private final boolean isTrueBranch;

    /**
     * A branch condition is identified by the className, methodName and branchIndex
     * belonging to the class in the SUT, the target constraint and all the
     * suporting constraint for that particular branch (zero checks, etc)
     *
     * @param className
     * @param methodName
     * @param instructionIndex
     * @param constraint            TODO
     * @param supportingConstraints a {@link java.util.Set} object.
     * @param isTrueBranch
     */
    public IfBranchCondition(String className, String methodName, int instructionIndex, Constraint<?> constraint,
                             List<Constraint<?>> supportingConstraints, boolean isTrueBranch) {

        super(className, methodName, instructionIndex, constraint, supportingConstraints);
        this.isTrueBranch = isTrueBranch;
    }

    public boolean isTrueBranch() {
        return isTrueBranch;
    }

    public boolean isFalseBranch() {
        return !isTrueBranch;
    }
}
