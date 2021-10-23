package org.evosuite.config;

import java.io.Serializable;
import java.util.List;

/**
 * @author lianghong
 * @since 2021/10/23 11:36
 */
public class EvosuiteParameter implements Serializable {

    private static final long serialVersionUID = 2318050528126174268L;

    private String parameterName;

    private String parameterType;

    private List<String> parameterValues;

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public List<String> getParameterValues() {
        return parameterValues;
    }

    public void setParameterValues(List<String> parameterValues) {
        this.parameterValues = parameterValues;
    }
}
