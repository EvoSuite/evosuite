package org.evosuite.ga;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * Mocks factories for {@code TestChromosome}s as factories for
 * {@code TestSuiteChromosome}s.
 */
public class TestChromosomeFactoryMock
        extends ChromosomeFactoryMock<TestChromosome, TestSuiteChromosome> {

    /**
     * {@inheritDoc}
     */
    public TestChromosomeFactoryMock(final ChromosomeFactory<TestChromosome> wrapped) {
        super(wrapped);
    }
}
