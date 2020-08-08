package org.evosuite.ga;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * Mocks fitness functions for {@code TestChromosome}s as fitness functions for
 * {@code TestSuiteChromosome}s.
 */
public class TestSuiteFitnessFunctionMock
        extends FitnessFunctionMock<TestChromosome, TestSuiteChromosome> {

    /**
     * {@inheritDoc}
     */
    public TestSuiteFitnessFunctionMock(final FitnessFunction<TestChromosome> wrapped) {
        super(wrapped);
    }
}
