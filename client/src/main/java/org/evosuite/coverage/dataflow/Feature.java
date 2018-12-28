package org.evosuite.coverage.dataflow;

import java.io.Serializable;

public class Feature implements Serializable {


    String variableName;
    String typeClass;
    Object value;

    public Feature(){

    }

    public Feature(Feature feature) {
        this.variableName = feature.variableName;
        this.value = feature.value;
        this.typeClass = feature.typeClass;
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

    @Override
    public String toString() {
        return "Feature{" +
                "variableName='" + variableName + '\'' +
                ", typeClass='" + typeClass + '\'' +
                ", value=" + value +
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
