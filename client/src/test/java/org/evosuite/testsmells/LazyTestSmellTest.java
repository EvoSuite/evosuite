package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsSimpleUser;
import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass1;
import org.evosuite.Properties;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.smells.LazyTest;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class LazyTestSmellTest {

    AbstractTestSmell lazyTest;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass1.class.getCanonicalName();
        this.lazyTest = new LazyTest();
    }

    @Test
    public void testEachTestCaseCallsADifferentMethod() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase0();
        DefaultTestCase test1 = createTestCase1();
        suite.addTest(test0);
        suite.addTest(test1);

        double computedMetric = this.lazyTest.computeTestSmellMetric(suite);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.00001);
    }

    @Test
    public void testAllTestCasesCallTheSameMethod() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase0();
        DefaultTestCase test1 = createTestCase0();
        DefaultTestCase test2 = createTestCase0();
        suite.addTest(test0);
        suite.addTest(test1);
        suite.addTest(test2);

        double computedMetric = this.lazyTest.computeTestSmellMetric(suite);
        double expectedComputedMetric = 2.0 / (1.0 + 2.0);
        assertEquals(expectedComputedMetric, computedMetric, 0.00001);
    }

    @Test
    public void testOneTestCaseCallsTheSameMethodsAsTwoOtherTestCases() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase0();
        DefaultTestCase test1 = createTestCase1();
        DefaultTestCase test2 = createTestCase2();
        suite.addTest(test0);
        suite.addTest(test1);
        suite.addTest(test2);

        double computedMetric = this.lazyTest.computeTestSmellMetric(suite);
        double expectedComputedMetric = 0.75;
        assertEquals(expectedComputedMetric, computedMetric, 0.00001);
    }

    @Test
    public void testTwoTestCasesCallTheSameMethodInInspectorAssertion() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase1();
        DefaultTestCase test1 = createTestCase3();
        suite.addTest(test0);
        suite.addTest(test1);

        double computedMetric = this.lazyTest.computeTestSmellMetric(suite);
        double expectedComputedMetric = 2.0 / (1.0 + 2.0);
        assertEquals(expectedComputedMetric, computedMetric, 0.00001);
    }

    @Test
    public void testAllTestCasesCallDifferentMethodsInInspectorAssertion() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase3();
        DefaultTestCase test1 = createTestCase4();
        suite.addTest(test0);
        suite.addTest(test1);

        double computedMetric = this.lazyTest.computeTestSmellMetric(suite);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.00001);
    }

    @Test
    public void testOneTestCaseTestsTheSameMethodsMultipleTimesDifferent() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase5();
        DefaultTestCase test1 = createTestCase0();
        suite.addTest(test0);
        suite.addTest(test1);

        double computedMetric = this.lazyTest.computeTestSmellMetric(suite);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.00001);
    }

    @Test
    public void testOneTestCaseTestsTheSameMethodsMultipleTimesRepeated() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase5();
        DefaultTestCase test1 = createTestCase5();
        suite.addTest(test0);
        suite.addTest(test1);

        double computedMetric = this.lazyTest.computeTestSmellMetric(suite);
        double expectedComputedMetric = 0.8;
        assertEquals(expectedComputedMetric, computedMetric, 0.00001);
    }

    @Test
    public void testOneTestCaseIsEmpty() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase1();
        DefaultTestCase test1 = createEmptyTestCase();
        suite.addTest(test0);
        suite.addTest(test1);

        double computedMetric = this.lazyTest.computeTestSmellMetric(suite);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.00001);
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

        Method getNumberMethod0 = TestSmellsTestingClass1.class.getMethod("getNumber");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNumberMethod0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(5);
        Statement currentStatement = testCase.getStatement(4);
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
        VariableReference methodStatement1 = builder.appendMethod(constructorStatement0, getNumberMethod0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement;

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("Bob");
        currentStatement = testCase.getStatement(2);
        currentStatement.addAssertion(primitiveAssertion0);

        PrimitiveAssertion primitiveAssertion1 = new PrimitiveAssertion();
        primitiveAssertion1.setSource(methodStatement1);
        primitiveAssertion1.setValue(5);
        currentStatement = testCase.getStatement(5);
        currentStatement.addAssertion(primitiveAssertion1);

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

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement = testCase.getStatement(3);
        Inspector inspector = new Inspector(TestSmellsTestingClass1.class, TestSmellsTestingClass1.class.getMethod("getNumber"));
        InspectorAssertion inspectorAssertion0 = new InspectorAssertion(inspector, currentStatement, constructorStatement0, 5);
        currentStatement.addAssertion(inspectorAssertion0);

        return testCase;
    }

    private DefaultTestCase createTestCase4() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement = testCase.getStatement(1);
        Inspector inspector = new Inspector(TestSmellsSimpleUser.class, TestSmellsSimpleUser.class.getMethod("getName"));
        InspectorAssertion inspectorAssertion0 = new InspectorAssertion(inspector, currentStatement, constructorStatement0, "Bob");
        currentStatement.addAssertion(inspectorAssertion0);

        return testCase;
    }

    private DefaultTestCase createTestCase5() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        VariableReference intStatement0 = builder.appendIntPrimitive(5);

        Method setNumberMethod0 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod0, intStatement0);

        Method getNumberMethod0 = TestSmellsTestingClass1.class.getMethod("getNumber");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNumberMethod0);

        VariableReference intStatement1 = builder.appendIntPrimitive(10);

        Method setNumberMethod1 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod1, intStatement1);

        Method getNumberMethod1 = TestSmellsTestingClass1.class.getMethod("getNumber");
        VariableReference methodStatement1 = builder.appendMethod(constructorStatement0, getNumberMethod1);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement;

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(5);
        currentStatement = testCase.getStatement(4);
        currentStatement.addAssertion(primitiveAssertion0);

        PrimitiveAssertion primitiveAssertion1 = new PrimitiveAssertion();
        primitiveAssertion1.setSource(methodStatement1);
        primitiveAssertion1.setValue(10);
        currentStatement = testCase.getStatement(7);
        currentStatement.addAssertion(primitiveAssertion1);

        return testCase;
    }

    private DefaultTestCase createEmptyTestCase() {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();
        return builder.getDefaultTestCase();
    }
}
