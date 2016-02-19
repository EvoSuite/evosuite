/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.utils.generic.GenericClass;

/**
 * Created by Andrea Arcuri on 06/12/15.
 */
public class DependencyPair {
    private final int recursion;
    private final GenericClass dependencyClass;

    public DependencyPair(int recursion, java.lang.reflect.Type dependencyClass) {
        this.recursion = recursion;
        this.dependencyClass = new GenericClass(dependencyClass);
    }

    public int getRecursion() {
        return recursion;
    }

    public GenericClass getDependencyClass() {
        return dependencyClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((dependencyClass == null) ? 0 : dependencyClass.hashCode());
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
        DependencyPair other = (DependencyPair) obj;
        if (dependencyClass == null) {
            if (other.dependencyClass != null)
                return false;
        } else if (!dependencyClass.equals(other.dependencyClass))
            return false;
        return true;
    }

}
