package org.evosuite.testcase.mutation;

import org.evosuite.testcase.TestCase;

public class GuidedInsertion implements InsertionStrategy {

    // singleton design pattern, use getInstance() instead
    private GuidedInsertion() { }

    public static GuidedInsertion getInstance() {
        return SingletonContainer.instance;
    }

    @Override
    public int insertStatement(TestCase test, int lastPosition) {
        throw new RuntimeException("not implemented");
    }

    private static final class SingletonContainer {
        private static final GuidedInsertion instance = new GuidedInsertion();
    }
}
