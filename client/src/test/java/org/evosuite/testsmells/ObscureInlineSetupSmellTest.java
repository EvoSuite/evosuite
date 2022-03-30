package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass1;
import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass2;
import org.evosuite.Properties;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.smells.ObscureInlineSetup;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class ObscureInlineSetupSmellTest {

    AbstractTestCaseSmell obscureInlineSetup;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass1.class.getCanonicalName();
        this.obscureInlineSetup = new ObscureInlineSetup();
    }

    @Test
    public void testNoDeclaredVariables() {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        double smellCount = this.obscureInlineSetup.computeNumberOfTestSmells(testCase);
        double expected = 0;
        assertEquals(expected, smellCount, 0.01);

        double computedMetric = this.obscureInlineSetup.computeTestSmellMetric(testCase);
        assertEquals(expected, computedMetric, 0.01);
    }

    @Test
    public void testOneDeclaredVariablePrimitiveStatement() {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase1();
        testCase.setTestCase(test0);

        double smellCount = this.obscureInlineSetup.computeNumberOfTestSmells(testCase);
        double expected = 1.0;
        assertEquals(expected, smellCount, 0.01);

        double computedMetric = this.obscureInlineSetup.computeTestSmellMetric(testCase);
        expected = 0.5;
        assertEquals(expected, computedMetric, 0.01);
    }

    @Test
    public void testOneDeclaredVariableConstructorStatement() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase2();
        testCase.setTestCase(test0);

        double smellCount = this.obscureInlineSetup.computeNumberOfTestSmells(testCase);
        double expected = 1.0;
        assertEquals(expected, smellCount, 0.01);

        double computedMetric = this.obscureInlineSetup.computeTestSmellMetric(testCase);
        expected = 0.5;
        assertEquals(expected, computedMetric, 0.01);
    }

    @Test
    public void testTwoDeclaredVariables() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase3();
        testCase.setTestCase(test0);

        double smellCount = this.obscureInlineSetup.computeNumberOfTestSmells(testCase);
        double expected = 2.0;
        assertEquals(expected, smellCount, 0.01);

        double computedMetric = this.obscureInlineSetup.computeTestSmellMetric(testCase);
        expected = 2.0 / (1.0 + 2.0);
        assertEquals(expected, computedMetric, 0.01);
    }

    @Test
    public void testThreeDeclaredVariablesMethodStatement() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase4();
        testCase.setTestCase(test0);

        double smellCount = this.obscureInlineSetup.computeNumberOfTestSmells(testCase);
        double expected = 3.0;
        assertEquals(expected, smellCount, 0.01);

        double computedMetric = this.obscureInlineSetup.computeTestSmellMetric(testCase);
        expected = 0.75;
        assertEquals(expected, computedMetric, 0.01);
    }

    @Test
    public void testThreeDeclaredVariablesMethodStatementVoid() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase5();
        testCase.setTestCase(test0);

        double smellCount = this.obscureInlineSetup.computeNumberOfTestSmells(testCase);
        double expected = 3.0;
        assertEquals(expected, smellCount, 0.01);

        double computedMetric = this.obscureInlineSetup.computeTestSmellMetric(testCase);
        expected = 0.75;
        assertEquals(expected, computedMetric, 0.01);
    }

    @Test
    public void testMultipleDeclaredVariables() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase6();
        testCase.setTestCase(test0);

        double smellCount = this.obscureInlineSetup.computeNumberOfTestSmells(testCase);
        double expected = 6.0;
        assertEquals(expected, smellCount, 0.01);

        double computedMetric = this.obscureInlineSetup.computeTestSmellMetric(testCase);
        expected = 6.0 / (1.0 + 6.0);
        assertEquals(expected, computedMetric, 0.01);
    }

    @Test
    public void testFullTestSuite() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase0();
        DefaultTestCase test1 = createTestCase1();
        DefaultTestCase test2 = createTestCase2();
        DefaultTestCase test3 = createTestCase3();
        DefaultTestCase test4 = createTestCase4();
        DefaultTestCase test5 = createTestCase5();
        DefaultTestCase test6 = createTestCase6();
        suite.addTest(test0);
        suite.addTest(test1);
        suite.addTest(test2);
        suite.addTest(test3);
        suite.addTest(test4);
        suite.addTest(test5);
        suite.addTest(test6);

        double smellCount = this.obscureInlineSetup.computeTestSmellMetric(suite);
        double expected = 16.0 / (1.0 + 16.0);
        assertEquals(expected, smellCount, 0.01);
    }

    private DefaultTestCase createTestCase0() {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();
        return builder.getDefaultTestCase();
    }

    private DefaultTestCase createTestCase1() {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        builder.appendStringPrimitive("Bob");

        return builder.getDefaultTestCase();
    }

    private DefaultTestCase createTestCase2() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        Constructor<TestSmellsTestingClass2> const0 = TestSmellsTestingClass2.class.getConstructor();
        builder.appendConstructor(const0);

        return builder.getDefaultTestCase();
    }

    private DefaultTestCase createTestCase3() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        builder.appendConstructor(const0, stringStatement0);

        return builder.getDefaultTestCase();
    }

    private DefaultTestCase createTestCase4() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        Method getNameMethod0 = TestSmellsTestingClass1.class.getMethod("getName");
        builder.appendMethod(constructorStatement0, getNameMethod0);

        return builder.getDefaultTestCase();
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

        return builder.getDefaultTestCase();
    }

    private DefaultTestCase createTestCase6() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        Method getNameMethod0 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNameMethod0);

        Method getNameMethod1 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement1 = builder.appendMethod(constructorStatement0, getNameMethod1);

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

        PrimitiveAssertion primitiveAssertion1 = new PrimitiveAssertion();
        primitiveAssertion1.setSource(methodStatement1);
        primitiveAssertion1.setValue("Bob");
        currentStatement = testCase.getStatement(3);
        currentStatement.addAssertion(primitiveAssertion1);

        PrimitiveAssertion primitiveAssertion2 = new PrimitiveAssertion();
        primitiveAssertion2.setSource(methodStatement2);
        primitiveAssertion2.setValue(5);
        currentStatement = testCase.getStatement(6);
        currentStatement.addAssertion(primitiveAssertion2);

        return testCase;
    }
}
