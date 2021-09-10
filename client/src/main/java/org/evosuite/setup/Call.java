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
package org.evosuite.setup;

import java.io.Serializable;

public class Call implements Serializable {

    /**
     * Call of call context
     * TODO this class is approximated and does not consider the method signature
     * mattia
     */
    private static final long serialVersionUID = -8148115191773499144L;
    private final String className;
    private final String methodName;
    private final int hcode;
    private final int approxHcode;

    public Call(String classname, String methodName) {
        this.className = classname;
        this.methodName = methodName;
        approxHcode = computeApproximatedHashCode();
        hcode = computeHashCode();
    }

    public Call(Call call) {
        this.className = call.className;
        this.methodName = call.methodName;
        approxHcode = computeApproximatedHashCode();
        hcode = computeHashCode();
    }

    private int computeApproximatedHashCode() {
        String mname = methodName;
        if (mname.contains("("))
            mname = mname.substring(0, mname.indexOf("("));
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((mname == null) ? 0 : mname.hashCode());
        return result;
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

    @Override
    public int hashCode() {
        return approxHcode;
    }

    private int computeHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
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
        Call other = (Call) obj;
        return hcode == other.hcode;
    }

    // TODO: Could consider line number?
    public boolean matches(Call other) {
        return approxHcode == other.approxHcode;
    }

    @Override
    public String toString() {
        return className + ":" + methodName;
    }

}