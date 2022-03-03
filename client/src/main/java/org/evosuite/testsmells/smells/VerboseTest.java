package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;

public class VerboseTest extends AbstractTestCaseSmell {

    public VerboseTest() {
        super("TestSmellVerboseTest");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        return chromosome.size();
    }
}