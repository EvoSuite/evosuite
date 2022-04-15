package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass1;
import org.evosuite.Properties;
import org.evosuite.assertion.NullAssertion;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.smells.UnknownTest;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class UnknownTestSmellTest {

    AbstractTestCaseSmell unknownTest;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass1.class.getCanonicalName();
        this.unknownTest = new UnknownTest();
    }

    @Test
    public void testAssertionlessTestCase() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        double computedMetric = this.unknownTest.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 1.0;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testOneAssertion() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase1();
        testCase.setTestCase(test0);

        double computedMetric = this.unknownTest.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testMultipleAssertions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase2();
        testCase.setTestCase(test0);

        double computedMetric = this.unknownTest.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testEmptyTestCase() {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createEmptyTestCase();
        testCase.setTestCase(test0);

        double computedMetric = this.unknownTest.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 1;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testFullTestSuite() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase0();
        DefaultTestCase test1 = createTestCase1();
        DefaultTestCase test2 = createTestCase2();
        DefaultTestCase test3 = createEmptyTestCase();
        suite.addTest(test0);
        suite.addTest(test1);
        suite.addTest(test2);
        suite.addTest(test3);

        double computedMetric = this.unknownTest.computeTestSmellMetric(suite);
        double expectedComputedMetric = 0.5;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    private DefaultTestCase createTestCase0() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        Method getNameMethod0 = TestSmellsTestingClass1.class.getMethod("getName");
        builder.appendMethod(constructorStatement0, getNameMethod0);

        return builder.getDefaultTestCase();
    }

    private DefaultTestCase createTestCase1() throws NoSuchMethodException {

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

    private DefaultTestCase createTestCase2() throws NoSuchMethodException {

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
        VariableReference methodStatement2 = builder.appendMethod(constructorStatement0, getNumberMethod0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement;

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("Bob");
        currentStatement = testCase.getStatement(2);
        currentStatement.addAssertion(primitiveAssertion0);

        NullAssertion nullAssertion0 = new NullAssertion();
        nullAssertion0.setSource(methodStatement0);
        nullAssertion0.setValue(false);
        currentStatement = testCase.getStatement(2);
        currentStatement.addAssertion(nullAssertion0);

        PrimitiveAssertion primitiveAssertion1 = new PrimitiveAssertion();
        primitiveAssertion1.setSource(methodStatement2);
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