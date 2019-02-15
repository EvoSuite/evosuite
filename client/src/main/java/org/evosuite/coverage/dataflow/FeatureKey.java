package org.evosuite.coverage.dataflow;

import java.io.Serializable;

/**
 * This class represent a Key which is a combination of variable name and
 * method name. This key hepls to distinguish between two features having
 * same variable name in multiple methods. Local variables always have distinct
 * names but in order to handle complex object we serialize the object and each of
 * the sub-members become additional feature. And in this case the variable names
 * can be same for different methods. See FeatureNoveltyFunction.executeAndAnalyseFeature()
 * and FeatureNoveltyFunction.updateFeatureValueRange()
 */
public class FeatureKey implements Serializable {

    private String variableName;
    private String methodName;

    public FeatureKey(String variableName, String methodName){
        this.variableName = variableName;
        this.methodName = methodName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeatureKey that = (FeatureKey) o;

        if (!getVariableName().equals(that.getVariableName())) return false;
        return getMethodName().equals(that.getMethodName());
    }

    @Override
    public int hashCode() {
        int result = getVariableName().hashCode();
        result = 31 * result + getMethodName().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "FeatureKey{" +
                "variableName='" + variableName + '\'' +
                ", methodName='" + methodName + '\'' +
                '}';
    }
}
