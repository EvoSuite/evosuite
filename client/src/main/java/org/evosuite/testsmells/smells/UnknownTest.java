package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.util.List;

/**
 * Definition:
 * A test case does not have any valid assertions.
 *
 * Metric:
 * Verify whether a test case does not contain any valid assertions.
 *
 * Detection:
 * 1 - Iterate over the statements of a test case
 * [1: Start loop]
 * 2 - If the current statement has assertions: return 0 (at least one assertion was found, so the test case is
 *     not affected by this smell)
 * [1: End loop]
 * 4 - Return 1 (no assertions were found)
 */
public class UnknownTest extends AbstractTestCaseSmell {

    public UnknownTest() {
        super("TestSmellUnknownTest");
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
            smellCount += computeTestSmellMetric(testChromosome);
        }

        return smellCount / testChromosomes.size();
    }
}
