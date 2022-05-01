package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

import java.util.Set;

/**
 * Definition:
 * A test case performs tests on classes other than the one under test.
 *
 * Metric:
 * Total number of methods of other classes that are checked by a test case.
 *
 * Computation:
 * 1 - Iterate over the statements of a test case
 * [1: Start loop]
 * 2 - Verify if the current statement is an instance of MethodStatement
 * 3 (2 is True):
 *    3.1 - Get the method called in the respective statement
 *    3.2 - If the class that declares this method is different from the class under test: increment the smell counter
 * 4 - Verify if the current statement has assertions
 * 5 (4 is True):
 *    5.1 - Iterate over the assertions of the current statement
 *    [5.1: Start loop]
 *    5.2 - Verify if the current assertion is an instance of InspectorAssertion
 *    5.3 (5.2 is True):
 *       5.3.1 - Get the method on which the assertion is made
 *       5.3.2 - If the class that declares this method is different from the class under test: increment the smell counter
 *    [5.1: End loop]
 * [1: End loop]
 * 6 - Return the smell counter
 */
public class IndirectTesting extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = 803733954341223273L;

    public IndirectTesting() {
        super("TestSmellIndirectTesting");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof MethodStatement){
                if(!((MethodStatement) currentStatement).getDeclaringClassName().equals(Properties.TARGET_CLASS)){
                    count++;
                }
            }

            if(currentStatement.hasAssertions()){

                Set<Assertion> assertions = currentStatement.getAssertions();

                for(Assertion assertion : assertions) {
                    if (assertion instanceof InspectorAssertion) {
                        if(!((InspectorAssertion) assertion).getInspector().getMethod().getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }
}
