package org.evosuite.ga;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * Mocks fitness functions for {@code TestChromosome}s as fitness functions for
 * {@code TestSuiteChromosome}s.
 */
public class TestSuiteFitnessFunctionMock
        extends FitnessFunctionMock<TestChromosome, TestSuiteChromosome> {

    private static final long serialVersionUID = 7438166177586343112L;

    /**
     * {@inheritDoc}
     */
    public TestSuiteFitnessFunctionMock(final FitnessFunction<TestChromosome> wrapped) {
        super(wrapped);
    }
}
