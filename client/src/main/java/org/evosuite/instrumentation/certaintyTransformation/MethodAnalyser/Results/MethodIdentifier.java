package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results;

import java.util.Objects;

public class MethodIdentifier {

    protected final String internalClassName;
    protected final String methodName;
    protected final String methodDescriptor;
    protected final int lineNumber;

    public MethodIdentifier(String internalClassName, String methodName, String methodDescriptor){
        this(internalClassName,methodName,methodDescriptor,-1);
    }

    public MethodIdentifier(String internalClassName, String methodName, String methodDescriptor, int lineNumber) {
        this.lineNumber = lineNumber;
        Objects.requireNonNull(internalClassName);
        Objects.requireNonNull(methodName);
        Objects.requireNonNull(methodDescriptor);
        this.internalClassName = internalClassName;
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getInternalClassName() {
        return internalClassName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDescriptor() {
        return methodDescriptor;
    }

    public boolean matchesClass(String internalName){
        return this.internalClassName.equals(internalName);
    }

    public boolean matchesMethod(String methodName){
        return this.methodName.equals(methodName);
    }

    public boolean matchesDescriptor(String methodDescriptor){
        return this.methodDescriptor.equals(methodDescriptor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodIdentifier that = (MethodIdentifier) o;

        if (!internalClassName.equals(that.internalClassName)) return false;
        if (!methodName.equals(that.methodName)) return false;
        return methodDescriptor.equals(that.methodDescriptor);
    }

    @Override
    public int hashCode() {
        int result = internalClassName.hashCode();
        result = 31 * result + methodName.hashCode();
        result = 31 * result + methodDescriptor.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MethodIdentifier{" +
                "internalClassName='" + internalClassName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", methodDescriptor='" + methodDescriptor + '\'' +
                '}';
    }
}
