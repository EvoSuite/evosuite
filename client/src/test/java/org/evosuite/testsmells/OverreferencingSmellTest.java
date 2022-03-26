package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass1;
import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass2;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.runtime.mock.java.io.MockFile;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.smells.Overreferencing;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class OverreferencingSmellTest {

    AbstractTestCaseSmell overreferencing;

    @Before
    public void setUp() {
        this.overreferencing = new Overreferencing();
    }

    @Test
    public void testNoUnnecessaryClassInstances() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        double smellCount = this.overreferencing.computeNumberOfSmells(testCase);
        double expected = 0;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testOneUnnecessaryClassInstance() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase1();
        testCase.setTestCase(test0);

        double smellCount = this.overreferencing.computeNumberOfSmells(testCase);
        double expected = 1;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testTwoEqualUnnecessaryClassInstances() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase2();
        testCase.setTestCase(test0);

        double smellCount = this.overreferencing.computeNumberOfSmells(testCase);
        double expected = 2;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testObjectOfClassUsedAsArgument() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase3();
        testCase.setTestCase(test0);

        double smellCount = this.overreferencing.computeNumberOfSmells(testCase);
        double expected = 0;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testTwoDifferentUnnecessaryClassInstances() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase4();
        testCase.setTestCase(test0);

        double smellCount = this.overreferencing.computeNumberOfSmells(testCase);
        double expected = 2;
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
        suite.addTest(test0);
        suite.addTest(test1);
        suite.addTest(test2);
        suite.addTest(test3);
        suite.addTest(test4);

        double smellCount = this.overreferencing.computeNumberOfSmells(suite);
        double expected = 5;
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
        builder.appendConstructor(const0, stringStatement0);

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

        Constructor<TestSmellsTestingClass1> const1 = TestSmellsTestingClass1.class.getConstructor(String.class);
        builder.appendConstructor(const1, stringStatement0);

        Constructor<TestSmellsTestingClass1> const2 = TestSmellsTestingClass1.class.getConstructor(String.class);
        builder.appendConstructor(const2, stringStatement0);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue("Bob");
        Statement currentStatement = testCase.getStatement(2);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }

    private DefaultTestCase createTestCase3() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        Constructor<TestSmellsTestingClass2> const0 = TestSmellsTestingClass2.class.getConstructor();
        VariableReference constructorStatement0 = builder.appendConstructor(const0);

        VariableReference stringStatement0 = builder.appendStringPrimitive("randomFile.random");

        Constructor<MockFile> const1 = MockFile.class.getConstructor(String.class);
        VariableReference constructorStatement1 = builder.appendConstructor(const1, stringStatement0);

        Method addFileMethod0 = TestSmellsTestingClass2.class.getMethod("addFile", File.class);
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, addFileMethod0, constructorStatement1);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(true);
        Statement currentStatement = testCase.getStatement(3);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }

    private DefaultTestCase createTestCase4() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        Constructor<TestSmellsTestingClass2> const0 = TestSmellsTestingClass2.class.getConstructor();
        builder.appendConstructor(const0);

        VariableReference stringStatement0 = builder.appendStringPrimitive("randomFile.random");

        Constructor<MockFile> const1 = MockFile.class.getConstructor(String.class);
        builder.appendConstructor(const1, stringStatement0);

        return builder.getDefaultTestCase();
    }
}
