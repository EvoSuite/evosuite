package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

/**
 * Definition:
 * Tests that take a long time to run.
 *
 * Metric:
 * Duration of the last execution of the test case.
 *
 * 1 - Verify if the last execution result is different from null
 * 2 (1 is True):
 *    2.1 - Get the duration of the last execution of the test case
 *    2.2 - Return the duration
 * 3 (1 is False):
 *    3.1 - Return 0
 */
public class SlowTests extends AbstractNormalizedTestCaseSmell {

    public SlowTests() {
        super("TestSmellSlowTests");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        if(chromosome.getLastExecutionResult() != null){
            return chromosome.getLastExecutionResult().getExecutionTime();
        }

        //Would it be better to run the test in this situation?
        return 0;
    }
}
