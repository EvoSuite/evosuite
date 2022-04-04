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

public class CCFGCodeNode extends CCFGNode {

    protected BytecodeInstruction codeInstruction;

    /**
     * <p>Constructor for CCFGCodeNode.</p>
     *
     * @param code a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     */
    public CCFGCodeNode(BytecodeInstruction code) {
        this.codeInstruction = code;
    }

    /**
     * <p>getMethod</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMethod() {
        return codeInstruction.getMethodName();
    }

    /**
     * <p>Getter for the field <code>codeInstruction</code>.</p>
     *
     * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     */
    public BytecodeInstruction getCodeInstruction() {
        return codeInstruction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((codeInstruction == null) ? 0 : codeInstruction.hashCode());
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
        CCFGCodeNode other = (CCFGCodeNode) obj;
        if (codeInstruction == null) {
            return other.codeInstruction == null;
        } else return codeInstruction.equals(other.codeInstruction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (codeInstruction.isMethodCall())
            return codeInstruction.toString() + " in class " + codeInstruction.getCalledMethodsClass();
        else
            return codeInstruction.toString();
    }
}
