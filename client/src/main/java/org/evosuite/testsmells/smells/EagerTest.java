package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Definition:
 * A test case checks multiple methods of the class under test (i.e., the test case verifies too much functionality).
 *
 * Metric:
 * Total number of different methods of the class under test that are checked by a test case.
 *
 * Computation:
 * 1 - Create an empty LinkedHashSet: elements - methods
 * 2 - Let S = {S1,...,Sn} be the set of n statements in a test case
 * 3 - Iterate over S and, for each statement Si:
 * [3: Start loop]
 * 4 - Verify if Si corresponds to a method statement (instance of MethodStatement)
 * 5 (4 is True):
 *    5.1 - Get the method called in Si
 *    5.2 - If the class that declares this method is the same as the class under test: add the method to the LinkedHashSet
 * 6 - Verify if Si has assertions
 * 7 (6 is True):
 *    7.1 - Let A = {A1,...,Ak} be the set of k assertions in Si
 *    7.2 - Iterate over A of Si and, for each assertion Aj:
 *    [7.2: Start loop]
 *    7.3 - Verify if Aj corresponds to an inspector assertion (instance of InspectorAssertion)
 *    7.4 (7.3 is True)
 *       7.4.1 - If the class that declares the method on which the assertion is made is the same as the class
 *               under test: add the method to LinkedHashSet
 *    [7.2: End loop]
 * [3: End loop]
 * 8 - Return the number of elements in the LinkedHashSet of methods
 */
public class EagerTest extends AbstractNormalizedTestCaseSmell {

    private static final long serialVersionUID = 8891588782895208412L;

    public EagerTest() {
        super("TestSmellEagerTest");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();

        Statement currentStatement;
        Method method;

        Set<Method> setOfMethods = new LinkedHashSet<>();

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof MethodStatement){
                method = ((MethodStatement) currentStatement).getMethod().getMethod();

                if(method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                    setOfMethods.add(method);
                }
            }

            if(currentStatement.hasAssertions()){

                Set<Assertion> assertions = currentStatement.getAssertions();

                for(Assertion assertion : assertions){
                    if(assertion instanceof InspectorAssertion){
                        method = ((InspectorAssertion) assertion).getInspector().getMethod();
                        if(method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                            setOfMethods.add(method);
                        }
                    }
                }
            }
        }

        return setOfMethods.size();
    }

    /*
    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();

        Statement currentStatement;
        Method method;

        Set<Method> setOfMethods = new LinkedHashSet<>();

        LoggingUtils.getEvoLogger().info("--------------- Start ---------------");

        LoggingUtils.getEvoLogger().info("\n");

        TestCodeVisitor visitor = new TestCodeVisitor();

        TestCase testCase = chromosome.getTestCase();
        visitor.visitTestCase(testCase);

        for (int i = 0; i < size; i++){
            visitor.visitStatement(testCase.getStatement(i));
        }

        LoggingUtils.getEvoLogger().info(visitor.getCode());

        LoggingUtils.getEvoLogger().info("\n");

        LoggingUtils.getEvoLogger().info("Class Under Test = " + Properties.TARGET_CLASS);
        LoggingUtils.getEvoLogger().info("Size = " + size);

        LoggingUtils.getEvoLogger().info("\n");

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof MethodStatement){
                method = ((MethodStatement) currentStatement).getMethod().getMethod();

                if(method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                    if(setOfMethods.add(method)){
                        LoggingUtils.getEvoLogger().info("Eager Test Smelly Statement = " + i + " / Method: " + method + " / Class: " + method.getDeclaringClass().getCanonicalName());
                    }
                } else {
                    LoggingUtils.getEvoLogger().info("Indirect Testing Smelly Statement = " + i + " / Method: " + method + " / Class: " + method.getDeclaringClass().getCanonicalName());
                }
            }

            if(currentStatement.hasAssertions()){

                Set<Assertion> assertions = currentStatement.getAssertions();

                for(Assertion assertion : assertions){
                    if(assertion instanceof InspectorAssertion){
                        method = ((InspectorAssertion) assertion).getInspector().getMethod();
                        if(method.getDeclaringClass().getCanonicalName().equals(Properties.TARGET_CLASS)){
                            if(setOfMethods.add(method)){
                                LoggingUtils.getEvoLogger().info("Eager Test Smelly Statement = " + i + " / Method: " + method + " / Class: " + method.getDeclaringClass().getCanonicalName());
                            }
                        } else {
                            LoggingUtils.getEvoLogger().info("Indirect Testing Smelly Statement = " + i + " / Method: " + method + " / Class: " + method.getDeclaringClass().getCanonicalName());
                        }
                    }
                }
            }
        }

        return setOfMethods.size();
    }
     */
}
