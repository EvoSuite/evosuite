package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;

public class EmptyTest extends AbstractTestCaseSmell {

    public EmptyTest() {
        super("TestSmellEmptyTest");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        return chromosome.getTestCase().size() == 0 ? Integer.MAX_VALUE : 0;
    }
}
