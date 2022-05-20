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
 * Number of valid assertions in a test case.
 *
 * Computation:
 * 1 - Let S = {S1,...,Sn} be the set of n statements in a test case
 * 2 - Iterate over S and, for each statement Si:
 * [2: Start loop]
 * 3 - If Si has assertions: return 0 (at least one assertion was found, so the test case is not affected by this smell)
 * [2: End loop]
 * 4 - Return 1 (none of the statements had assertions)
 */
public class UnknownTest extends AbstractTestCaseSmell {

    private static final long serialVersionUID = 8494696184138645912L;

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
