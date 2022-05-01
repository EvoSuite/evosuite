package org.evosuite.testsmells.smells;

import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestCase;
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
 * Total number of assertions that check primitive statements.
 *
 * Computation:
 * 1 - Iterate over the statements of a test case
 * [1: Start loop]
 * 2 - Iterate over the assertions of the current statement
 * [2: Start loop]
 * 3 - Verify if the current assertion is not an instance of InspectorAssertion
 * 4 (3 is True):
 *    4.1 - Get the statement that contains the variable on which the assertion is made
 *    4.2 - If this statement is an instance of PrimitiveStatement: increment the smell counter
 * [2: End loop]
 * [1: End loop]
 * 5 - Return the smell counter
 */
public class RedundantAssertion extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = -7526406592569488867L;

    public RedundantAssertion() {
        super("TestSmellRedundantAssertion");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;

        TestCase testCase = chromosome.getTestCase();

        for (int i = 0; i < size; i++){

            Set<Assertion> assertions = testCase.getStatement(i).getAssertions();

            for(Assertion assertion : assertions){

                if(!(assertion instanceof InspectorAssertion)){
                    Statement variableStatement = testCase.getStatement(assertion.getSource().getStPosition());
                    count += variableStatement instanceof PrimitiveStatement ? 1 : 0;
                }
            }
        }

        return count;
    }
}
