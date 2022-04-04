package org.evosuite.testsmells.smells;

import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;
import org.evosuite.testcase.statements.Statement;

import java.util.Set;

/**
 * Definition:
 * Test cases that have assertions that are either permanently true or permanently false.
 *
 * Metric:
 * Count the total number of assertions that check primitive statements.
 *
 * Computation:
 * 1 - Iterate over the statements of a test case
 * [1: Start loop]
 * 2 - Verify if the current statement is an instance of PrimitiveStatement
 * 3 (2 is True):
 *    3.1 - Iterate over the assertions of the current statement
 *    [3.1: Start loop]
 *    3.2 - If the current assertion is not an instance of InspectorAssertion: increment the smell counter
 *    [3.1: End loop]
 * [1: End loop]
 * 4 - Return the smell counter
 */
public class RedundantAssertion extends AbstractNormalizedTestCaseSmell {

    public RedundantAssertion() {
        super("TestSmellRedundantAssertion");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof PrimitiveStatement){

                Set<Assertion> assertions = currentStatement.getAssertions();

                for(Assertion assertion : assertions){
                    count += assertion instanceof InspectorAssertion ? 0 : 1;
                }
            }
        }

        return count;
    }
}
