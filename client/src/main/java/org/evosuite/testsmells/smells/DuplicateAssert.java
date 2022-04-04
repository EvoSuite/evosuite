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
 * A test case tests the same condition several times.
 *
 * Adaptation:
 * In the proposed metric, two assertions are equal if they:
 * 1 - Check the same method of the same class;
 * 2 - Correspond to the same type of assertion;
 * 3 - Have the same expected value.
 *
 * Metric:
 * Number of assertion statements of the same type that have the same parameters.
 *
 * Detection:
 * 1 - Create an empty LinkedHashSet: elements - triples which corresponds to the assertions
 * 2 - Iterate over the statements of a test case
 * [2: Start loop]
 * 3 - Iterate over the assertions of the current statement
 * [3: Start loop]
 * 4 - Verify if the current assertion is an instance of InspectorAssertion
 * 5 (4 is True):
 *    5.1 - Get the method on which the assertion is made
 * 6 (4 is False):
 *    6.1 - Get the statement that contains the variable on which the assertion is made
 *    6.2 - Verify if this statement is an instance of MethodStatement
 *    6.3 (6.2 is True):
 *       6.3.1 - Get the method called in the respective statement
 *    6.4 (6.2 is False):
 *       6.4.1 - Consider that the method is null
 * 7 - Get the assertion type and the expected value
 * 8 - Store the method, assertion type, and expected value in a Triple (which identifies the assertion)
 * 9 - Verify if the LinkedHashSet contains this Triple
 * 10 (9 is True):
 *    10.1 - This indicates that an equal assertion has already been added to the LinkedHashSet: increment the smell counter
 * 11 (9 is False):
 *    11.1 - Add the Triple to the LinkedHashSet
 * [3: End loop]
 * [2: End loop]
 * 12 - Return the smell counter
 */
public class DuplicateAssert extends AbstractNormalizedTestCaseSmell {

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
