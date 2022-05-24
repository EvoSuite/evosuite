package org.evosuite.testsmells.smells;

import org.apache.commons.lang3.tuple.Triple;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;
import org.evosuite.testcase.statements.Statement;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Definition:
 * A test case contains several assertions that check the same condition.
 *
 * Adaptation:
 * In the proposed metric, two assertions are equal if they:
 * 1 - Check the same method of the same class;
 * 2 - Correspond to the same type of assertion;
 * 3 - Have the same expected value.
 *
 * Metric:
 * Number of assertion statements of the same type that check the same method of the same class and have the
 * same expected value.
 *
 * Computation:
 * 1 - Create a smell counter and initialize the variable with the value 0
 * 2 - Create an empty LinkedHashSet: elements - triples which corresponds to the assertions
 * 3 - Let S = {S1,...,Sn} be the set of n statements in a test case and let A = {A1,...,Ak} be the set of k assertions
 *     in a statement Si
 * 4 - Iterate over S and, for each statement Si:
 * [4: Start loop]
 * 5 - Iterate over A of Si and, for each assertion Aj:
 * [5: Start loop]
 * 6 - Verify if Aj corresponds to an inspector assertion (instance of InspectorAssertion)
 * 7 (6 is True):
 *    7.1 - Get the method on which the assertion is made
 * 8 (6 is False):
 *    8.1 - Get the statement that contains the variable on which the assertion is made
 *    8.2 - If this statement corresponds to a method statement, get the respective method; otherwise, consider that
 *          the method is null
 * 9 - Get the assertion type and the expected value of Aj
 * 10 - Store the method, assertion type, and expected value in a Triple (which identifies the assertion)
 * 11 - If the LinkedHashSet contains this Triple, increment the smell counter; otherwise, add the Triple to the LinkedHashSet
 * [5: End loop]
 * [4: End loop]
 * 12 - Return the smell counter
 */
public class DuplicateAssert extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = -8140948222271634238L;

    public DuplicateAssert() {
        super("TestSmellDuplicateAssert");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;

        Set<Triple<Method, Class<? extends Assertion>, Object>> tripleSet = new LinkedHashSet<>();
        Method method;

        TestCase testCase = chromosome.getTestCase();

        for (int i = 0; i < size; i++){

            Set<Assertion> assertions = testCase.getStatement(i).getAssertions();

            for(Assertion assertion : assertions){

                if(assertion instanceof InspectorAssertion){

                    // Method on which the assertion is made
                    method = ((InspectorAssertion) assertion).getInspector().getMethod();

                } else {

                    // Statement that contains the variable on which the assertion is made
                    Statement variableStatement = testCase.getStatement(assertion.getSource().getStPosition());

                    if(variableStatement instanceof MethodStatement){
                        method = ((MethodStatement) variableStatement).getMethod().getMethod();
                    } else {
                        method = null;
                    }
                }

                Triple<Method, Class<? extends Assertion>, Object> currentAssertion = Triple.of(method, assertion.getClass(), assertion.getValue());

                if(tripleSet.contains(currentAssertion)){
                    count++;
                } else {
                    tripleSet.add(currentAssertion);
                }
            }
        }

        return count;
    }
}
