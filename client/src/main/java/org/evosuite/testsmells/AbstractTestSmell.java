package org.evosuite.testsmells;

import org.evosuite.testsuite.TestSuiteChromosome;

import java.io.Serializable;

public abstract class AbstractTestSmell implements Serializable {

    private static final long serialVersionUID = 1605585391821192703L;
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

    /**
     * Compute the test smell metric for each test case in a given test suite
     * @param chromosome The analyzed test suite
     * @return String that corresponds to the computed test smell metric for each test case
     */
    public abstract String computeTestSmellMetricForEachTestCase(TestSuiteChromosome chromosome);
}
