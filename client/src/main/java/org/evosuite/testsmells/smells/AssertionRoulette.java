package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

import java.lang.reflect.Method;

/**
 * Definition:
 * A test case has several unexplained assertions.
 *
 * Adaptation:
 * This smell typically applies to test cases that have multiple assertions without assertion messages. However,
 * EvoSuite does not generate assertions with such messages. Therefore, this metric focuses on avoiding an excessive
 * number of assertions. A test case
 *
 * A test case may have too many assertions if the number of assertions in a test case exceeds the total number of
 * method calls.
 *
 * Metric:
 * Number of assertions in a test case that exceed the number of statements that call methods of the class under test.
 *
 * Computation:
 * 1 - Iterate over the statements of a test case
 * 2 - Increment the smell counter by the number of assertions in the current statement
 * 3 - Verify if the current statement is an instance of MethodStatement
 * 4 (3 is True):
 *    4.1 - Get the method called in the respective statement
 *    4.2 - If the class that declares this method is the same as the class under test: increment numStatements counter
 * 5 - To determine the number of assertions that exceed the number of statements that call methods of the class
 *     under test, it is necessary to calculate the difference between the smell counter and the numStatements
 *     counter: Math.max() is used to ensure that the result is always greater than or equal to zero
 * 6 - Return the the final result
 */
public class AssertionRoulette extends AbstractNormalizedTestCaseSmell {

    public AssertionRoulette() {
        super("TestSmellAssertionRoulette");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;
        long numStatements = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            count += currentStatement.getAssertions().size();

            if(currentStatement instanceof MethodStatement){
                Method method = ((MethodStatement) currentStatement).getMethod().getMethod();
                if(method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                    numStatements++;
                }
            }
        }

        return Math.max(0, count - numStatements);
    }
}
