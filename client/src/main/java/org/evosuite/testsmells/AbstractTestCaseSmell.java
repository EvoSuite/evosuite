package org.evosuite.testsmells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.TestSuiteChromosome;

public abstract class AbstractTestCaseSmell extends AbstractTestSmell {

    public AbstractTestCaseSmell(String name) {
        super(name);
    }

    /**
     * Calculate the smell count for a given test case
     * @param chromosome The test case that will be analyzed
     * @return int with the total smell count
     */
    public abstract int computeNumberOfSmells(TestChromosome chromosome);

    @Override
    public int computeNumberOfSmells(TestSuiteChromosome chromosome){
        int smellCount = 0;

        for(TestChromosome testcase : chromosome.getTestChromosomes()){
            smellCount += computeNumberOfSmells(testcase);
        }

        return smellCount;
    }

}
