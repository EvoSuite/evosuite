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

/**
 * The superclass of an entry of the static usage graph.
 * The static usage graph contains information regarding how static fields are used
 * by static methods.
 *
 * @author galeotti
 */
abstract class GetStaticGraphEntry {

    private final String sourceClass;
    private final String sourceMethod;
    private final String targetClass;

    public GetStaticGraphEntry(String sourceClass, String sourceMethod,
                               String targetClass) {
        super();
        this.sourceClass = sourceClass;
        this.sourceMethod = sourceMethod;
        this.targetClass = targetClass;
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public String getSourceMethod() {
        return sourceMethod;
    }

    public String getTargetClass() {
        return targetClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((sourceClass == null) ? 0 : sourceClass.hashCode());
        result = prime * result
                + ((sourceMethod == null) ? 0 : sourceMethod.hashCode());
        result = prime * result
                + ((targetClass == null) ? 0 : targetClass.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof GetStaticGraphEntry))
            return false;
        GetStaticGraphEntry other = (GetStaticGraphEntry) obj;
        if (sourceClass == null) {
            if (other.sourceClass != null)
                return false;
        } else if (!sourceClass.equals(other.sourceClass))
            return false;
        if (sourceMethod == null) {
            if (other.sourceMethod != null)
                return false;
        } else if (!sourceMethod.equals(other.sourceMethod))
            return false;
        if (targetClass == null) {
            return other.targetClass == null;
        } else return targetClass.equals(other.targetClass);
    }

}
