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
import org.evosuite.testsmells.smells.ResourceOptimism;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class ResourceOptimismSmellTest {

    AbstractTestCaseSmell resourceOptimism;

    @Before
    public void setUp() {
        Properties.TARGET_CLASS = TestSmellsTestingClass2.class.getCanonicalName();
        this.resourceOptimism = new ResourceOptimism();
    }

    @Test
    public void testUseMockFile() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase0();
        testCase.setTestCase(test0);

        int smellCount = this.resourceOptimism.computeNumberOfSmells(testCase);
        int expected = 0;
        assertEquals(expected, smellCount);
    }

    @Test
    public void testFileObjectMethodStatement() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase1();
        testCase.setTestCase(test0);

        int smellCount = this.resourceOptimism.computeNumberOfSmells(testCase);
        int expected = 1;
        assertEquals(expected, smellCount);
    }

    @Test
    public void testCallMethodOfFileObject() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase2();
        testCase.setTestCase(test0);

        int smellCount = this.resourceOptimism.computeNumberOfSmells(testCase);
        int expected = 2;
        assertEquals(expected, smellCount);
    }

    @Test
    public void testCallMethodOfFileObjectAfterCallingExists() throws NoSuchMethodException {
        TestChromosome testCase = new TestChromosome();
        DefaultTestCase test0 = createTestCase3();
        testCase.setTestCase(test0);

        int smellCount = this.resourceOptimism.computeNumberOfSmells(testCase);
        int expected = 0;
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

        int smellCount = this.resourceOptimism.computeNumberOfSmells(suite);
        int expected = 3;
        assertEquals(expected, smellCount);
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

        Constructor<File> const1 = File.class.getConstructor(String.class);
        VariableReference constructorStatement1 = builder.appendConstructor(const1, stringStatement0);

        Method getNameMethod0 = File.class.getMethod("getName");
        builder.appendMethod(constructorStatement1, getNameMethod0);

        Method addFileMethod0 = TestSmellsTestingClass2.class.getMethod("addFile", File.class);
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, addFileMethod0, constructorStatement1);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(true);
        Statement currentStatement = testCase.getStatement(4);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }

    private DefaultTestCase createTestCase3() throws NoSuchMethodException {

        // Create test case

        TestCaseBuilder builder = new TestCaseBuilder();

        Constructor<TestSmellsTestingClass2> const0 = TestSmellsTestingClass2.class.getConstructor();
        VariableReference constructorStatement0 = builder.appendConstructor(const0);

        VariableReference stringStatement0 = builder.appendStringPrimitive("randomFile.random");

        Constructor<File> const1 = File.class.getConstructor(String.class);
        VariableReference constructorStatement1 = builder.appendConstructor(const1, stringStatement0);

        Method existsMethod0 = File.class.getMethod("exists");
        builder.appendMethod(constructorStatement1, existsMethod0);

        Method getNameMethod0 = File.class.getMethod("getName");
        builder.appendMethod(constructorStatement1, getNameMethod0);

        Method addFileMethod0 = TestSmellsTestingClass2.class.getMethod("addFile", File.class);
        VariableReference methodStatement0 = builder.appendMethod(constructorStatement0, addFileMethod0, constructorStatement1);

        DefaultTestCase testCase = builder.getDefaultTestCase();

        // Add assertions

        PrimitiveAssertion primitiveAssertion0 = new PrimitiveAssertion();
        primitiveAssertion0.setSource(methodStatement0);
        primitiveAssertion0.setValue(true);
        Statement currentStatement = testCase.getStatement(5);
        currentStatement.addAssertion(primitiveAssertion0);

        return testCase;
    }
}
