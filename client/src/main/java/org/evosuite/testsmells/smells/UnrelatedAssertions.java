package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Definition:
 * A test case has assertions that check methods that are not declared in the class under test.
 *
 * Metric:
 * Count the total number of assertions that check methods that are not declared in the class under test.
 *
 * Detection:
 * 1 - Iterate over the statements of a test case
 * [1: Start loop]
 * 2 - Iterate over the assertions of the current statement
 * [2: Start loop]
 * 3 - Verify if the current assertion is an instance of InspectorAssertion
 * 4 (3 is True):
 *    4.1 - Get the method on which the assertion is made
 *    4.2 - If the class that declares this method is different from the class under test: increment the smell counter
 * 5 (3 is False):
 *    5.1 - Get the statement that contains the variable on which the assertion is made
 *    5.2 - Verify if the current statement is an instance of MethodStatement
 *    5.3 (5.2 is True):
 *       5.3.1 - Get the method called in the respective statement
 *       5.3.2 - If the class that declares this method is different from the class under test: increment the smell counter
 * [2: End loop]
 * [1: End loop]
 * 6 - Return the smell counter
 */
public class UnrelatedAssertions extends AbstractNormalizedTestCaseSmell {

    public UnrelatedAssertions() {
        super("TestSmellUnrelatedAssertions");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;

        TestCase testCase = chromosome.getTestCase();

        for (int i = 0; i < size; i++){
            Set<Assertion> assertions = testCase.getStatement(i).getAssertions();

            for(Assertion assertion : assertions){
                if(assertion instanceof InspectorAssertion){
                    Method method = ((InspectorAssertion) assertion).getInspector().getMethod();

                    if(!method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                        count ++;
                    }
                } else {
                    Statement variableStatement = testCase.getStatement(assertion.getSource().getStPosition());

                    if(variableStatement instanceof MethodStatement){
                        Method method = ((MethodStatement) variableStatement).getMethod().getMethod();

                        if(!method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                            count ++;
                        }
                    }
                }
            }
        }

        return count;
    }
}
