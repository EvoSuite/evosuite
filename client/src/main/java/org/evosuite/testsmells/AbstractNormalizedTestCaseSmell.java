package org.evosuite.testsmells;

import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

public abstract class AbstractNormalizedTestCaseSmell extends AbstractTestCaseSmell {

    private static final long serialVersionUID = 7365306489099478724L;

    public AbstractNormalizedTestCaseSmell(String name) {
        super(name);
    }

    /**
     * Calculate the smell count for a given test case
     * @param chromosome The analyzed test case
     * @return long with the total smell count
     */
    public abstract long computeNumberOfTestSmells(TestChromosome chromosome);

    @Override
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
