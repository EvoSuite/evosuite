package org.evosuite.ga;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * Mocks factories for {@code TestChromosome}s as factories for
 * {@code TestSuiteChromosome}s.
 */
public class TestSuiteChromosomeFactoryMock
        extends ChromosomeFactoryMock<TestChromosome, TestSuiteChromosome> {

    private static final long serialVersionUID = 8395282399919895283L;

    /**
     * {@inheritDoc}
     */
    public TestSuiteChromosomeFactoryMock(final ChromosomeFactory<TestChromosome> wrapped) {
        super(wrapped);
    }
}
