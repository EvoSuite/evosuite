package org.evosuite.testsmells.smells;

import org.apache.commons.lang3.tuple.Triple;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testsmells.AbstractTestCaseSmell;
import org.evosuite.testcase.statements.Statement;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Detection:
 * 1 - Iterate over the statements of a test case
 * 2 - Verify if the current statement is an instance of MethodStatement
 * 3 (True):
 *    3.1 - Iterate over the assertions of the current statement
 *    3.2 - Verify if the current assertion is an instance of InspectorAssertion
 *    3.3 (True):
 *       3.3.1 - Get the inspector method
 *    3.4 (False):
 *       3.4.1 - Get the method called in the respective statement
 *    3.5 - Get the assertion type and the expected value
 *    3.6 - Store the method, assertion type, and expected value in a Triple (which identifies the assertion)
 *    3.7 - Verify if a LinkedHashSet contains the Triple
 *    3.8 (True):
 *       3.8.1 - This indicates that an equal assertion has already been added to the LinkedHashSet, i.e., it is
 *               necessary to increment the smell counter
 *    3.9 (False):
 *       3.9.1 - Add the Triple to the LinkedHashSet
 * 4 (False):
 * 4.1 - Repeat the same process described in Step 3, but only get the method if the current assertion
 *       is an instance of InspectorAssertion
 * 5 - Return the smell counter
 */
public class DuplicateAssert extends AbstractTestCaseSmell {

    public DuplicateAssert() {
        super("TestSmellDuplicateAssert");
    }

    @Override
    public double computeNumberOfSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        int count = 0;

        Set<Triple<Method, Class<? extends Assertion>, Object>> tripleSet = new LinkedHashSet<>();

        Method method;
        Class<? extends Assertion> assertionType;
        Object value;

        for (int i = 0; i < size; i++){

            Statement currentStatement = chromosome.getTestCase().getStatement(i);
            Set<Assertion> assertions = currentStatement.getAssertions();

            if(currentStatement instanceof MethodStatement){

                for(Assertion assertion : assertions){

                    // If the current assertion is an instance of InspectorAssertion, then it is
                    // necessary to get the inspector method
                    if(assertion instanceof InspectorAssertion){
                        method = ((InspectorAssertion) assertion).getInspector().getMethod();
                    } else {
                        method = ((MethodStatement) currentStatement).getMethod().getMethod();

                        if(assertion.getStatement() instanceof MethodStatement){
                            method = ((MethodStatement) currentStatement).getMethod().getMethod();
                        }
                    }

                    assertionType = assertion.getClass();
                    value = assertion.getValue();

                    Triple<Method, Class<? extends Assertion>, Object> currentAssertion = Triple.of(method, assertionType, value);

                    if(tripleSet.contains(currentAssertion)){
                        count++;
                    } else {
                        tripleSet.add(currentAssertion);
                    }
                }
            } else {

                for(Assertion assertion : assertions){

                    if(assertion instanceof InspectorAssertion){

                        method = ((InspectorAssertion) assertion).getInspector().getMethod();
                        assertionType = assertion.getClass();
                        value = assertion.getValue();

                        Triple<Method, Class<? extends Assertion>, Object> currentAssertion = Triple.of(method, assertionType, value);

                        if(tripleSet.contains(currentAssertion)){
                            count++;
                        } else {
                            tripleSet.add(currentAssertion);
                        }
                    }
                }
            }
        }

        return FitnessFunction.normalize(count);
    }
}
