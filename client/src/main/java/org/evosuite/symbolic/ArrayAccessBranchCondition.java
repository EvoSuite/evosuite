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

import org.evosuite.symbolic.expr.Constraint;

import java.util.ArrayList;

/**
 * Represents a branch condition originated from the execution of a XSTORE, XLOAD, XASTORE, XALOAD, NEWARRAY, ANEWARRAY
 * or MULINEWARRAY instruction at the bytecode level.
 *
 * @author Ignacio Lebrero
 */
public final class ArrayAccessBranchCondition extends BranchCondition {

    private final boolean isErrorBranch;

    /**
     * An array access condition is identified by the className, methodName and instructionIndex and the target
     * constraint.
     *
     * @param className
     * @param methodName
     * @param instructionIndex
     * @param constraint
     * @param isErrorBranch
     */
    public ArrayAccessBranchCondition(String className, String methodName, int instructionIndex, Constraint<?> constraint,
                                      boolean isErrorBranch) {
        super(className, methodName, instructionIndex, constraint, new ArrayList<>());
        this.isErrorBranch = isErrorBranch;
    }

    public boolean isErrorBranch() {
        return isErrorBranch;
    }
}
