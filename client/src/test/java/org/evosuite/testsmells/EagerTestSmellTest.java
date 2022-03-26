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
import org.evosuite.testsmells.smells.EagerTest;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class EagerTestSmellTest {

    AbstractTestCaseSmell eagerTest;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass1.class.getCanonicalName();
        this.eagerTest = new EagerTest();
    }

    @Test
    public void testOneMethod() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        double smellCount = this.eagerTest.computeNumberOfSmells(testCase);
        double expected = 1;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testTwoDifferentMethods() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase1();
        testCase.setTestCase(test0);

        double smellCount = this.eagerTest.computeNumberOfSmells(testCase);
        double expected = 2;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testTwoRepeatedMethods() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase2();
        testCase.setTestCase(test0);

        double smellCount = this.eagerTest.computeNumberOfSmells(testCase);
        double expected = 1;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testThreeDifferentMethods() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase3();
        testCase.setTestCase(test0);

        double smellCount = this.eagerTest.computeNumberOfSmells(testCase);
        double expected = 3;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testDifferentMethodInInspectorAssertion() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase4();
        testCase.setTestCase(test0);

        double smellCount = this.eagerTest.computeNumberOfSmells(testCase);
        double expected = 3;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testRepeatedMethodInInspectorAssertion() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase5();
        testCase.setTestCase(test0);

        double smellCount = this.eagerTest.computeNumberOfSmells(testCase);
        double expected = 3;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testMethodOfADifferentClass() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase6();
        testCase.setTestCase(test0);

        double smellCount = this.eagerTest.computeNumberOfSmells(testCase);
        double expected = 2;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testMethodOfADifferentClassInInspectorAssertion() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase7();
        testCase.setTestCase(test0);

        double smellCount = this.eagerTest.computeNumberOfSmells(testCase);
        double expected = 2;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testMethodsOfDifferentInstancesOfTheSameClass() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase8();
        testCase.setTestCase(test0);

        double smellCount = this.eagerTest.computeNumberOfSmells(testCase);
        double expected = 2;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testEmptyTestCase() {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createEmptyTestCase();
        testCase.setTestCase(test0);

        double smellCount = this.eagerTest.computeNumberOfSmells(testCase);
        double expected = 0;
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
        DefaultTestCase test8 = createTestCase8();
        DefaultTestCase test9 = createEmptyTestCase();
        suite.addTest(test0);
        suite.addTest(test1);
        suite.addTest(test2);
        suite.addTest(test3);
        suite.addTest(test4);
        suite.addTest(test5);
        suite.addTest(test6);
        suite.addTest(test7);
        suite.addTest(test8);
        suite.addTest(test9);

        double smellCount = this.eagerTest.computeNumberOfSmells(suite);
        double expected = 19;
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

    private DefaultTestCase createTestCase2() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        VariableReference intStatement0 = builder.appendIntPrimitive(5);

        Method setNumberMethod0 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod0, intStatement0);

        VariableReference intStatement1 = builder.appendIntPrimitive(7);

        Method setNumberMethod1 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod1, intStatement1);

        return builder.getDefaultTestCase();
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

    private DefaultTestCase createTestCase4() throws NoSuchMethodException {

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

        currentStatement = testCase.getStatement(4);
        Inspector inspector = new Inspector(TestSmellsTestingClass1.class, TestSmellsTestingClass1.class.getMethod("getNumber"));
        InspectorAssertion inspectorAssertion0 = new InspectorAssertion(inspector, currentStatement, constructorStatement0, 5);
        currentStatement.addAssertion(inspectorAssertion0);

        return testCase;
    }

    private DefaultTestCase createTestCase5() throws NoSuchMethodException {

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

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("Bob");
        Statement currentStatement = testCase.getStatement(2);
        currentStatement.addAssertion(primitiveAssertion0);

        currentStatement = testCase.getStatement(4);
        Inspector inspector = new Inspector(TestSmellsTestingClass1.class, TestSmellsTestingClass1.class.getMethod("getNumber"));
        InspectorAssertion inspectorAssertion0 = new InspectorAssertion(inspector, currentStatement, constructorStatement0, 5);
        currentStatement.addAssertion(inspectorAssertion0);

        PrimitiveAssertion primitiveAssertion1 = new PrimitiveAssertion();
        primitiveAssertion1.setSource(methodStatement1);
        primitiveAssertion1.setValue(5);
        currentStatement = testCase.getStatement(5);
        currentStatement.addAssertion(primitiveAssertion1);

        return testCase;
    }

    private DefaultTestCase createTestCase6() throws NoSuchMethodException {

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

        Constructor<TestSmellsSimpleUser> const1 = TestSmellsSimpleUser.class.getConstructor(String.class);
        VariableReference constructorStatement1 = builder.appendConstructor(const1, stringStatement0);

        Method getNameMethod0 = TestSmellsSimpleUser.class.getMethod("getName");
        VariableReference methodStatement1 = builder.appendMethod(constructorStatement1, getNameMethod0);

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
        primitiveAssertion1.setValue("Bob");
        currentStatement = testCase.getStatement(6);
        currentStatement.addAssertion(primitiveAssertion1);

        return testCase;
    }


    private DefaultTestCase createTestCase7() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        Constructor<TestSmellsSimpleUser> const1 = TestSmellsSimpleUser.class.getConstructor(String.class);
        VariableReference constructorStatement1 = builder.appendConstructor(const1, stringStatement0);

        VariableReference intStatement0 = builder.appendIntPrimitive(5);

        Method setNumberMethod0 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod0, intStatement0);

        Method getNumberMethod0 = TestSmellsTestingClass1.class.getMethod("getNumber");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNumberMethod0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement;

        currentStatement = testCase.getStatement(4);
        Inspector inspector = new Inspector(TestSmellsSimpleUser.class, TestSmellsSimpleUser.class.getMethod("getName"));
        InspectorAssertion inspectorAssertion0 = new InspectorAssertion(inspector, currentStatement, constructorStatement1, "Bob");
        currentStatement.addAssertion(inspectorAssertion0);

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(5);
        currentStatement = testCase.getStatement(5);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }

    private DefaultTestCase createTestCase8() throws NoSuchMethodException {

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

        Constructor<TestSmellsTestingClass1> const1 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement1 = builder.appendConstructor(const1, stringStatement0);

        VariableReference intStatement1 = builder.appendIntPrimitive(5);

        Method setNumberMethod1 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement1, setNumberMethod1, intStatement1);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(5);
        Statement currentStatement = testCase.getStatement(4);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }

    private DefaultTestCase createEmptyTestCase() {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();
        return builder.getDefaultTestCase();
    }
}
