package org.evosuite.ga;

public class RecursiveConstructionFailedException extends ConstructionFailedException {

    private static final long serialVersionUID = -2934799333306971428L;
    public RecursiveConstructionFailedException(String reason) {
        super(reason);
    }

}
