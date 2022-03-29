package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestCaseSmell;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Definition:
 * A test case checks multiple methods of the class under test (i.e., the test case verifies too much functionality).
 *
 * Metric:
 * Count the total number of different methods checked by a test case.
 *
 * Computation:
 * 1 - Iterate over the statements of a test case
 * 2 - Verify if the current statement is an instance of MethodStatement
 * 3 (2 is True):
 *    3.1 - Get the method called in the respective statement
 *    3.2 - If the class that declares this method is the same as the class under test: add method to a LinkedHashSet
 *          of methods (a LinkedHashSet does not store duplicate elements).
 *    3.3 - Verify if the current statement has assertions
 *    3.4 (3.3 is True):
 *       3.4.1 - Iterate over the assertions of the current statement
 *       3.4.2 - Verify if the current assertion is an instance of InspectorAssertion
 *       3.4.3 (3.4.2 is True):
 *          3.4.3.1 - Get the method on which the assertion is made
 *          3.4.3.2 - If the class that declares this method is the same as the class under test: add method to LinkedHashSet
 *                    of methods
 * 4 - Return the number of elements in the LinkedHashSet of methods
 */
public class EagerTest extends AbstractTestCaseSmell {

    public EagerTest() {
        super("TestSmellEagerTest");
    }

    @Override
    public double computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();

        Statement currentStatement;
        Method method;

        Set<Method> setOfMethods = new LinkedHashSet<>();

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof MethodStatement){
                method = ((MethodStatement) currentStatement).getMethod().getMethod();
                if(method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                    setOfMethods.add(method);
                }
            }

            if(currentStatement.hasAssertions()){

                Set<Assertion> assertions = currentStatement.getAssertions();

                for(Assertion assertion : assertions){
                    if(assertion instanceof InspectorAssertion){
                        method = ((InspectorAssertion) assertion).getInspector().getMethod();
                        if(method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                            setOfMethods.add(method);
                        }
                    }
                }
            }
        }

        return setOfMethods.size();
    }
}
