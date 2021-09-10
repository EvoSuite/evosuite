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

/**
 * This class represent a method call of a field class.
 *
 * @author Mattia Vivanti
 */
public class CCFGFieldClassCallNode extends CCFGCodeNode {

    private final String className;
    private final String methodName;
    private final String methodParameters;


//	

    /**
     * <p>Constructor for CCFGFieldClassCallNode.</p>
     *
     * @param code a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     */
    public CCFGFieldClassCallNode(BytecodeInstruction code, String className, String methodName, String methodParameters) {
        super(code);
        this.className = className;
        this.methodName = methodName;
        this.methodParameters = methodParameters;
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
        return methodName + methodParameters;
    }

    public String getOnlyMethodName() {
        return methodName;
    }

    public String getOnlyParameters() {
        return methodParameters;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((className == null) ? 0 : className.hashCode());
        result = prime * result
                + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime
                * result
                + ((methodParameters == null) ? 0 : methodParameters.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CCFGFieldClassCallNode other = (CCFGFieldClassCallNode) obj;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        if (methodParameters == null) {
            return other.methodParameters == null;
        } else return methodParameters.equals(other.methodParameters);
    }


}
