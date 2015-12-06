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
