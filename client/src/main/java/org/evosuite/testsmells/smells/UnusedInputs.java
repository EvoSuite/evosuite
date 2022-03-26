package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testcase.statements.Statement;

import java.lang.reflect.Method;

/**
 * Definition:
 * Input that is controlled by a test case is not checked by an assertion.
 *
 * Adaptation:
 * Every statement which calls a method of the class under test that returns a value should necessarily have at
 * least one assertion. Otherwise, the test is considered smelly.
 *
 * Computation:
 * 1 - Iterate over the statements of a test case
 * 2 - Verify if the current statement is an instance of MethodStatement
 * 3 (2 is True):
 *    3.1 - Get the method called in the respective statement
 *    3.2 - Get the return type of the method
 *    3.3 - Verify whether: (1) the class that declares the method is the same as the class under test; (2) the type of
 *          the method is not equal to "void" - if this is true, then it indicates that the statement should
 *          necessarily have assertions
 *    3.4 (3.3 is True):
 *       3.4.1 - If the current statement does not have assertions, increment the smell counter
 * 4 - Return the smell counter
 */
public class UnusedInputs extends AbstractTestCaseSmell {

    public UnusedInputs() {
        super("TestSmellUnusedInputs");
    }

    @Override
    public int computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof MethodStatement){

                Method method = ((MethodStatement) currentStatement).getMethod().getMethod();
                String typeName = method.getGenericReturnType().getTypeName();

                if(method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS) && !typeName.equals("void")){
                    count += currentStatement.hasAssertions() ? 0 : 1;
                }
            }
        }
        return count;
    }
}
