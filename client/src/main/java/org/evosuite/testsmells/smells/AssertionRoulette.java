package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

/**
 * Definition:
 * A test case has several unexplained assertions.
 *
 * Adaptation:
 * This smell can occur for one of two reasons:
 * 1 - A test case has assertions without assertion messages
 * 2 - A test case has an excessive number of assertions
 * EvoSuite does not generate assertions with such messages, so this metric only focuses on avoiding an excessive
 * number of assertions. Before establishing this metric, it is necessary to stipulate what corresponds to an
 * "excessive" number of assertions. Specifically, a test case is only affected by this smell if the number of
 * assertions is greater than the total number of method calls.
 *
 * Metric:
 * Number of assertions in a test case that exceed the total amount of statements that call methods of
 * the class under test.
 *
 * Computation:
 * 1 - Iterate over the statements of a test case
 * [1: Start loop]
 * 2 - Increment the smell counter by the number of assertions in the current statement
 * 3 - Verify if the current statement is an instance of MethodStatement
 * 4 (3 is True):
 *    4.1 - Get the method called in the respective statement
 *    4.2 - If the class that declares this method is the same as the class under test: increment methodCalls counter
 * [1: End loop]
 * 5 - Calculate the difference between the smell counter and the methodCalls counter to determine the number of
 *     assertions in the test case that exceed the total amount of statements that call methods of the class under
 *     test: Math.max() is used to ensure that the result is always greater than or equal to zero
 * 6 - Return the the final result
 */
public class AssertionRoulette extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = 2415248538433560358L;

    public AssertionRoulette() {
        super("TestSmellAssertionRoulette");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;
        long methodCalls = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            count += currentStatement.getAssertions().size();

            if(currentStatement instanceof MethodStatement){
                if(((MethodStatement) currentStatement).getDeclaringClassName().equals(Properties.TARGET_CLASS)){
                    methodCalls++;
                }
            }
        }

        return Math.max(0, count - methodCalls);
    }
}
