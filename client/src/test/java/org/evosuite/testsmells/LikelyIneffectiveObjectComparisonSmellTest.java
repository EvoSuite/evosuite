package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass1;
import org.evosuite.Properties;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.smells.LikelyIneffectiveObjectComparison;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class LikelyIneffectiveObjectComparisonSmellTest {

    AbstractTestCaseSmell likelyIneffectiveObjectComparison;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass1.class.getCanonicalName();
        this.likelyIneffectiveObjectComparison = new LikelyIneffectiveObjectComparison();
    }

    @Test
    public void testCompareDifferentObjectsEqualsMethodImplementedInClassUnderTest() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        int smellCount = this.likelyIneffectiveObjectComparison.computeNumberOfSmells(testCase);
        int expected = 0;
        assertEquals(expected, smellCount);
    }

    @Test
    public void testCompareTheSameObjectEqualsMethodImplementedInClassUnderTest() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase1();
        testCase.setTestCase(test0);

        int smellCount = this.likelyIneffectiveObjectComparison.computeNumberOfSmells(testCase);
        int expected = 0;
        assertEquals(expected, smellCount);
    }

    @Test
    public void testCompareDifferentObjectsEqualsMethodNotImplementedInClassUnderTest() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase2();
        testCase.setTestCase(test0);

        int smellCount = this.likelyIneffectiveObjectComparison.computeNumberOfSmells(testCase);
        int expected = 0;
        assertEquals(expected, smellCount);
    }

    @Test
    public void testCompareTheSameObjectEqualsMethodNotImplementedInClassUnderTest() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase3();
        testCase.setTestCase(test0);

        int smellCount = this.likelyIneffectiveObjectComparison.computeNumberOfSmells(testCase);
        int expected = 1;
        assertEquals(expected, smellCount);
    }

    @Test
    public void testFullTestSuite() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase0();
        DefaultTestCase test1 = createTestCase1();
        DefaultTestCase test2 = createTestCase2();
        DefaultTestCase test3 = createTestCase3();
        suite.addTest(test0);
        suite.addTest(test1);
        suite.addTest(test2);
        suite.addTest(test3);

        int smellCount = this.likelyIneffectiveObjectComparison.computeNumberOfSmells(suite);
        int expected = 1;
        assertEquals(expected, smellCount);
    }

    private DefaultTestCase createTestCase0() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        VariableReference stringStatement1 = builder.appendStringPrimitive("John");

        Constructor<TestSmellsTestingClass1> const1 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement1 = builder.appendConstructor(const1, stringStatement1);

        Method equalsMethod0 = TestSmellsTestingClass1.class.getMethod("equals", Object.class);
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, equalsMethod0, constructorStatement1);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(false);
        Statement currentStatement = testCase.getStatement(4);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }

    private DefaultTestCase createTestCase1() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        Method equalsMethod0 = TestSmellsTestingClass1.class.getMethod("equals", Object.class);
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, equalsMethod0, constructorStatement0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(true);
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

        VariableReference stringStatement1 = builder.appendStringPrimitive("John");

        Method equalsMethod0 = String.class.getMethod("equals", Object.class);
        VariableReference methodStatement1 = builder.appendMethod(stringStatement1, equalsMethod0, methodStatement0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement1);
        primitiveAssertion0.setValue(false);
        Statement currentStatement = testCase.getStatement(4);
        currentStatement.addAssertion(primitiveAssertion0);

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

        Method equalsMethod0 = String.class.getMethod("equals", Object.class);
        VariableReference methodStatement1 = builder.appendMethod(methodStatement0, equalsMethod0, methodStatement0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement1);
        primitiveAssertion0.setValue(true);
        Statement currentStatement = testCase.getStatement(3);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }
}
