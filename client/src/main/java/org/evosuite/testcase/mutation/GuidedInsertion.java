package org.evosuite.testcase.mutation;

import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class GuidedInsertion implements InsertionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(GuidedInsertion.class);

    private Set<TestFitnessFunction> goals = Collections.emptySet();

    // singleton design pattern, use getInstance() instead
    private GuidedInsertion() { }

    public static GuidedInsertion getInstance() {
        return SingletonContainer.instance;
    }

    /**
     * Sets the goals to be targeted during the insertion process.
     *
     * @param goals the goals intended for covering
     */
    public void setGoals(final Set<TestFitnessFunction> goals) {
        this.goals = Objects.requireNonNull(goals);
    }

    @Override
    public int insertStatement(TestCase test, int lastPosition) {
        throw new RuntimeException("not implemented");
    }

    private static final class SingletonContainer {
        private static final GuidedInsertion instance = new GuidedInsertion();
    }
}
