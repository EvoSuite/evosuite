package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;

/**
 * 1 - Verify if the last execution result is different than null
 * 2 (1 is True):
 *    2.1 - Get the execution time of the last execution result
 *    2.2 - Return the last execution time
 * 3 (1 is False):
 *    3.1 - Return 0
 */
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
