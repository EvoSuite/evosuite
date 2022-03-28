package org.evosuite.testsmells;

import org.evosuite.testsuite.TestSuiteChromosome;

public abstract class AbstractTestSmell {

    private final String name;

    public AbstractTestSmell(String name) {
        this.name = name;
    }

    /**
     * Obtain the name of the test smell
     * @return String that corresponds to the name of the test smell
     */
    public String getName() {
        return name;
    }

    /**
     * Compute the test smell metric for a given test suite
     * @param chromosome The analyzed test suite
     * @return double that corresponds to the computed test smell metric
     */
    public abstract double computeTestSmellMetric(TestSuiteChromosome chromosome);
}
