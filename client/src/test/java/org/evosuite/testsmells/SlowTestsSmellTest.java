package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass1;
import org.evosuite.Properties;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.smells.SlowTests;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SlowTestsSmellTest {

    AbstractTestCaseSmell slowTests;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass1.class.getCanonicalName();
        this.slowTests = new SlowTests();
    }

    @Test
    public void testLastExecutionResultNull() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        double computedMetric = this.slowTests.computeTestSmellMetric(testCase);
        double expectedComputedMetric = Double.NaN;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testLastExecutionResultNotNull() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        ExecutionResult result = TestCaseExecutor.runTest(testCase.getTestCase());
        testCase.setLastExecutionResult(result);

        double computedMetric = this.slowTests.computeTestSmellMetric(testCase);
        assertTrue(computedMetric > 0);
    }

    @Test
    public void testEmptyTestCaseLastExecutionResultNull() {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createEmptyTestCase();
        testCase.setTestCase(test0);

        double computedMetric = this.slowTests.computeTestSmellMetric(testCase);
        double expectedComputedMetric = Double.NaN;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testEmptyTestCaseLastExecutionResultNotNull() {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createEmptyTestCase();
        testCase.setTestCase(test0);

        ExecutionResult result = TestCaseExecutor.runTest(testCase.getTestCase());
        testCase.setLastExecutionResult(result);

        double computedMetric = this.slowTests.computeTestSmellMetric(testCase);
        assertTrue(computedMetric >= 0);
    }

    @Test
    public void testFullTestSuiteNotExecuted() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase0();
        DefaultTestCase test1 = createEmptyTestCase();
        suite.addTest(test0);
        suite.addTest(test1);

        double computedMetric = this.slowTests.computeTestSmellMetric(suite);
        double expectedComputedMetric = Double.NaN;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testFullTestSuiteExecuted() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase0();
        DefaultTestCase test1 = createEmptyTestCase();
        suite.addTest(test0);
        suite.addTest(test1);

        for (TestChromosome test : suite.getTestChromosomes()) {
            ExecutionResult result;
            result = TestCaseExecutor.runTest(test.getTestCase());
            test.setLastExecutionResult(result);
        }

        double computedMetric = this.slowTests.computeTestSmellMetric(suite);
        assertTrue(computedMetric > 0);
    }

    private DefaultTestCase createTestCase0() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        Method getNameMethod0 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNameMethod0);

        VariableReference intStatement0 = builder.appendIntPrimitive(5);

        Method setNumberMethod0 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod0, intStatement0);

        Method getNumberMethod0 = TestSmellsTestingClass1.class.getMethod("getNumber");
        VariableReference methodStatement1 = builder.appendMethod(constructorStatement0, getNumberMethod0);

        Method wasteTimeMethod0 = TestSmellsTestingClass1.class.getMethod("wasteTime");
        builder.appendMethod(constructorStatement0, wasteTimeMethod0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("Bob");
        Statement currentStatement = testCase.getStatement(2);
        currentStatement.addAssertion(primitiveAssertion0);

        PrimitiveAssertion primitiveAssertion1 = new PrimitiveAssertion();
        primitiveAssertion1.setSource(methodStatement1);
        primitiveAssertion1.setValue(5);
        currentStatement = testCase.getStatement(5);
        currentStatement.addAssertion(primitiveAssertion1);

        return testCase;
    }

    private DefaultTestCase createEmptyTestCase() {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();
        return builder.getDefaultTestCase();
    }
}
