package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;
import org.evosuite.testcase.statements.Statement;

import java.lang.reflect.Method;

/**
 * Definition:
 * Assertions do not check input controlled by a test case (the test is checking too little).
 *
 * Adaptation:
 * Every statement which calls a method of the class under test that returns a value should necessarily have at
 * least one assertion. Otherwise, the test is considered smelly.
 *
 * Metric:
 * Number of statements that call methods of the class under test but that do not have assertions.
 *
 * Computation:
 * 1 - Create a smell counter and initialize the variable with the value 0
 * 2 - Let S = {S1,...,Sn} be the set of n statements in a test case
 * 3 - Iterate over S and, for each statement Si:
 * [3: Start loop]
 * 4 - Verify if: (1) Si corresponds to a statement that calls a method of the class under test and (2) the type of
 *     the method is not equal to "void" - if true, this indicates that the statement should necessarily have assertions
 * 5 (4 is True):
 *    5.1 - If the current statement does not have assertions: increment the smell counter
 * [3: End loop]
 * 6 - Return the smell counter
 */
public class UnusedInputs extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = -6258624735851248001L;

    public UnusedInputs() {
        super("TestSmellUnusedInputs");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;

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
