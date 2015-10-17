package org.evosuite.idNaming.qualifiers;

import org.evosuite.coverage.TestCoverageGoal;

/**
 * Created by jmr on 06/08/15.
 */
public class ArgumentQualifier implements TestNameQualifier {

    private String param;
    private String value;
    private String preposition = "With";
    private String connector = "Equals";


    public ArgumentQualifier(String param, String value) {
        this.param = param;
        this.value = value;
    }

    public ArgumentQualifier(String param, String value, String preposition) {
        this.param = param;
        this.value = value;
        this.preposition = preposition;
    }

    public ArgumentQualifier(String param, String value, String preposition, String connector) {
        this.param = param;
        this.value = value;
        this.preposition = preposition;
        this.connector = connector;
    }

    @Override
    public String toString() {
        return preposition + param + connector + value;
    }
}
