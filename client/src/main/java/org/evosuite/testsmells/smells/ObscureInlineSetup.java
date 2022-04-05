package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.*;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

import java.lang.reflect.Method;

/**
 * Definition:
 * A test case contains too much setup functionality.
 *
 * Adaptation:
 * This metric does not consider the variables that store values returned from methods of the class under test.
 *
 * Metric:
 * Number of declared variables in a test case.
 *
 * Computation:
 * 1 - Iterate over the statements of a test case
 * [1: Start loop]
 * 2 - Verify if the current statement is an instance of MethodStatement
 * 3 (2 is True):
 *    3.1 - Verify if the class that declares this method is different from the class under test
 *    3.2 (3.1 is True):
 *       3.2.1 - If the type of the method is not "void": increment the smell counter
 * 4 (2 is False):
 *    4.1 - Verify if the current statement is an instance of one of the following types of statements: (1) PrimitiveStatement;
 *          (2) ConstructorStatement; (3) ArrayStatement; (4) FieldStatement
 *    4.2 (4.1 is True):
 *       4.2.1 - Increment the smell counter
 * [1: End loop]
 * 5 - Return the smell counter
 */
public class ObscureInlineSetup extends AbstractNormalizedTestCaseSmell {

    public ObscureInlineSetup() {
        super("TestSmellObscureInlineSetup");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);
            if (currentStatement instanceof MethodStatement) {
                Method method = ((MethodStatement) currentStatement).getMethod().getMethod();

                if(!method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                    String typeName = method.getGenericReturnType().getTypeName();
                    count += typeName.equals("void") ? 0 : 1;
                }
            } else if(currentStatement instanceof PrimitiveStatement || currentStatement instanceof ConstructorStatement ||
                    currentStatement instanceof ArrayStatement || currentStatement instanceof FieldStatement){
                count++;
            }
        }

        return count;
    }
}
