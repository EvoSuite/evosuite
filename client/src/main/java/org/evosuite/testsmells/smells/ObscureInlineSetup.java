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
 * 1 - Create a smell counter and initialize the variable with the value 0
 * 2 - Let S = {S1,...,Sn} be the set of n statements in a test case
 * 3 - Iterate over S and, for each statement Si:
 * [3: Start loop]
 * 4 - Verify if Si corresponds to a method statement (instance of MethodStatement)
 * 5 (4 is True):
 *    5.1 - If the class that declares this method is different from the class under test and the type of the method is
 *          not equal to "void": increment the smell counter
 * 6 (4 is False):
 *    6.1 - Increment the smell counter if Si is an instance of one of the following types of statements: (1) PrimitiveStatement;
 *          (2) ConstructorStatement; (3) ArrayStatement; (4) FieldStatement; (5) FunctionalMockStatement; (6) AssignmentStatement
 * [3: End loop]
 * 7 - Return the smell counter
 */
public class ObscureInlineSetup extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = -7409051587578491230L;

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
                    currentStatement instanceof ArrayStatement || currentStatement instanceof FieldStatement ||
                    currentStatement instanceof FunctionalMockStatement || currentStatement instanceof AssignmentStatement){
                count++;
            }
        }

        return count;
    }
}
