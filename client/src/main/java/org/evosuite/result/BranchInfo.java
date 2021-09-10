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
package org.evosuite.result;

import org.evosuite.coverage.branch.Branch;

import java.io.Serializable;

public class BranchInfo implements Serializable {

    private static final long serialVersionUID = -2145547942894978737L;

    private final String className;

    private final String methodName;

    private final int lineNo;

    private final boolean truthValue;

    public BranchInfo(Branch branch, boolean truthValue) {
        this.className = branch.getClassName();
        this.methodName = branch.getMethodName();
        this.lineNo = branch.getInstruction().getLineNumber();
        this.truthValue = truthValue;
    }

    public BranchInfo(String className, String methodName, int lineNo,
                      boolean truthValue) {
        this.className = className;
        this.methodName = methodName;
        this.lineNo = lineNo;
        this.truthValue = truthValue;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getLineNo() {
        return lineNo;
    }

    public boolean getTruthValue() {
        return truthValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((className == null) ? 0 : className.hashCode());
        result = prime * result + lineNo;
        result = prime * result
                + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result + (truthValue ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BranchInfo other = (BranchInfo) obj;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (lineNo != other.lineNo)
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        return truthValue == other.truthValue;
    }

    @Override
    public String toString() {
        return "BranchInfo [className=" + className + ", methodName="
                + methodName + ", lineNo=" + lineNo + ", truthValue="
                + truthValue + "]";
    }


}
