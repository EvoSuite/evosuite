package org.evosuite.testsmells;

import com.examples.with.different.packagename.testsmells.TestSmellsTestingClass2;
import org.evosuite.Properties;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.runtime.mock.java.io.MockFile;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.smells.MysteryGuest;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class MysteryGuestSmellTest {

    AbstractNormalizedTestCaseSmell mysteryGuest;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass2.class.getCanonicalName();
        this.mysteryGuest = new MysteryGuest();
    }

    @Test
    public void testUseMockFileConstructorStatement() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        double smellCount = this.mysteryGuest.computeTestSmellMetric(testCase);
        double expected = 0;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testUseFileConstructorStatement() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase1();
        testCase.setTestCase(test0);

        double smellCount = this.mysteryGuest.computeTestSmellMetric(testCase);
        double expected = 1;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testFileObjectMethodStatement() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase2();
        testCase.setTestCase(test0);

        double smellCount = this.mysteryGuest.computeTestSmellMetric(testCase);
        double expected = 2;
        assertEquals(expected, smellCount, 0.01);
    }

    @Test
    public void testFullTestSuite() throws NoSuchMethodException {
        TestSuiteChromosome suite = new TestSuiteChromosome();
        DefaultTestCase test0 = createTestCase0();
        DefaultTestCase test1 = createTestCase1();
        DefaultTestCase test2 = createTestCase2();
        suite.addTest(test0);
        suite.addTest(test1);
        suite.addTest(test2);

        double smellCount = this.mysteryGuest.computeTestSmellMetric(suite);
        double expected = 3;
        assertEquals(expected, smellCount, 0.01);
    }

    private DefaultTestCase createTestCase0() throws NoSuchMethodException {

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

    private DefaultTestCase createTestCase1() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        Constructor<TestSmellsTestingClass2> const0 = TestSmellsTestingClass2.class.getConstructor();
        VariableReference constructorStatement0 = builder.appendConstructor(const0);

        VariableReference stringStatement0 = builder.appendStringPrimitive("randomFile.random");

        Constructor<File> const1 = File.class.getConstructor(String.class);
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

    private DefaultTestCase createTestCase2() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        Constructor<TestSmellsTestingClass2> const0 = TestSmellsTestingClass2.class.getConstructor();
        VariableReference constructorStatement0 = builder.appendConstructor(const0);

        VariableReference stringStatement0 = builder.appendStringPrimitive("randomFile.random");

        Constructor<MockFile> const1 = MockFile.class.getConstructor(String.class);
        VariableReference constructorStatement1 = builder.appendConstructor(const1, stringStatement0);

        VariableReference stringStatement1 = builder.appendStringPrimitive("anotherRandomFile");
        VariableReference stringStatement2 = builder.appendStringPrimitive("moreRandomNames");

        Method createTempFileMethod0 = MockFile.class.getMethod("createTempFile", String.class, String.class);
        builder.appendMethod(constructorStatement1, createTempFileMethod0, stringStatement1, stringStatement2);

        Method createTempFileMethod1 = MockFile.class.getMethod("createTempFile", String.class, String.class);
        builder.appendMethod(constructorStatement1, createTempFileMethod1, stringStatement1, stringStatement2);

        Method addFileMethod0 = TestSmellsTestingClass2.class.getMethod("addFile", File.class);
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, addFileMethod0, constructorStatement1);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(true);
        Statement currentStatement = testCase.getStatement(7);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }
}
