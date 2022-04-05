package org.evosuite.testsmells.smells;

import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;
import org.evosuite.testcase.statements.Statement;

import java.util.Set;

/**
 * Definition:
 * Input that is not controlled by a test case is checked by an assertion (the test is checking too much).
 *
 * Adaptation:
 * Without having full access to the class under test, it is difficult to know for sure which values change under
 * specific circumstances. Hence, the proposed metric focuses on avoiding assertions that may be unrelated to the
 * statement to which the assertion is added:
 * 1 - A method on which an inspector assertion is made is not guaranteed to be related to the specific statement
 *     to which the assertion is added;
 * 2 - A variable on which an assertion is made might be unrelated to the statement to which the assertion is added.
 *
 * Note: The computed metric considers assertions that cannot be proven to be related to the respective statement
 *       (i.e., the may still be related).
 *
 * Metric:
 * Number of assertions that may be unrelated to the statement to which the assertion is added.
 *
 * Computation:
 * 1 - Iterate over the statements of a test case
 * [1: Start loop]
 * 2 - Iterate over the assertions of the current statement
 * [2: Start loop]
 * 3 - Verify if the current assertion is an instance of InspectorAssertion
 * 4 (3 is True):
 *    4.1 - As we do not know if a method in an inspector assertion is related to the specific statement to which
 *          the assertion is added, we consider that, if possible, it is better to avoid using inspector
 *          assertions: increment the smell counter
 * 5 (3 is False):
 *    5.1 - If the position of the variable on which the assertion is made is different from the position of the
 *          statement to which the assertion is added: increment the smell counter
 * [2: End loop]
 * [1: End loop]
 * 6 - Return the smell counter
 */
public class BrittleAssertion extends AbstractNormalizedTestCaseSmell {

    public BrittleAssertion() {
        super("TestSmellBrittleAssertion");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            Set<Assertion> assertions = currentStatement.getAssertions();

            for(Assertion assertion : assertions){
                if(assertion instanceof InspectorAssertion){
                    count++;
                } else {
                    count += assertion.getSource().getStPosition() == i ? 0 : 1;
                }
            }
        }

        return count;
    }
}
