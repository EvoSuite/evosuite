package org.evosuite.testsmells.smells;

import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractTestSmell;
import org.evosuite.testsuite.TestSuiteChromosome;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Definition:
 * Multiple test cases check the same production method.
 *
 * Metric:
 * Number of times each production method is called by more than one test case.
 *
 * Computation:
 * 1 - Create an empty LinkedHashMap: key - method; value - test case that initially called the method
 * 2 - Iterate over the test cases of a test suite
 * [2: Start loop]
 * 3 - Iterate over the statements of the current test case
 * [3: Start loop]
 * 4 - Verify if the current statement is an instance of MethodStatement
 * 5 (4 is True):
 *    5.1 - Get the method called in the respective statement
 *    5.2 - Verify if another test case already calls this method: use the LinkedHashMap to check whether the test case
 *          to which the specified method is mapped is equal to the current test case
 *    5.3 (5.2 is True):
 *       5.3.1 - Increment the smell counter
 *    5.4 (5.2 is False):
 *       5.4.1 - Add element to the LinkedHashMap
 * 6 - Verify if the current statement has assertions
 * 7 (6 is True):
 *    7.1 - Iterate over the assertions of the current statement
 *    [7.1: Start loop]
 *    7.2 - Verify if the current assertion is an instance of InspectorAssertion
 *    7.3 (7.2 is True):
 *       7.3.1 - Get the method on which the assertion is made
 *       7.3.2 - Perform the steps described in 5.2 - 5.4
 *    [7.1: End loop]
 * [3: End loop]
 * [2: End loop]
 * 8 - Return the normalized value for the smell counter
 */
public class LazyTest extends AbstractTestSmell {

    public LazyTest() {
        super("TestSmellLazyTest");
    }

    @Override
    public double computeTestSmellMetric(TestSuiteChromosome chromosome) {
        int count = 0;
        Statement currentStatement;

        LinkedHashMap<Method, TestChromosome> methodsCalledByTestCases = new LinkedHashMap<>();

        for(TestChromosome testCase : chromosome.getTestChromosomes()){
            int size = testCase.size();

            for (int i = 0; i < size; i++){
                currentStatement = testCase.getTestCase().getStatement(i);

                if(currentStatement instanceof MethodStatement){
                    Method method = ((MethodStatement) currentStatement).getMethod().getMethod();

                    //Verify if a different test case tests the same method
                    if(methodsCalledByTestCases.containsKey(method) && !methodsCalledByTestCases.get(method).equals(testCase)){
                        count++;
                    }else{
                        methodsCalledByTestCases.put(method, testCase);
                    }
                }

                if(currentStatement.hasAssertions()){

                    Set<Assertion> assertions = currentStatement.getAssertions();

                    for(Assertion assertion : assertions) {
                        if (assertion instanceof InspectorAssertion) {
                            Method method = ((InspectorAssertion) assertion).getInspector().getMethod();

                            if (methodsCalledByTestCases.containsKey(method) && !methodsCalledByTestCases.get(method).equals(testCase)) {
                                count++;
                            } else {
                                methodsCalledByTestCases.put(method, testCase);
                            }
                        }
                    }
                }
            }
        }

        return FitnessFunction.normalize(count);
    }
}
