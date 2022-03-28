package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

public class EmptyTest extends AbstractTestCaseSmell {

    public EmptyTest() {
        super("TestSmellEmptyTest");
    }

    @Override
    public double computeNumberOfTestSmells(TestChromosome chromosome) {
        return computeTestSmellMetric(chromosome);
    }

    @Override
    public double computeTestSmellMetric(TestChromosome chromosome) {
        return chromosome.getTestCase().size() == 0 ? Integer.MAX_VALUE : 0;
    }

    @Override
    public double computeTestSmellMetric(TestSuiteChromosome chromosome) {
        double smellCount = 0;

        for(TestChromosome testcase : chromosome.getTestChromosomes()){
            smellCount += computeNumberOfTestSmells(testcase);
        }

        return smellCount;
    }
}
