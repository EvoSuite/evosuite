package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsTemporaryClass;
import org.evosuite.Properties;
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
        Properties.TARGET_CLASS = TestSmellsTemporaryClass.class.getCanonicalName();
        this.eagerTest = new EagerTest();
    }

    @Test
    public void testTwoDifferentMethodsTestCase() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        int smellCount = this.eagerTest.computeNumberOfSmells(testCase);
        int expected = 2;
        assertEquals(expected, smellCount);
    }

    @Test
    public void testRepeatedMethodTestCase() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase1();
        testCase.setTestCase(test0);

        int smellCount = this.eagerTest.computeNumberOfSmells(testCase);
        int expected = 1;
        assertEquals(expected, smellCount);
    }

    @Test
    public void testEmptyTestCase() {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createEmptyTestCase();
        testCase.setTestCase(test0);

        int smellCount = this.eagerTest.computeNumberOfSmells(testCase);
        int expected = 0;
        assertEquals(expected, smellCount);
    }

    @Test
    public void testFullTestSuite() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase0();
        DefaultTestCase test1 = createTestCase1();
        DefaultTestCase test2 = createEmptyTestCase();
        suite.addTest(test0);
        suite.addTest(test1);
        suite.addTest(test2);

        int smellCount = this.eagerTest.computeNumberOfSmells(suite);
        int expected = 3;
        assertEquals(expected, smellCount);
    }

    private DefaultTestCase createTestCase0 () throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("MySever1");
        VariableReference stringStatement1 = builder.appendStringPrimitive("Bob");
        VariableReference stringStatement2 = builder.appendStringPrimitive("qwerty123");

        Constructor<TestSmellsTemporaryClass> const0 = TestSmellsTemporaryClass.class.getConstructor(String.class, String.class, String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0, stringStatement1, stringStatement2);

        VariableReference stringStatement3 = builder.appendStringPrimitive("Alice");
        VariableReference stringStatement4 = builder.appendStringPrimitive("12345");

        Method addUserMethod0 = TestSmellsTemporaryClass.class.getMethod("addUser", String.class, String.class, String.class, String.class);
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, addUserMethod0, stringStatement1, stringStatement2, stringStatement3, stringStatement4);

        Method addUserMethod1 = TestSmellsTemporaryClass.class.getMethod("getServerID");
        VariableReference methodStatement1 = builder.appendMethod(constructorStatement0, addUserMethod1);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement;

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(true);
        currentStatement = testCase.getStatement(6);
        currentStatement.addAssertion(primitiveAssertion0);

        PrimitiveAssertion primitiveAssertion1 = new PrimitiveAssertion();
        primitiveAssertion1.setSource(methodStatement1);
        primitiveAssertion1.setValue(12345);
        currentStatement = testCase.getStatement(7);
        currentStatement.addAssertion(primitiveAssertion1);

        return testCase;
    }

    private DefaultTestCase createTestCase1 () throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("MySever2");
        VariableReference stringStatement1 = builder.appendStringPrimitive("Jon");
        VariableReference stringStatement2 = builder.appendStringPrimitive("asdf6789");

        Constructor<TestSmellsTemporaryClass> const0 = TestSmellsTemporaryClass.class.getConstructor(String.class, String.class, String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0, stringStatement1, stringStatement2);

        VariableReference stringStatement3 = builder.appendStringPrimitive("George");
        VariableReference stringStatement4 = builder.appendStringPrimitive("12345");

        Method addUserMethod0 = TestSmellsTemporaryClass.class.getMethod("addUser", String.class, String.class, String.class, String.class);

        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, addUserMethod0, stringStatement1, stringStatement2, stringStatement1, stringStatement2);

        VariableReference methodStatement1 = builder.appendMethod(constructorStatement0, addUserMethod0, stringStatement1, stringStatement2, stringStatement3, stringStatement4);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement;

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(false);
        currentStatement = testCase.getStatement(6);
        currentStatement.addAssertion(primitiveAssertion0);

        PrimitiveAssertion primitiveAssertion1 = new PrimitiveAssertion();
        primitiveAssertion1.setSource(methodStatement1);
        primitiveAssertion1.setValue(true);
        currentStatement = testCase.getStatement(7);
        currentStatement.addAssertion(primitiveAssertion1);

        return testCase;
    }

    private DefaultTestCase createEmptyTestCase () {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();
        return builder.getDefaultTestCase();
    }
}
