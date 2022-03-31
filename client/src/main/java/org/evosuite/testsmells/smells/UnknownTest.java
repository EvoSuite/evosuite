package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.List;

/**
 * Detection:
 * 1 - Initialize the smell counter with value 1 (we start by assuming that the test case has no assertions)
 * 2 - Iterate over the statements of a test case
 * 3 - Verify whether the current statement has assertions
 * 4 (True):
 *    4.1 - Change the smell counter to 0
 *    4.2 - As at least one assertion has been found, we know that the test case is not affected by this smell
 *    4.3 - Stop the loop iterations
 * 5 - Return the smell counter
 */
public class UnknownTest extends AbstractTestCaseSmell {

    public UnknownTest() {
        super("TestSmellUnknownTest");
    }

    @Override
    public double computeNumberOfTestSmells(TestChromosome chromosome) {
        return computeTestSmellMetric(chromosome);
    }

    @Override
    public double computeTestSmellMetric(TestChromosome chromosome) {
        int size = chromosome.size();

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            if(currentStatement.hasAssertions()){
                return 0;
            }
        }

        return 1;
    }

    @Override
    public double computeTestSmellMetric(TestSuiteChromosome chromosome) {
        double smellCount = 0;

        List<TestChromosome> testChromosomes = chromosome.getTestChromosomes();

        for(TestChromosome testChromosome : testChromosomes){
            smellCount += computeNumberOfTestSmells(testChromosome);
        }

        return smellCount / testChromosomes.size();
    }
}
