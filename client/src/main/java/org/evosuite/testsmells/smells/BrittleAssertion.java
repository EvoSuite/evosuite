package org.evosuite.testsmells.smells;

import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;
import org.evosuite.testcase.statements.Statement;

import java.util.Set;

/**
 * Definition:
 * Assertions check values derived from input not controlled by a test case (the test is checking too much).
 *
 * Adaptation:
 * Without having full access to the class under test, it is difficult to know for sure if an assertion checks values
 * not controlled by the test case. Hence, the proposed metric only focuses on assertions that may be unrelated to the
 * statements to which they are added (i.e., that cannot be proven to be related to the respective statements):
 * -> The method on which an inspector assertion is made is not guaranteed to be related to the specific statement to
 *    which the assertion is added;
 * -> The variable on which an assertion is made might be unrelated to the statement to which the assertion is added.
 *
 * Metric:
 * Number of assertions that may be unrelated to the statements to which they are added.
 *
 * Computation:
 * 1 - Create a smell counter and initialize the variable with the value 0
 * 2 - Let S = {S1,...,Sn} be the set of n statements in a test case and let A = {A1,...,Ak} be the set of k assertions
 *     in a statement Si
 * 3 - Iterate over S and, for each statement Si:
 * [3: Start loop]
 * 4 - Iterate over A of Si and, for each assertion Aj:
 * [4: Start loop]
 * 5 - Verify if Aj corresponds to an inspector assertion (instance of InspectorAssertion)
 * 6 (5 is True):
 *    6.1 - Increment the smell counter (as we do not know if a method in an inspector assertion is related to the
 *          specific statement to which the assertion is added, we consider that, if possible, it is better to avoid
 *          using inspector assertions)
 * 7 (5 is False):
 *    7.1 - If the position of the variable on which the assertion is made is different from the position of the
 *          statement to which the assertion is added: increment the smell counter
 * [4: End loop]
 * [3: End loop]
 * 8 - Return the smell counter
 */
public class BrittleAssertion extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = -14599848509702936L;

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
