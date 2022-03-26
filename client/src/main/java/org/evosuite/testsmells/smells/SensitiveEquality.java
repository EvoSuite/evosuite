package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestCaseSmell;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Detection:
 * 1 - Iterate over the statements of a test case
 * 2 - Verify if the current statement is an instance of MethodStatement
 * 3 (2 is True):
 *    3.1 - Get the name of the method called in the respective statement
 *    3.2 - Get the name of the class that declares the method
 *    3.3 - Verify whether (1) the name of the method is equal to "toString" and (2) the class that declares the method
 *          does not correspond to the class under test - we have to make this distinction because the class under test
 *          may implement its own version of the "toString" method, so it is necessary to exercise the method
 *    3.4 (3.3 is True):
 *       3.4.1 - If the current statement has assertions: increment the smell counter
 * 4 - Verify if the current statement has assertions
 * 5 (4 is True):
 *    5.1 - Iterate over the assertions of the current statement
 *    5.2 - Verify if the current assertion is an instance of InspectorAssertion
 *    5.3 (5.2 is True):
 *       5.3.1 - Get the name of the method on which the assertion is made and the name of the class within
 *               which this method is declared
 *       5.3.2 - If the name of the method is equal to "toString" and the class that declares the method is different
 *               from the class under test: increment the smell counter
 * 6 - Return the smell counter
 */
public class SensitiveEquality extends AbstractTestCaseSmell {

    public SensitiveEquality() {
        super("TestSmellSensitiveEquality");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof MethodStatement){
                String curr = ((MethodStatement) currentStatement).getMethodName();
                String className = ((MethodStatement) currentStatement).getDeclaringClassName();

                if(curr.equals("toString") && !className.equals(Properties.TARGET_CLASS)){
                    count += currentStatement.hasAssertions() ? 1 : 0;
                }
            }

            if(currentStatement.hasAssertions()){

                Set<Assertion> assertions = currentStatement.getAssertions();

                for(Assertion assertion : assertions){
                    if(assertion instanceof InspectorAssertion){
                        Method method = ((InspectorAssertion) assertion).getInspector().getMethod();
                        String curr = method.getName();

                        if(curr.equals("toString") && !method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                            count ++;
                        }
                    }
                }
            }
        }

        return count;
    }
}
