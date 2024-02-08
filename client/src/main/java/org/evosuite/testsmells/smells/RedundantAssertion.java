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
 * Adaptation:
 * EvoSuite never generates assertions with the same values for the actual and expected parameters. As such, this
 * metric focuses on another type of redundant assertions: assertions that check primitive statements.
 *
 * Metric:
 * Total number of assertions that check primitive statements.
 *
 * Computation:
 * 1 - Create a smell counter and initialize the variable with the value 0
 * 2 - Let S = {S1,...,Sn} be the set of n statements in a test case and let A = {A1,...,Ak} be the set of k assertions
 *     in a statement Si
 * 3 - Iterate over S and, for each statement Si:
 * [3: Start loop]
 * 4 - Iterate over A of Si and, for each assertion Aj:
 * [4: Start loop]
 * 5 - Verify if Aj does not correspond to an inspector assertion (instance of InspectorAssertion)
 * 6 (5 is True):
 *    6.1 - If the statement that contains the variable on which the assertion is made corresponds to a primitive
 *          statement: increment the smell counter
 * [4: End loop]
 * [3: End loop]
 * 7 - Return the smell counter
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
