package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsSimpleUser;
import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass1;
import org.evosuite.Properties;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.smells.SensitiveEquality;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class SensitiveEqualitySmellTest {

    AbstractTestCaseSmell sensitiveEquality;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass1.class.getCanonicalName();
        this.sensitiveEquality = new SensitiveEquality();
    }

    @Test
    public void testToStringMethodNotUsed() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        double smellCount = this.sensitiveEquality.computeNumberOfSmells(testCase);
        double expected = 0;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testToStringMethodUsedToExerciseClassUnderTest() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase1();
        testCase.setTestCase(test0);

        double smellCount = this.sensitiveEquality.computeNumberOfSmells(testCase);
        double expected = 0;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testToStringMethodDoesNotExerciseClassUnderTestNoAssertion() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase2();
        testCase.setTestCase(test0);

        double smellCount = this.sensitiveEquality.computeNumberOfSmells(testCase);
        double expected = 0;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testToStringMethodDoesNotExerciseClassUnderTestWithAssertion() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase3();
        testCase.setTestCase(test0);

        double smellCount = this.sensitiveEquality.computeNumberOfSmells(testCase);
        double expected = 1;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testToStringMethodUsedToExerciseClassUnderTestInspectorAssertion() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase4();
        testCase.setTestCase(test0);

        double smellCount = this.sensitiveEquality.computeNumberOfSmells(testCase);
        double expected = 0;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testToStringMethodDoesNotExerciseClassUnderTestInspectorAssertion() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase5();
        testCase.setTestCase(test0);

        double smellCount = this.sensitiveEquality.computeNumberOfSmells(testCase);
        double expected = 1;
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
        suite.addTest(test0);
        suite.addTest(test1);
        suite.addTest(test2);
        suite.addTest(test3);
        suite.addTest(test4);
        suite.addTest(test5);

        double smellCount = this.sensitiveEquality.computeNumberOfSmells(suite);
        double expected = 2;
        assertEquals(expected, smellCount, 0.01);
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

        VariableReference stringStatement1 = builder.appendStringPrimitive("Something");

        Method setSomethingMethod0 = TestSmellsTestingClass1.class.getMethod("setSomething", String.class);
        builder.appendMethod(constructorStatement0, setSomethingMethod0, stringStatement1);

        Method toStringMethod0 = TestSmellsTestingClass1.class.getMethod("toString");
        builder.appendMethod(constructorStatement0, toStringMethod0);

        return builder.getDefaultTestCase();
    }

    private DefaultTestCase createTestCase2() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        builder.appendConstructor(const0, stringStatement0);

        Constructor<TestSmellsSimpleUser> const1 = TestSmellsSimpleUser.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const1, stringStatement0);

        Method toStringMethod0 = TestSmellsSimpleUser.class.getMethod("toString");
        builder.appendMethod(constructorStatement0, toStringMethod0);

        return builder.getDefaultTestCase();
    }

    private DefaultTestCase createTestCase3() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        builder.appendConstructor(const0, stringStatement0);

        Constructor<TestSmellsSimpleUser> const1 = TestSmellsSimpleUser.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const1, stringStatement0);

        Method toStringMethod0 = TestSmellsSimpleUser.class.getMethod("toString");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, toStringMethod0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("TestSmellsSimpleUser{name='Bob'}");
        Statement currentStatement = testCase.getStatement(3);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }

    private DefaultTestCase createTestCase4() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        VariableReference intStatement0 = builder.appendIntPrimitive(5);

        Method setNumberMethod0 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod0, intStatement0);

        VariableReference stringStatement1 = builder.appendStringPrimitive("Something");

        Method setSomethingMethod0 = TestSmellsTestingClass1.class.getMethod("setSomething", String.class);
        builder.appendMethod(constructorStatement0, setSomethingMethod0, stringStatement1);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement = testCase.getStatement(5);
        Inspector inspector = new Inspector(TestSmellsTestingClass1.class, TestSmellsTestingClass1.class.getMethod("toString"));
        InspectorAssertion inspectorAssertion0 = new InspectorAssertion(inspector, currentStatement, constructorStatement0,
                "TestSmellsTestingClass1{name='Bob', number=5, something='Something'}");
        currentStatement.addAssertion(inspectorAssertion0);

        return testCase;
    }

    private DefaultTestCase createTestCase5() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        builder.appendConstructor(const0, stringStatement0);

        Constructor<TestSmellsSimpleUser> const1 = TestSmellsSimpleUser.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const1, stringStatement0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement = testCase.getStatement(2);
        Inspector inspector = new Inspector(TestSmellsSimpleUser.class, TestSmellsSimpleUser.class.getMethod("toString"));
        InspectorAssertion inspectorAssertion0 = new InspectorAssertion(inspector, currentStatement, constructorStatement0, "TestSmellsSimpleUser{name='Bob'}");
        currentStatement.addAssertion(inspectorAssertion0);

        return testCase;
    }
}
