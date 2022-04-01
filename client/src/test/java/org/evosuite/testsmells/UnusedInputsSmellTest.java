package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsSimpleUser;
import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass1;
import org.evosuite.Properties;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.smells.UnusedInputs;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class UnusedInputsSmellTest {

    AbstractNormalizedTestCaseSmell unusedInputs;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass1.class.getCanonicalName();
        this.unusedInputs = new UnusedInputs();
    }

    @Test
    public void testAllMethodsClassUnderTestWithoutVoidReturnTypeCheckedByAssertions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        long smellCount = this.unusedInputs.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 0;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.unusedInputs.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testNoMethodsClassUnderTestWithoutVoidReturnTypeCheckedByAssertions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase1();
        testCase.setTestCase(test0);

        long smellCount = this.unusedInputs.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 2;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.unusedInputs.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 2.0 / (1.0 + 2.0);
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testSomeMethodsClassUnderTestWithoutVoidReturnTypeCheckedByAssertions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase2();
        testCase.setTestCase(test0);

        long smellCount = this.unusedInputs.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 1;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.unusedInputs.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0.5;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testNoMethodsClassUnderTestWithVoidReturnTypeCheckedByAssertions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase3();
        testCase.setTestCase(test0);

        long smellCount = this.unusedInputs.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 0;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.unusedInputs.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testNoMethodsDifferentClassCheckedByAssertions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase4();
        testCase.setTestCase(test0);

        long smellCount = this.unusedInputs.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 0;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.unusedInputs.computeTestSmellMetric(testCase);
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
        DefaultTestCase test4 = createTestCase4();
        suite.addTest(test0);
        suite.addTest(test1);
        suite.addTest(test2);
        suite.addTest(test3);
        suite.addTest(test4);

        double computedMetric = this.unusedInputs.computeTestSmellMetric(suite);
        double expectedComputedMetric = 0.75;
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

    private DefaultTestCase createTestCase1() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        Method getNameMethod0 = TestSmellsTestingClass1.class.getMethod("getName");
        builder.appendMethod(constructorStatement0, getNameMethod0);

        VariableReference intStatement0 = builder.appendIntPrimitive(5);

        Method setNumberMethod0 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod0, intStatement0);

        Method getNumberMethod0 = TestSmellsTestingClass1.class.getMethod("getNumber");
        builder.appendMethod(constructorStatement0, getNumberMethod0);

        return builder.getDefaultTestCase();
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
        builder.appendMethod(constructorStatement0, getNumberMethod0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement;

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("Bob");
        currentStatement = testCase.getStatement(2);
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

        Method setNumberMethod1 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod1, intStatement0);

        Method setNumberMethod2 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod2, intStatement0);

        return builder.getDefaultTestCase();
    }

    private DefaultTestCase createTestCase4() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsSimpleUser> const1 = TestSmellsSimpleUser.class.getConstructor(String.class);
        VariableReference constructorStatement1 = builder.appendConstructor(const1, stringStatement0);

        Method getNameMethod0 = TestSmellsSimpleUser.class.getMethod("getName");
        builder.appendMethod(constructorStatement1, getNameMethod0);

        Method getNameMethod1 = TestSmellsSimpleUser.class.getMethod("getName");
        builder.appendMethod(constructorStatement1, getNameMethod1);

        return builder.getDefaultTestCase();
    }
}
