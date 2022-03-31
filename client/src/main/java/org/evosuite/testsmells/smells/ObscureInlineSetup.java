package org.evosuite.testsmells.smells;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestCaseSmell;

import java.lang.reflect.Method;

/**
 * Definition:
 * A test case contains too much setup functionality.
 *
 * Metric:
 * Count the number of declared variables in a test case.
 *
 * Computation:
 * 1 - Iterate over the statements of a test case
 * 2 - Verify if the current statement is an instance PrimitiveStatement or ConstructorStatement
 * 3 (2 is True):
 *    3.1 - Increment the smell counter
 * 4 (2 is False):
 *    4.1 - Verify if the current statement is an instance of MethodStatement
 *    4.2 (4.1 is True):
 *       4.2.1 - If the type of the method is not "void": increment the smell counter
 * 5 - Return the smell counter
 */
public class ObscureInlineSetup extends AbstractTestCaseSmell {

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
            if(currentStatement instanceof PrimitiveStatement || currentStatement instanceof ConstructorStatement){
                count++;
            } else if (currentStatement instanceof MethodStatement) {
                Method method = ((MethodStatement) currentStatement).getMethod().getMethod();
                String typeName = method.getGenericReturnType().getTypeName();
                count += typeName.equals("void") ? 0 : 1;
            }
        }

        return count;
    }
}
