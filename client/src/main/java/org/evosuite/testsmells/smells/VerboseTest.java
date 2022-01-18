package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestSmell;

public class VerboseTest extends AbstractTestSmell {

    public VerboseTest() {
        setSmellName("Verbose Test");
    }

    @Override
    public int obtainSmellCount(TestChromosome chromosome) {
        return chromosome.size();
    }
}
