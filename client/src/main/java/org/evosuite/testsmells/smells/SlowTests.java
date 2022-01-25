package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;

public class SlowTests extends AbstractTestCaseSmell {

    public SlowTests() {
        super("TestSmellSlowTests");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        return (int) chromosome.getDuration();
    }
}
