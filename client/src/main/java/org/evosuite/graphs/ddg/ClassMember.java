package org.evosuite.graphs.ddg;

/**
 * Common abstract superclass for all Java Beans that represent members of a class.
 */
public abstract class ClassMember {
    public abstract boolean isField();
    public abstract boolean isMethod();
}