package org.evosuite.symbolic.dse.algorithm;

import org.evosuite.symbolic.PathCondition;

public class DSEPathCondition {
    private PathCondition pathCondition;

    /**
     * Index from which this path condition was generated.
     * Useful to avoid recreating the same path conditions
     */
    private int generatedFromIndex;

    public DSEPathCondition(PathCondition pathCondition, int generatedFromIndex) {
        this.pathCondition = pathCondition;
        this.generatedFromIndex = generatedFromIndex;
    }

    public int getGeneratedFromIndex() {
        return generatedFromIndex;
    }

    public PathCondition getPathCondition() {
        return pathCondition;
    }
}
