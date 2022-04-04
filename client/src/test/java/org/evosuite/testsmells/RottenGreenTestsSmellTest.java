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
import org.evosuite.testsmells.smells.RottenGreenTests;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class RottenGreenTestsSmellTest {

    AbstractNormalizedTestCaseSmell rottenGreenTests;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass1.class.getCanonicalName();
        this.rottenGreenTests = new RottenGreenTests();
    }

    @Test
    public void testNoExceptions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        ExecutionResult result = TestCaseExecutor.runTest(testCase.getTestCase());
        testCase.setLastExecutionResult(result);

        long smellCount = this.rottenGreenTests.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 0;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.rottenGreenTests.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testFirstExceptionInTheLastStatement() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase1();
        testCase.setTestCase(test0);

        ExecutionResult result = TestCaseExecutor.runTest(testCase.getTestCase());
        testCase.setLastExecutionResult(result);

        long smellCount = this.rottenGreenTests.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 0;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.rottenGreenTests.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testFirstExceptionNotInTheLastStatement() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase2();
        testCase.setTestCase(test0);

        ExecutionResult result = TestCaseExecutor.runTest(testCase.getTestCase());
        testCase.setLastExecutionResult(result);

        long smellCount = this.rottenGreenTests.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 1;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.rottenGreenTests.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0.5;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testMultipleExceptions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase3();
        testCase.setTestCase(test0);

        ExecutionResult result = TestCaseExecutor.runTest(testCase.getTestCase());
        testCase.setLastExecutionResult(result);

        long smellCount = this.rottenGreenTests.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 3;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.rottenGreenTests.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0.75;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testEmptyTestCase() {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createEmptyTestCase();
        testCase.setTestCase(test0);

        ExecutionResult result = TestCaseExecutor.runTest(testCase.getTestCase());
        testCase.setLastExecutionResult(result);

        long smellCount = this.rottenGreenTests.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 0;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.rottenGreenTests.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testFullTestSuite() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase0();
        DefaultTestCase test1 = createTestCase1();
        DefaultTestCase test2 = createTestCase2();
        DefaultTestCase test3 = createTestCase3();
        DefaultTestCase test4 = createEmptyTestCase();
        suite.addTest(test0);
        suite.addTest(test1);
        suite.addTest(test2);
        suite.addTest(test3);
        suite.addTest(test4);

        for (TestChromosome test : suite.getTestChromosomes()) {
            ExecutionResult result;
            result = TestCaseExecutor.runTest(test.getTestCase());
            test.setLastExecutionResult(result);
        }

        double computedMetric = this.rottenGreenTests.computeTestSmellMetric(suite);
        double expectedComputedMetric = 0.8;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    private DefaultTestCase createTestCase0() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        Method getNameMethod0 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNameMethod0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("Bob");
        Statement currentStatement = testCase.getStatement(2);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }

    private DefaultTestCase createTestCase1() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        VariableReference intStatement0 = builder.appendIntPrimitive(5);

        Method setNumberMethod0 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod0, intStatement0);

        VariableReference intStatement1 = builder.appendIntPrimitive(0);

        Method divideNumber0 = TestSmellsTestingClass1.class.getMethod("divideNumber", int.class);
        builder.appendMethod(constructorStatement0, divideNumber0, intStatement1);

        return builder.getDefaultTestCase();
    }

    private DefaultTestCase createTestCase2() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        VariableReference intStatement0 = builder.appendIntPrimitive(5);

        Method setNumberMethod0 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod0, intStatement0);

        VariableReference intStatement1 = builder.appendIntPrimitive(0);

        Method divideNumber0 = TestSmellsTestingClass1.class.getMethod("divideNumber", int.class);
        builder.appendMethod(constructorStatement0, divideNumber0, intStatement1);

        Method getNameMethod0 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNameMethod0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("Bob");
        Statement currentStatement = testCase.getStatement(6);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }

    private DefaultTestCase createTestCase3() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        VariableReference intStatement0 = builder.appendIntPrimitive(5);

        Method setNumberMethod0 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod0, intStatement0);

        VariableReference intStatement1 = builder.appendIntPrimitive(0);

        Method divideNumber0 = TestSmellsTestingClass1.class.getMethod("divideNumber", int.class);
        builder.appendMethod(constructorStatement0, divideNumber0, intStatement1);

        Method divideNumber1 = TestSmellsTestingClass1.class.getMethod("divideNumber", int.class);
        builder.appendMethod(constructorStatement0, divideNumber1, intStatement1);

        Method divideNumber2 = TestSmellsTestingClass1.class.getMethod("divideNumber", int.class);
        builder.appendMethod(constructorStatement0, divideNumber2, intStatement1);

        Method getNameMethod0 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNameMethod0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("Bob");
        Statement currentStatement = testCase.getStatement(8);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }

    private DefaultTestCase createEmptyTestCase() {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();
        return builder.getDefaultTestCase();
    }
}
