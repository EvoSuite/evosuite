package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

public class EmptyTest extends AbstractTestSmell {

    public EmptyTest(String smellName) {
        super(smellName);
    }

    @Override
    public int obtainSmellCount(TestChromosome chromosome) {
        return chromosome.getTestCase().size() == 0 ? Integer.MAX_VALUE : 0;
    }

    @Override
    public int obtainSmellCount(TestSuiteChromosome chromosome) {
        int smellCount = 0;

        for(TestChromosome testcase : chromosome.getTestChromosomes()){
            smellCount += obtainSmellCount(testcase);
        }

        return smellCount;
    }
}
