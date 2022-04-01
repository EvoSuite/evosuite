package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsSimpleUser;
import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass1;
import org.evosuite.Properties;
import org.evosuite.assertion.ArrayEqualsAssertion;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorAssertion;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.smells.DuplicateAssert;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class DuplicateAssertSmellTest {

    AbstractNormalizedTestCaseSmell duplicateAssert;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass1.class.getCanonicalName();
        this.duplicateAssert = new DuplicateAssert();
    }

    @Test
    public void testDifferentAssertions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        double smellCount = this.duplicateAssert.computeTestSmellMetric(testCase);
        double expected = 0;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testTwoStatementsWithEqualAssertions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase1();
        testCase.setTestCase(test0);

        double smellCount = this.duplicateAssert.computeTestSmellMetric(testCase);
        double expected = 1;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testThreeStatementsWithEqualAssertions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase2();
        testCase.setTestCase(test0);

        double smellCount = this.duplicateAssert.computeTestSmellMetric(testCase);
        double expected = 2;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testDifferentInstancesOfTheSameClassWithEqualAssertions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase3();
        testCase.setTestCase(test0);

        double smellCount = this.duplicateAssert.computeTestSmellMetric(testCase);
        double expected = 1;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testDifferentClassesWithEqualAssertions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase4();
        testCase.setTestCase(test0);

        double smellCount = this.duplicateAssert.computeTestSmellMetric(testCase);
        double expected = 0;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testDifferentInspectorAssertion() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase5();
        testCase.setTestCase(test0);

        double smellCount = this.duplicateAssert.computeTestSmellMetric(testCase);
        double expected = 0;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testTwoEqualInspectorAssertions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase6();
        testCase.setTestCase(test0);

        double smellCount = this.duplicateAssert.computeTestSmellMetric(testCase);
        double expected = 1;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testDifferentInspectorAssertionNotInMethodStatement() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase7();
        testCase.setTestCase(test0);

        double smellCount = this.duplicateAssert.computeTestSmellMetric(testCase);
        double expected = 0;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testTwoEqualInspectorAssertionsNotInMethodStatement() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase8();
        testCase.setTestCase(test0);

        double smellCount = this.duplicateAssert.computeTestSmellMetric(testCase);
        double expected = 1;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testInputValueEqualToReturnValue() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase9();
        testCase.setTestCase(test0);

        double smellCount = this.duplicateAssert.computeTestSmellMetric(testCase);
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
        DefaultTestCase test6 = createTestCase6();
        DefaultTestCase test7 = createTestCase7();
        DefaultTestCase test8 = createTestCase8();
        DefaultTestCase test9 = createTestCase9();
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

        double smellCount = this.duplicateAssert.computeTestSmellMetric(suite);
        double expected = 7;
        assertEquals(expected, smellCount, 0.01);
    }

    private DefaultTestCase createTestCase0() throws NoSuchMethodException {

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

        Method getNameMethod0 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement2 = builder.appendMethod(constructorStatement0, getNameMethod0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement;

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(5);
        currentStatement = testCase.getStatement(4);
        currentStatement.addAssertion(primitiveAssertion0);

        PrimitiveAssertion primitiveAssertion1 = new PrimitiveAssertion();
        primitiveAssertion1.setSource(methodStatement2);
        primitiveAssertion1.setValue("Bob");
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

        VariableReference intStatement0 = builder.appendIntPrimitive(5);

        Method setNumberMethod0 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod0, intStatement0);

        Method getNumberMethod0 = TestSmellsTestingClass1.class.getMethod("getNumber");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNumberMethod0);

        Method getNumberMethod1 = TestSmellsTestingClass1.class.getMethod("getNumber");
        VariableReference methodStatement1 = builder.appendMethod(constructorStatement0, getNumberMethod1);

        Method getNameMethod0 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement2 = builder.appendMethod(constructorStatement0, getNameMethod0);

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
        primitiveAssertion1.setValue(5);
        currentStatement = testCase.getStatement(5);
        currentStatement.addAssertion(primitiveAssertion1);

        PrimitiveAssertion primitiveAssertion2 = new PrimitiveAssertion();
        primitiveAssertion2.setSource(methodStatement2);
        primitiveAssertion2.setValue("Bob");
        currentStatement = testCase.getStatement(6);
        currentStatement.addAssertion(primitiveAssertion2);

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

        Method isPositiveMethod0 = TestSmellsTestingClass1.class.getMethod("isPositive");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, isPositiveMethod0);

        VariableReference intStatement1 = builder.appendIntPrimitive(7);

        Method setNumberMethod1 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod1, intStatement1);

        Method isPositiveMethod1 = TestSmellsTestingClass1.class.getMethod("isPositive");
        VariableReference methodStatement1 = builder.appendMethod(constructorStatement0, isPositiveMethod1);

        VariableReference intStatement2 = builder.appendIntPrimitive(1);

        Method setNumberMethod2 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod2, intStatement2);

        Method isPositiveMethod2 = TestSmellsTestingClass1.class.getMethod("isPositive");
        VariableReference methodStatement2 = builder.appendMethod(constructorStatement0, isPositiveMethod2);

        VariableReference intStatement3 = builder.appendIntPrimitive(0);

        Method setNumberMethod3 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod3, intStatement3);

        Method isPositiveMethod3 = TestSmellsTestingClass1.class.getMethod("isPositive");
        VariableReference methodStatement3 = builder.appendMethod(constructorStatement0, isPositiveMethod3);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement;

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(true);
        currentStatement = testCase.getStatement(4);
        currentStatement.addAssertion(primitiveAssertion0);

        PrimitiveAssertion primitiveAssertion1 = new PrimitiveAssertion();
        primitiveAssertion1.setSource(methodStatement1);
        primitiveAssertion1.setValue(true);
        currentStatement = testCase.getStatement(7);
        currentStatement.addAssertion(primitiveAssertion1);

        PrimitiveAssertion primitiveAssertion2 = new PrimitiveAssertion();
        primitiveAssertion2.setSource(methodStatement2);
        primitiveAssertion2.setValue(true);
        currentStatement = testCase.getStatement(10);
        currentStatement.addAssertion(primitiveAssertion2);

        PrimitiveAssertion primitiveAssertion3 = new PrimitiveAssertion();
        primitiveAssertion3.setSource(methodStatement3);
        primitiveAssertion3.setValue(false);
        currentStatement = testCase.getStatement(13);
        currentStatement.addAssertion(primitiveAssertion3);

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

        Constructor<TestSmellsTestingClass1> const1 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement1 = builder.appendConstructor(const1, stringStatement0);

        Method getNameMethod1 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement1 = builder.appendMethod(constructorStatement1, getNameMethod1);

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
        currentStatement = testCase.getStatement(4);
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

        Constructor<TestSmellsSimpleUser> const1 = TestSmellsSimpleUser.class.getConstructor(String.class);
        VariableReference constructorStatement1 = builder.appendConstructor(const1, stringStatement0);

        Method getNameMethod1 = TestSmellsSimpleUser.class.getMethod("getName");
        VariableReference methodStatement1 = builder.appendMethod(constructorStatement1, getNameMethod1);

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
        currentStatement = testCase.getStatement(4);
        currentStatement.addAssertion(primitiveAssertion1);

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

    private DefaultTestCase createTestCase6() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        VariableReference intStatement0 = builder.appendIntPrimitive(5);

        Method setNumberMethod0 = TestSmellsTestingClass1.class.getMethod("setNumber", int.class);
        builder.appendMethod(constructorStatement0, setNumberMethod0, intStatement0);

        Method getNameMethod0 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNameMethod0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement;

        currentStatement = testCase.getStatement(3);
        Inspector inspector = new Inspector(TestSmellsTestingClass1.class, TestSmellsTestingClass1.class.getMethod("getNumber"));
        InspectorAssertion inspectorAssertion0 = new InspectorAssertion(inspector, currentStatement, constructorStatement0, 5);
        currentStatement.addAssertion(inspectorAssertion0);

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("Bob");
        currentStatement = testCase.getStatement(4);
        currentStatement.addAssertion(primitiveAssertion0);

        currentStatement = testCase.getStatement(4);
        inspector = new Inspector(TestSmellsTestingClass1.class, TestSmellsTestingClass1.class.getMethod("getNumber"));
        InspectorAssertion inspectorAssertion2 = new InspectorAssertion(inspector, currentStatement, constructorStatement0, 5);
        currentStatement.addAssertion(inspectorAssertion2);

        return testCase;
    }

    private DefaultTestCase createTestCase7() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement = testCase.getStatement(1);
        Inspector inspector = new Inspector(TestSmellsTestingClass1.class, TestSmellsTestingClass1.class.getMethod("getName"));
        InspectorAssertion inspectorAssertion0 = new InspectorAssertion(inspector, currentStatement, constructorStatement0, "Bob");
        currentStatement.addAssertion(inspectorAssertion0);

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

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement;

        currentStatement = testCase.getStatement(1);
        Inspector inspector = new Inspector(TestSmellsTestingClass1.class, TestSmellsTestingClass1.class.getMethod("getName"));
        InspectorAssertion inspectorAssertion0 = new InspectorAssertion(inspector, currentStatement, constructorStatement0, "Bob");
        currentStatement.addAssertion(inspectorAssertion0);

        currentStatement = testCase.getStatement(2);
        inspector = new Inspector(TestSmellsTestingClass1.class, TestSmellsTestingClass1.class.getMethod("getName"));
        InspectorAssertion inspectorAssertion1 = new InspectorAssertion(inspector, currentStatement, constructorStatement0, "Bob");
        currentStatement.addAssertion(inspectorAssertion1);

        currentStatement = testCase.getStatement(3);
        inspector = new Inspector(TestSmellsTestingClass1.class, TestSmellsTestingClass1.class.getMethod("getNumber"));
        InspectorAssertion inspectorAssertion2 = new InspectorAssertion(inspector, currentStatement, constructorStatement0, 5);
        currentStatement.addAssertion(inspectorAssertion2);

        return testCase;
    }

    private DefaultTestCase createTestCase9() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        Method getNameMethod0 = TestSmellsTestingClass1.class.getMethod("getName");
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, getNameMethod0);

        VariableReference arrayStatement0 = builder.appendArrayStmt(int[].class, 4);

        Method changeAndReturnArrayMethod0 = TestSmellsTestingClass1.class.getMethod("changeAndReturnArray", int[].class);
        VariableReference methodStatement1 = builder.appendMethod(constructorStatement0, changeAndReturnArrayMethod0, arrayStatement0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement;

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("Bob");
        currentStatement = testCase.getStatement(2);
        currentStatement.addAssertion(primitiveAssertion0);

        Object [] expectedArray = {2, 2, 2, 2};

        ArrayEqualsAssertion arrayEqualsAssertion0 = new ArrayEqualsAssertion();
        arrayEqualsAssertion0.setSource(arrayStatement0);
        arrayEqualsAssertion0.setValue(expectedArray);
        currentStatement = testCase.getStatement(4);
        currentStatement.addAssertion(arrayEqualsAssertion0);

        ArrayEqualsAssertion arrayEqualsAssertion1 = new ArrayEqualsAssertion();
        arrayEqualsAssertion1.setSource(methodStatement1);
        arrayEqualsAssertion1.setValue(expectedArray);
        currentStatement = testCase.getStatement(4);
        currentStatement.addAssertion(arrayEqualsAssertion1);

        return testCase;
    }
}
