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
 * 1 - Create a smell counter and initialize the variable with the value 0
 * 2 - Create an empty LinkedHashMap: key - method; value - test case that initially called the respective method
 * 3 - Let T = {T1,...,Tn} be the set of n test cases in a test suite
 * 4 - Iterate over T and, for each test case Ti:
 * [4: Start loop]
 * 5 - Let S = {S1,...,Sk} be the set of k statements in Ti
 * 6 - Iterate over S and, for each statement Sj:
 * [6: Start loop]
 * 7 - Verify if Sj corresponds to a method statement (instance of MethodStatement)
 * 8 (7 is True):
 *    8.1 - If a different test case already calls the method called in Si, increment the smell counter; otherwise,
 *          insert this method and the test case it is mapping into the LinkedHashMap
 * 9 - Verify if Si has assertions
 * 10 (9 is True):
 *    10.1 - Let A = {A1,...,Am} be the set of m assertions in Sj
 *    10.2 - Iterate over A of Sj and, for each assertion Ar:
 *    [10.2: Start loop]
 *    10.3 - Verify if Ar corresponds to an inspector assertion (instance of InspectorAssertion)
 *    10.4 (10.3 is True):
 *       10.4.1 - If a different test case already calls the method on which Ar is made, increment the smell counter; otherwise,
 *                insert this method and the test case it is mapping into the LinkedHashMap
 *    [10.2: End loop]
 * [6: End loop]
 * [4: End loop]
 * 11 - Return the normalized value for the smell counter
 */
public class LazyTest extends AbstractTestSmell {

    private static final long serialVersionUID = -7422791772724543972L;

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

    /*
    @Override
    public double computeTestSmellMetric(TestSuiteChromosome chromosome) {
        int count = 0;
        Statement currentStatement;

        LinkedHashMap<Method, TestChromosome> methodsCalledByTestCases = new LinkedHashMap<>();
        LinkedHashMap<Method, Integer> methodsCalledByTestCasesPos = new LinkedHashMap<>();

        int number = 0;

        for(TestChromosome testCase : chromosome.getTestChromosomes()){
            int size = testCase.size();

            LoggingUtils.getEvoLogger().info("--------------- Start Suite - " + number + " ---------------");

            LoggingUtils.getEvoLogger().info("\n");

            TestCodeVisitor visitor = new TestCodeVisitor();

            TestCase tc = testCase.getTestCase();
            visitor.visitTestCase(tc);

            for (int j = 0; j < size; j++){
                visitor.visitStatement(tc.getStatement(j));
            }

            LoggingUtils.getEvoLogger().info(visitor.getCode());

            LoggingUtils.getEvoLogger().info("\n");

            LoggingUtils.getEvoLogger().info("Size = " + size);

            LoggingUtils.getEvoLogger().info("\n");

            for (int i = 0; i < size; i++){
                currentStatement = testCase.getTestCase().getStatement(i);

                if(currentStatement instanceof MethodStatement){
                    Method method = ((MethodStatement) currentStatement).getMethod().getMethod();

                    //Verify if a different test case tests the same method
                    if(methodsCalledByTestCases.containsKey(method) && !methodsCalledByTestCases.get(method).equals(testCase)){
                        LoggingUtils.getEvoLogger().info("Smelly Statement! Method: " + method + " | Tested By Suite: " + methodsCalledByTestCasesPos.get(method));
                        count++;
                    }else{
                        methodsCalledByTestCases.put(method, testCase);
                        methodsCalledByTestCasesPos.put(method, number);
                        LoggingUtils.getEvoLogger().info("New! Method: " + method + " | Tested By Suite: " + methodsCalledByTestCasesPos.get(method));
                    }
                }

                if(currentStatement.hasAssertions()){

                    Set<Assertion> assertions = currentStatement.getAssertions();

                    for(Assertion assertion : assertions) {
                        if (assertion instanceof InspectorAssertion) {
                            Method method = ((InspectorAssertion) assertion).getInspector().getMethod();

                            if (methodsCalledByTestCases.containsKey(method) && !methodsCalledByTestCases.get(method).equals(testCase)) {
                                LoggingUtils.getEvoLogger().info("Smelly Statement! Method: " + method + " | Tested By Suite: " + methodsCalledByTestCasesPos.get(method));
                                count++;
                            } else {
                                methodsCalledByTestCases.put(method, testCase);
                                methodsCalledByTestCasesPos.put(method, number);
                                LoggingUtils.getEvoLogger().info("New! Method: " + method + " | Tested By Suite: " + methodsCalledByTestCasesPos.get(method));
                            }
                        }
                    }
                }
            }
            LoggingUtils.getEvoLogger().info("--------------- End Suite - " + number + " ---------------");
            number++;
        }

        return FitnessFunction.normalize(count);
    }

     */
}
