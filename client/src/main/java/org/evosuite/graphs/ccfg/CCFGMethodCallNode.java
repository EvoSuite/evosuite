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

public class CCFGMethodCallNode extends CCFGNode {

    private final BytecodeInstruction callInstruction;
    private final CCFGMethodReturnNode returnNode;

    /**
     * <p>Constructor for CCFGMethodCallNode.</p>
     *
     * @param callInstruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @param returnNode      a {@link org.evosuite.graphs.ccfg.CCFGMethodReturnNode} object.
     */
    public CCFGMethodCallNode(BytecodeInstruction callInstruction, CCFGMethodReturnNode returnNode) {
        this.callInstruction = callInstruction;
        this.returnNode = returnNode;
    }

    /**
     * <p>getMethod</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMethod() {
        return callInstruction.getMethodName();
    }

    /**
     * <p>getCalledMethod</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCalledMethod() {
        return callInstruction.getCalledMethod();
    }

    /**
     * <p>Getter for the field <code>callInstruction</code>.</p>
     *
     * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     */
    public BytecodeInstruction getCallInstruction() {
        return callInstruction;
    }

    /**
     * <p>Getter for the field <code>returnNode</code>.</p>
     *
     * @return a {@link org.evosuite.graphs.ccfg.CCFGMethodReturnNode} object.
     */
    public CCFGMethodReturnNode getReturnNode() {
        return returnNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((callInstruction == null) ? 0 : callInstruction.hashCode());
        result = prime * result
                + ((returnNode == null) ? 0 : returnNode.hashCode());
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
        CCFGMethodCallNode other = (CCFGMethodCallNode) obj;
        if (callInstruction == null) {
            if (other.callInstruction != null)
                return false;
        } else if (!callInstruction.equals(other.callInstruction))
            return false;
        if (returnNode == null) {
            return other.returnNode == null;
        } else return returnNode.equals(other.returnNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CALL from " + callInstruction.toString();
    }
}
