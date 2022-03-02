package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;

public class SlowTests extends AbstractTestCaseSmell {

    public SlowTests() {
        super("TestSmellSlowTests");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        if(chromosome.getLastExecutionResult() != null){
            return (int) chromosome.getLastExecutionResult().getExecutionTime();
        }

        //Would it be better to run the test in this situation?
        return 0;
    }
}
