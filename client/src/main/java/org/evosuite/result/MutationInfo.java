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

import org.evosuite.coverage.mutation.Mutation;

import java.io.Serializable;

public class MutationInfo implements Serializable {

    private static final long serialVersionUID = 4580001065523289191L;

    private final String className;

    private final String methodName;

    int lineNo;

    private final String replacement;

    public MutationInfo(Mutation m) {
        this.className = m.getClassName();
        this.methodName = m.getMethodName();
        this.lineNo = m.getLineNumber();
        this.replacement = m.getMutationName();
    }

    public MutationInfo(String className, String methodName, int lineNo,
                        String replacement) {
        this.className = className;
        this.methodName = methodName;
        this.lineNo = lineNo;
        this.replacement = replacement;
    }

    public String getClassName() {
        return className;
    }

    public int getLineNo() {
        return lineNo;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getReplacement() {
        return replacement;
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
        result = prime * result
                + ((replacement == null) ? 0 : replacement.hashCode());
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
        MutationInfo other = (MutationInfo) obj;
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
        if (replacement == null) {
            return other.replacement == null;
        } else return replacement.equals(other.replacement);
    }

    @Override
    public String toString() {
        return "MutationInfo [className=" + className + ", methodName="
                + methodName + ", lineNo=" + lineNo + ", replacement="
                + replacement + "]";
    }


}
