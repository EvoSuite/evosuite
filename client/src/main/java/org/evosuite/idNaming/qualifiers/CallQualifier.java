package org.evosuite.idNaming.qualifiers;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.utils.generic.GenericMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jmr on 06/08/15.
 */
public class CallQualifier implements TestNameQualifier {

    private String preposition = "Calls";
    private GenericMethod method = null;
    private List<TestNameQualifier> subQualifiers = new ArrayList<TestNameQualifier>();

    public CallQualifier(GenericMethod method) {
        this.method = method;
    }

    public CallQualifier(GenericMethod method, String preposition) {
        this.method = method;
        this.preposition = preposition;
    }

    public CallQualifier(GenericMethod method, String preposition, List<TestNameQualifier> subQualifiers) {
        this.method = method;
        this.preposition = preposition;
        this.subQualifiers = subQualifiers;
    }

    public void addSubQualifier(TestNameQualifier subQualifier) {
        this.subQualifiers.add(subQualifier);
    }

    public List<TestNameQualifier> getSubQualifiers() {
        return this.subQualifiers;
    }

    @Override
    public String toString() {
        return preposition + StringUtils.capitalize(method.getName());
    }
}
