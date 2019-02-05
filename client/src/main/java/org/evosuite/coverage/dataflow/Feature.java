package org.evosuite.coverage.dataflow;

import java.io.Serializable;

public class Feature implements Serializable {


    private String variableName;
    private String methodName;
    private String typeClass;
    private Object value;
    double normalizedValue;

    public Feature(){

    }

    public Feature(Feature feature) {
        this.variableName = feature.getVariableName();
        this.value = feature.getValue();
        this.typeClass = feature.getTypeClass();
        this.methodName = feature.getMethodName();
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getTypeClass() {
        return typeClass;
    }

    public void setTypeClass(String typeClass) {
        this.typeClass = typeClass;
    }

    public double getNormalizedValue() {
        return normalizedValue;
    }

    public void setNormalizedValue(double normalizedValue) {
        this.normalizedValue = normalizedValue;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "Feature{" +
                "variableName='" + variableName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", typeClass='" + typeClass + '\'' +
                ", value=" + value + '\'' +
                ", normalizedValue=" + normalizedValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Feature feature = (Feature) o;

        if (getVariableName() != null ? !getVariableName().equals(feature.getVariableName()) : feature.getVariableName() != null)
            return false;
        if (getTypeClass() != null ? !getTypeClass().equals(feature.getTypeClass()) : feature.getTypeClass() != null)
            return false;
        return getValue() != null ? getValue().equals(feature.getValue()) : feature.getValue() == null;
    }

    @Override
    public int hashCode() {
        int result = getVariableName() != null ? getVariableName().hashCode() : 0;
        result = 31 * result + (getTypeClass() != null ? getTypeClass().hashCode() : 0);
        result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
        return result;
    }
}
