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
 * 2 - A test case has an excessive number of assertions (typically because the test case is inspecting too much functionality)
 * EvoSuite does not generate assertion messages, so this metric only focuses on avoiding an excessive number of
 * assertions. A test case is only affected by this smell if the number of assertions is greater than the total
 * number of method calls.
 *
 * Metric:
 * Number of assertions in a test case that exceed the total amount of statements that call methods of
 * the class under test.
 *
 * Computation:
 * 1 - Create an assertion and a method counter - initialize both variables with the value 0
 * 2 - Let S = {S1,...,Sn} be the set of n statements in a test case
 * 3 - Iterate over S and, for each statement Si:
 * [3: Start loop]
 * 4 - Increment the assertion counter by the number of assertions in Si
 * 5 - Verify if Si corresponds to a method statement (instance of MethodStatement)
 * 6 (5 is True):
 *    6.1 - If the class that declares the method called in Si is the same as the class under test: increment the method counter
 * [3: End loop]
 * 7 - Calculate the difference between the assertion counter and the method counter to determine the number of assertions
 *     in the test case that exceed the total amount of statements that call methods of the class under test: use
 *     Math.max() to ensure that the result is always greater than or equal to zero
 * 8 - If the result is greater than or equal to zero, return the result; otherwise, return 0
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
