package org.evosuite.graphs.ddg;

/**
 * Java Bean that represents a method or constructor of a class. The Bean holds the methods's or
 * constructor's name and descriptor, and the name of its owner class.
 */
public class MethodEntry extends ClassMember {
    private final String className;
    private final String methodName;
    private final String descriptor;

    public MethodEntry(String className, String methodName,
                       String descriptor) {
        this.className = className;
        this.methodName = methodName;
        this.descriptor = descriptor;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + className.hashCode();
        result = prime * result + descriptor.hashCode();
        result = prime * result + methodName.hashCode();
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
        MethodEntry other = (MethodEntry) obj;
        return (className.equals(other.className) && methodName
                .equals(other.methodName))
                && descriptor.equals(other.descriptor);
    }

    @Override
    public String toString() {
        return className + "." + methodName + descriptor;
    }


    @Override
    public boolean isField() {
        return false;
    }

    @Override
    public boolean isMethod() {
        return true;
    }
}
