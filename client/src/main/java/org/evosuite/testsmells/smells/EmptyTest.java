package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestSmell;

public class EmptyTest extends AbstractTestSmell {

    public EmptyTest(String smellName) {
        super(smellName);
    }

    @Override
    public int obtainSmellCount(TestChromosome chromosome) {
        return chromosome.getTestCase().size() == 0 ? 10000 : 0;
    }
}
