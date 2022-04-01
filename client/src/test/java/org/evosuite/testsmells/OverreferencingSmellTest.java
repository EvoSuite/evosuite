package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsSimpleServer;
import com.examples.with.different.packagename.testsmells.TestSmellsSimpleUser;
import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass1;
import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass2;
import org.evosuite.assertion.Inspector;
import org.evosuite.assertion.InspectorAssertion;
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

    AbstractNormalizedTestCaseSmell overreferencing;

    @Before
    public void setUp() {
        this.overreferencing = new Overreferencing();
    }

    @Test
    public void testAllClassInstancesAreUsed() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        long smellCount = this.overreferencing.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 0;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.overreferencing.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testOneUnnecessaryClassInstance() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase1();
        testCase.setTestCase(test0);

        long smellCount =  this.overreferencing.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 1;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.overreferencing.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0.5;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testTwoEqualUnnecessaryClassInstances() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase2();
        testCase.setTestCase(test0);

        long smellCount = this.overreferencing.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 2;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.overreferencing.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 2.0 / (1.0 + 2.0);
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testTwoDifferentUnnecessaryClassInstances() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase3();
        testCase.setTestCase(test0);

        long smellCount = this.overreferencing.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 2;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.overreferencing.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 2.0 / (1.0 + 2.0);
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testObjectOfClassPassedAsArgumentToTheMethod() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase4();
        testCase.setTestCase(test0);

        long smellCount = this.overreferencing.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 0;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.overreferencing.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testObjectOfClassPassedAsArgumentToTheConstructor() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase5();
        testCase.setTestCase(test0);

        long smellCount = this.overreferencing.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 0;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.overreferencing.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testClassInstancesUsedInInspectorAssertions() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase6();
        testCase.setTestCase(test0);

        long smellCount = this.overreferencing.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 0;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.overreferencing.computeTestSmellMetric(testCase);
        double expectedComputedMetric = 0;
        assertEquals(expectedComputedMetric, computedMetric, 0.01);
    }

    @Test
    public void testEmptyTestCase() {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createEmptyTestCase();
        testCase.setTestCase(test0);

        long smellCount = this.overreferencing.computeNumberOfTestSmells(testCase);
        long expectedSmellCount = 0;
        assertEquals(expectedSmellCount, smellCount);

        double computedMetric = this.overreferencing.computeTestSmellMetric(testCase);
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
        DefaultTestCase test5 = createTestCase5();
        DefaultTestCase test6 = createTestCase6();
        DefaultTestCase test7 = createEmptyTestCase();
        suite.addTest(test0);
        suite.addTest(test1);
        suite.addTest(test2);
        suite.addTest(test3);
        suite.addTest(test4);
        suite.addTest(test5);
        suite.addTest(test6);
        suite.addTest(test7);

        double computedMetric = this.overreferencing.computeTestSmellMetric(suite);
        double expectedComputedMetric = 5.0 / (1.0 + 5.0);
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
        builder.appendConstructor(const0);

        VariableReference stringStatement0 = builder.appendStringPrimitive("randomFile.random");

        Constructor<MockFile> const1 = MockFile.class.getConstructor(String.class);
        builder.appendConstructor(const1, stringStatement0);

        return builder.getDefaultTestCase();
    }

    private DefaultTestCase createTestCase4() throws NoSuchMethodException {

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

    private DefaultTestCase createTestCase5() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsSimpleUser> const0 = TestSmellsSimpleUser.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        Constructor<TestSmellsSimpleServer> const1 = TestSmellsSimpleServer.class.getConstructor(TestSmellsSimpleUser.class);
        VariableReference constructorStatement1 = builder.appendConstructor(const1, constructorStatement0);

        Method getAdminMethod0 = TestSmellsSimpleServer.class.getMethod("getAdmin");
        builder.appendMethod(constructorStatement1, getAdminMethod0);

        return builder.getDefaultTestCase();
    }

    private DefaultTestCase createTestCase6() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        VariableReference stringStatement0 = builder.appendStringPrimitive("Bob");

        Constructor<TestSmellsTestingClass1> const0 = TestSmellsTestingClass1.class.getConstructor(String.class);
        VariableReference constructorStatement0 = builder.appendConstructor(const0, stringStatement0);

        builder.appendIntPrimitive(5);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        Statement currentStatement = testCase.getStatement(2);
        Inspector inspector = new Inspector(TestSmellsTestingClass1.class, TestSmellsTestingClass1.class.getMethod("getName"));
        InspectorAssertion inspectorAssertion0 = new InspectorAssertion(inspector, currentStatement, constructorStatement0, "Bob");
        currentStatement.addAssertion(inspectorAssertion0);

        return testCase;
    }

    private DefaultTestCase createEmptyTestCase() {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();
        return builder.getDefaultTestCase();
    }
}
