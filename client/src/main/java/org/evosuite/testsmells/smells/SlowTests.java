package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestSmell;

public class SlowTests extends AbstractTestSmell {

    public SlowTests() {
        setSmellName("Slow Tests");
    }

    @Override
    public int obtainSmellCount(TestChromosome chromosome) {
        return (int) chromosome.getDuration();
    }
}
