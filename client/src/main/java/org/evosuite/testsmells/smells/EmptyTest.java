package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * Definition:
 * Test cases that do not have executable statements.
 *
 * Metric:
 * Verify whether a test case does not contain any executable statements.
 *
 * Computation:
 * 1 - Verify if the test case has 0 statements (i.e., if it is empty)
 * 2 (1 is True):
 *    2.1 - Return 1
 * 3 (1 is False):
 *    3.1 - Return 0
 */
public class EmptyTest extends AbstractTestCaseSmell {

    private static final long serialVersionUID = -1575307787986268961L;

    public EmptyTest() {
        super("TestSmellEmptyTest");
    }

    @Override
    public double computeTestSmellMetric(TestChromosome chromosome) {
        return chromosome.getTestCase().size() == 0 ? 1 : 0;
    }

    @Override
    public double computeTestSmellMetric(TestSuiteChromosome chromosome) {

        for(TestChromosome testcase : chromosome.getTestChromosomes()){
            if(computeTestSmellMetric(testcase) > 0){
                return 1;
            }
        }

        return 0;
    }
}
