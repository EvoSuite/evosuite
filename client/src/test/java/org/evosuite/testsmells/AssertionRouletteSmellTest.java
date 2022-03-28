package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsSimpleUser;
import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass1;
import org.evosuite.Properties;
import org.evosuite.assertion.NullAssertion;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.smells.AssertionRoulette;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class AssertionRouletteSmellTest {

    AbstractTestCaseSmell assertionRoulette;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass1.class.getCanonicalName();
        this.assertionRoulette = new AssertionRoulette();
    }

    @Test
    public void testAssertionlessTestCase() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        double smellCount = this.assertionRoulette.computeTestSmellMetric(testCase);
        double expected = 0.0;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testOneAssertionOneStatementThatCallsMethodOfTheClassUnderTest() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase1();
        testCase.setTestCase(test0);

        double smellCount = this.assertionRoulette.computeTestSmellMetric(testCase);
        double expected = 0.0;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testTwoAssertionsOneStatementThatCallsMethodOfTheClassUnderTest() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase2();
        testCase.setTestCase(test0);

        double smellCount = this.assertionRoulette.computeTestSmellMetric(testCase);
        double expected = 0.5;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testOneAssertionTwoStatementsThatCallMethodsOfTheClassUnderTest() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase3();
        testCase.setTestCase(test0);

        double smellCount = this.assertionRoulette.computeTestSmellMetric(testCase);
        double expected = 0.0;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testOneAssertionOneStatementThatCallsMethodOfDifferentClass() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase4();
        testCase.setTestCase(test0);

        double smellCount = this.assertionRoulette.computeTestSmellMetric(testCase);
        double expected = 0.5;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testTwoAssertionsOneStatementThatCallsMethodOfDifferentClass() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase5();
        testCase.setTestCase(test0);

        double smellCount = this.assertionRoulette.computeTestSmellMetric(testCase);
        double expected = 2.0 / (1.0 + 2.0);
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testOneAssertionTwoStatementsThatCallMethodsOfDifferentClass() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase6();
        testCase.setTestCase(test0);

        double smellCount = this.assertionRoulette.computeTestSmellMetric(testCase);
        double expected = 0.5;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testEightAssertionsFiveStatementsThatCallMethodsOfTheClassUnderTest() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase7();
        testCase.setTestCase(test0);

        double smellCount = this.assertionRoulette.computeTestSmellMetric(testCase);
        double expected = 0.75;
        assertEquals(expected, smellCount, 0.01);
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
        DefaultTestCase test7 = createTestCase7();
        suite.addTest(test0);
        suite.addTest(test1);
        suite.addTest(test2);
        suite.addTest(test3);
        suite.addTest(test4);
        suite.addTest(test5);
        suite.addTest(test6);
        suite.addTest(test7);

        double smellCount = this.assertionRoulette.computeTestSmellMetric(suite);
        double expected = 8.0 / (1.0 + 8.0);
        assertEquals(expected, smellCount, 0.01);
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

        return testCase;
    }

    private DefaultTestCase createTestCase3() throws NoSuchMethodException {

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

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("Bob");
        Statement currentStatement = testCase.getStatement(2);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }

    private DefaultTestCase createTestCase4() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsSimpleUser> const0 = TestSmellsSimpleUser.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        Method getNameMethod0 = TestSmellsSimpleUser.class.getMethod("getName");
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

    private DefaultTestCase createTestCase5() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsSimpleUser> const0 = TestSmellsSimpleUser.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        Method getNameMethod0 = TestSmellsSimpleUser.class.getMethod("getName");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNameMethod0);

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

        return testCase;
    }

    private DefaultTestCase createTestCase6() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsSimpleUser> const0 = TestSmellsSimpleUser.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        Method getNameMethod0 = TestSmellsSimpleUser.class.getMethod("getName");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNameMethod0);

        Method getNameMethod1 = TestSmellsSimpleUser.class.getMethod("getName");
        builder.appendMethod(constructorStatement0, getNameMethod1);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("Bob");
        Statement currentStatement = testCase.getStatement(2);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }

    private DefaultTestCase createTestCase7() throws NoSuchMethodException {

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

        Method getNameMethod1 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement2 = builder.appendMethod(constructorStatement0, getNameMethod1);

        Method getNameMethod2 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement3 = builder.appendMethod(constructorStatement0, getNameMethod2);

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
        primitiveAssertion1.setSource(methodStatement1);
        primitiveAssertion1.setValue(5);
        currentStatement = testCase.getStatement(5);
        currentStatement.addAssertion(primitiveAssertion1);

        NullAssertion nullAssertion1 = new NullAssertion();
        nullAssertion1.setSource(methodStatement1);
        nullAssertion1.setValue(false);
        currentStatement = testCase.getStatement(5);
        currentStatement.addAssertion(nullAssertion1);

        PrimitiveAssertion primitiveAssertion2 = new PrimitiveAssertion();
        primitiveAssertion2.setSource(methodStatement2);
        primitiveAssertion2.setValue("Bob");
        currentStatement = testCase.getStatement(6);
        currentStatement.addAssertion(primitiveAssertion2);

        NullAssertion nullAssertion2 = new NullAssertion();
        nullAssertion2.setSource(methodStatement2);
        nullAssertion2.setValue(false);
        currentStatement = testCase.getStatement(6);
        currentStatement.addAssertion(nullAssertion2);

        PrimitiveAssertion primitiveAssertion3 = new PrimitiveAssertion();
        primitiveAssertion3.setSource(methodStatement3);
        primitiveAssertion3.setValue("Bob");
        currentStatement = testCase.getStatement(7);
        currentStatement.addAssertion(primitiveAssertion3);

        NullAssertion nullAssertion3 = new NullAssertion();
        nullAssertion3.setSource(methodStatement3);
        nullAssertion3.setValue(false);
        currentStatement = testCase.getStatement(7);
        currentStatement.addAssertion(nullAssertion3);

        return testCase;
    }
}
