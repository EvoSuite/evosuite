package org.evosuite.config;

import java.io.Serializable;
import java.util.List;

/**
 * @author lianghong
 * @since 2021/10/23 11:34
 */
public class EvosuiteParameterConfig implements Serializable{

    private static final long serialVersionUID = -4248850615304592152L;
    /**
     * class name
     */
    private String className;
    /**
     * method name
     */
    private String methodName;
    /**
     * method parameters
     */
    private List<EvosuiteParameter> parameters;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<EvosuiteParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<EvosuiteParameter> parameters) {
        this.parameters = parameters;
    }
}
