package org.evosuite.testsmells;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

public abstract class AbstractTestCaseSmell extends AbstractTestSmell {

    public AbstractTestCaseSmell(String name) {
        super(name);
    }

    /**
     * Calculate the smell count for a given test case
     * @param chromosome The analyzed test case
     * @return long with the total smell count
     */
    public abstract long computeNumberOfTestSmells(TestChromosome chromosome);

    /**
     * Compute the test smell metric for a given test case
     * @param chromosome The analyzed test case
     * @return double that corresponds to the computed test smell metric
     */
    public double computeTestSmellMetric(TestChromosome chromosome) {
        return FitnessFunction.normalize(computeNumberOfTestSmells(chromosome));
    }

    @Override
    public double computeTestSmellMetric(TestSuiteChromosome chromosome) {
        double smellCount = 0;

        for(TestChromosome testcase : chromosome.getTestChromosomes()){
            smellCount += computeNumberOfTestSmells(testcase);
        }

        return FitnessFunction.normalize(smellCount);
    }

}
