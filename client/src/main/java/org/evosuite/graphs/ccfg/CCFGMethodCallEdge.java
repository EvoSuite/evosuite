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
package org.evosuite.graphs.ccfg;

import org.evosuite.graphs.cfg.BytecodeInstruction;

public class CCFGMethodCallEdge extends CCFGEdge {

    private static final long serialVersionUID = -1638791707105165885L;

    private final BytecodeInstruction callInstruction;

    private final boolean isCallingEdge;

    /**
     * <p>
     * Constructor for CCFGMethodCallEdge.
     * </p>
     *
     * @param callInstruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @param isCallingEdge   a boolean.
     */
    public CCFGMethodCallEdge(BytecodeInstruction callInstruction, boolean isCallingEdge) {
        this.callInstruction = callInstruction;
        this.isCallingEdge = isCallingEdge;
    }

    /**
     * Marks whether this is a calling edge or a returning edge
     *
     * @return a boolean.
     */
    public boolean isCallingEdge() {
        return isCallingEdge;
    }

    /**
     * <p>
     * Getter for the field <code>callInstruction</code>.
     * </p>
     *
     * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     */
    public BytecodeInstruction getCallInstruction() {
        return callInstruction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return (isCallingEdge ? "calling " : "returning from ")
                + callInstruction.getCalledMethod();
    }
}
