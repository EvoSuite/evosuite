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
 * Total number of assertions that check methods that are not declared in the class under test.
 *
 * Computation:
 * 1 - Create a smell counter and initialize the variable with the value 0
 * 2 - Let S = {S1,...,Sn} be the set of n statements in a test case and let A = {A1,...,Ak} be the set of k assertions
 *     in a statement Si
 * 3 - Iterate over S and, for each statement Si:
 * [3: Start loop]
 * 4 - Iterate over A of Si and, for each assertion Aj:
 * [4: Start loop]
 * 5 - Verify if Aj corresponds to an inspector assertion (instance of InspectorAssertion)
 * 6 (5 is True):
 *    6.1 - If the class that declares the method on which the assertion is made is different from the class under
 *          test: increment the smell counter
 * 7 (5 is False):
 *    7.1 - Verify if the statement that contains the variable on which the assertion is made corresponds to a method statement
 *    7.2 (7.1 is True):
 *       7.2.1 - If the class that declares this method is different from the class under test: increment the smell counter
 * [4: End loop]
 * [3: End loop]
 * 8 - Return the smell counter
 */
public class UnrelatedAssertions extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = -6572853267089998309L;

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
