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
 *    2.1 - Return a value equivalent to the total number of analyzed test smells: an empty test is the worst type of
 *          test because it lacks any evolutionary potential - as such, it has the highest smell score
 * 3 (1 is False):
 *    3.1 - Return 0
 */
public class EmptyTest extends AbstractTestCaseSmell {

    public EmptyTest() {
        super("TestSmellEmptyTest");
    }

    @Override
    public double computeTestSmellMetric(TestChromosome chromosome) {
        return chromosome.getTestCase().size() == 0 ? 11 : 0;
    }

    @Override
    public double computeTestSmellMetric(TestSuiteChromosome chromosome) {

        for(TestChromosome testcase : chromosome.getTestChromosomes()){
            if(computeTestSmellMetric(testcase) > 0){
                // Should the optimization of test smell metrics as a secondary criterion also work for test suites?
                // If so, we need to find a way to get this value dynamically!
                return 22;
            }
        }

        return 0;
    }
}
