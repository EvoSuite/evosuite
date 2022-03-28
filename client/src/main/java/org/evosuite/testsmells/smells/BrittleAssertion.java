package org.evosuite.testsmells.smells;

import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testcase.statements.Statement;

import java.util.Set;

/**
 * Definition:
 * Input that is not controlled by a test case is checked by an assertion.
 *
 * Adaptation:
 * Without accessing the class under test, we cannot know for sure which values change under specific circumstances.
 * Hence, we decided to focus on avoiding assertions that may not be related to the statement to which assertion is added:
 * 1 - A method in an inspector assertion is not guaranteed to be related to the specific statement to which the assertion is added;
 * 2 - The variable on which the assertion is made might be unrelated to the statement to which the assertion is added.
 *
 * Computation:
 * 1 - Iterate over the statements of a test case
 * 2 - Iterate over the assertions of the current statement
 * 3 - Verify if the current assertion is an instance of InspectorAssertion
 * 4 (3 is True):
 *    4.1 - As we do not know if a method in an inspector assertion is related to the specific statement to which
 *          the assertion is added, we consider that, if possible, it is better to avoid using inspector
 *          assertions: increment the smell counter
 * 5 (3 is False):
 *    5.1 - If the position of the variable on which the assertion is made is different from the position of the
 *          statement to which the assertion is added: increment the smell counter
 * 6 - Return the smell counter
 */
public class BrittleAssertion extends AbstractTestCaseSmell {

    public BrittleAssertion() {
        super("TestSmellBrittleAssertion");
    }

    @Override
    public double computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

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
