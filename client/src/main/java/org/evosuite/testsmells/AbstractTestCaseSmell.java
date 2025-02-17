package org.evosuite.testsmells;

import org.evosuite.testcase.TestChromosome;

public abstract class AbstractTestCaseSmell extends AbstractTestSmell {

    private static final long serialVersionUID = -323403603309674812L;

    public AbstractTestCaseSmell(String name) {
        super(name);
    }

    /**
     * Compute the test smell metric for a given test case
     * @param chromosome The analyzed test case
     * @return double that corresponds to the computed test smell metric
     */
    public abstract double computeTestSmellMetric(TestChromosome chromosome);
}
