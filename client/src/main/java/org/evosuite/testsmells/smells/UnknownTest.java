package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestCaseSmell;

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
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 1;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            if(currentStatement.hasAssertions()){
                count = 0;
                break;
            }
        }

        // We will have to define a value greater than 1
        return count;
    }
}
