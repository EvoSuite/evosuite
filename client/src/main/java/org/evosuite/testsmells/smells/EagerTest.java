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
 * 1 - Iterate over the statements of a test case
 * [1: Start loop]
 * 2 - Verify if the current statement is an instance of MethodStatement
 * 3 (2 is True):
 *    3.1 - Get the method called in the respective statement
 *    3.2 - If the class that declares this method is the same as the class under test: add method to a LinkedHashSet
 *          of methods (a LinkedHashSet does not store duplicate elements)
 * 4 - Verify if the current statement has assertions
 * 5 (4 is True):
 *    5.1 - Iterate over the assertions of the current statement
 *    [5.1: Start loop]
 *    5.2 - Verify if the current assertion is an instance of InspectorAssertion
 *    5.3 (5.2 is True):
 *       5.3.1 - Get the method on which the assertion is made
 *       5.3.2 - If the class that declares this method is the same as the class under test: add method to LinkedHashSet
 *               of methods
 *    [5.1: End loop]
 * [1: End loop]
 * 6 - Return the number of elements in the LinkedHashSet of methods
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
