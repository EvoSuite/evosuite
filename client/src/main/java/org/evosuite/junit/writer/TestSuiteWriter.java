/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package org.evosuite.junit.writer;

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.OutputGranularity;
import org.evosuite.TimeController;
import org.evosuite.coverage.dataflow.DefUseCoverageTestFitness;
import org.evosuite.junit.UnitTestAdapter;
import org.evosuite.junit.naming.methods.CoverageGoalTestNameGenerationStrategy;
import org.evosuite.junit.naming.methods.NumberedTestNameGenerationStrategy;
import org.evosuite.junit.naming.methods.TestNameGenerationStrategy;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.runtime.*;
import org.evosuite.runtime.testdata.EnvironmentDataList;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.FileIOUtils;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.PrintStream;
import java.util.*;

import static org.evosuite.junit.writer.TestSuiteWriterUtils.*;

/**
 * Class used to generate the source code of the JUnit test cases.
 * <p/>
 * <p/>
 * NOTE: a test case should only access to the following packages
 * <ul>
 * <li> Java API
 * <li> Junit
 * <li> org.evosuite.runtime.*
 *
 * @author Gordon Fraser
 */
public class TestSuiteWriter implements Opcodes {

    /**
     * Constant <code>logger</code>
     */
    private final static Logger logger = LoggerFactory.getLogger(TestSuiteWriter.class);

    public static final String NOT_GENERATED_TEST_NAME = "notGeneratedAnyTest";

    protected TestCaseExecutor executor = TestCaseExecutor.getInstance();

    protected List<TestCase> testCases = new ArrayList<>();

    protected Map<Integer, String> testComment = new HashMap<>();

    private final UnitTestAdapter adapter = TestSuiteWriterUtils.getAdapter();

    private final TestCodeVisitor visitor = new TestCodeVisitor();

    private final static String NEWLINE = java.lang.System.getProperty("line.separator");

    private TestNameGenerationStrategy nameGenerator = null;

    /**
     * Add test to suite. If the test is a prefix of an existing test, just keep
     * existing test. If an existing test is a prefix of the test, replace the
     * existing test.
     *
     * @param test a {@link org.evosuite.testcase.TestCase} object.
     * @return Index of the test case
     */
    public int insertTest(TestCase test) {
        if (Properties.CALL_PROBABILITY <= 0) {
            for (int i = 0; i < testCases.size(); i++) {
                if (test.isPrefix(testCases.get(i))) {
                    // It's shorter than an existing one
                    // test_cases.set(i, test);
                    logger.info("This is a prefix of an existing test");
                    testCases.get(i).addAssertions(test);
                    return i;
                } else {
                    // Already have that one...
                    if (testCases.get(i).isPrefix(test)) {
                        test.addAssertions(testCases.get(i));
                        testCases.set(i, test);
                        logger.info("We have a prefix of this one");
                        return i;
                    }
                }
            }
        }
        logger.info("Adding new test case:");
        if (logger.isDebugEnabled()) {
            logger.debug(test.toCode());
        }
        testCases.add(test);
        return testCases.size() - 1;
    }

    /**
     * <p>
     * insertTest
     * </p>
     *
     * @param test    a {@link org.evosuite.testcase.TestCase} object.
     * @param comment a {@link java.lang.String} object.
     * @return a int.
     */
    public int insertTest(TestCase test, String comment) {
        int id = insertTest(test);
        if (testComment.containsKey(id)) {
            if (!testComment.get(id).contains(comment))
                testComment.put(id, testComment.get(id) + NEWLINE + METHOD_SPACE + "//"
                        + comment);
        } else
            testComment.put(id, comment);
        return id;
    }

    /**
     * <p>
     * insertTests
     * </p>
     *
     * @param tests a {@link java.util.List} object.
     */
    public void insertTests(List<TestCase> tests) {
        for (TestCase test : tests)
            insertTest(test);
    }

    /**
     * <p>
     * insertTests
     * </p>
     *
     * @param tests a {@link java.util.List} object.
     */
    public void insertAllTests(List<TestCase> tests) {
        testCases.addAll(tests);
    }

    /**
     * Get all test cases
     *
     * @return a {@link java.util.List} object.
     */
    public List<TestCase> getTestCases() {
        return testCases;
    }

    /**
     * Create JUnit test suite for class
     *
     * @param name      Name of the class
     * @param directory Output directory
     */
    public List<File> writeTestSuite(String name, String directory, List<ExecutionResult> cachedResults) throws IllegalArgumentException {

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Empty test class name");
        }
        if (!name.endsWith("Test")) {
            /*
             * This is VERY important, as otherwise tests can get ignored by "mvn test"
             */
            throw new IllegalArgumentException("Test classes should have name ending with 'Test'. Invalid input name: " + name);
        }

        List<File> generated = new ArrayList<>();
        String dir = TestSuiteWriterUtils.makeDirectory(directory);
        String content = "";

        // Execute all tests
        executor.newObservers();
        LoopCounter.getInstance().setActive(true); //be sure it is active here, as JUnit checks might have left it to false

        List<ExecutionResult> results = new ArrayList<>();
        for (TestCase test : testCases) {
            boolean added = false;
            if (!TimeController.getInstance().hasTimeToExecuteATestCase()) {
                logger.info("Using cached result");
                for (ExecutionResult result : cachedResults) {
                    if (result != null && result.test == test) {
                        results.add(result);
                        added = true;
                        break;
                    }
                }
            }
            if (!added) {
                ExecutionResult result = runTest(test);
                results.add(result);
            }
        }

        if (Properties.TEST_NAMING_STRATEGY == Properties.TestNamingStrategy.NUMBERED) {
            nameGenerator = new NumberedTestNameGenerationStrategy(testCases, results);
        } else if (Properties.TEST_NAMING_STRATEGY == Properties.TestNamingStrategy.COVERAGE) {
            nameGenerator = new CoverageGoalTestNameGenerationStrategy(testCases, results);
        } else {
            throw new RuntimeException("Unsupported naming strategy: " + Properties.TEST_NAMING_STRATEGY);
        }

        // Avoid downcasts that could break
        removeUnnecessaryDownCasts(results);

        // Sometimes some timeouts lead to assertions being attached to statements
        // related to exceptions. This is not currently handled, so as a workaround
        // let's try to remove any remaining assertions. TODO: Better solution
        removeAssertionsAfterException(results);


        if (Properties.OUTPUT_GRANULARITY == OutputGranularity.MERGED || testCases.size() == 0) {
            File file = new File(dir + "/" + name + ".java");
            //executor.newObservers();
            content = getUnitTestsAllInSameFile(name, results);
            FileIOUtils.writeFile(content, file);
            generated.add(file);
        } else {
            for (int i = 0; i < testCases.size(); i++) {
                File file = new File(dir + "/" + name + "_" + i + ".java"); // e.g., dir/Foo_ESTest_0.java
                //executor.newObservers();
                String testCode = getOneUnitTestInAFile(name, i, results);
                FileIOUtils.writeFile(testCode, file);
                content += testCode;
                generated.add(file);
            }
        }

        if (Properties.TEST_SCAFFOLDING && !Properties.NO_RUNTIME_DEPENDENCY) {
            String scaffoldingName = Scaffolding.getFileName(name);
            File file = new File(dir + "/" + scaffoldingName + ".java");
            String scaffoldingContent = Scaffolding.getScaffoldingFileContent(name, results,
                    TestSuiteWriterUtils.hasAnySecurityException(results));
            FileIOUtils.writeFile(scaffoldingContent, file);
            generated.add(file);
            content += scaffoldingContent;
        }

        writeCoveredGoalsFile();

        TestGenerationResultBuilder.getInstance().setTestSuiteCode(content);
        return generated;
    }

    /**
     * To avoid having completely empty test classes, a no-op test is created
     *
     * @return
     */
    private String getEmptyTest() {
        StringBuilder bd = new StringBuilder();
        bd.append(METHOD_SPACE);
        bd.append("@Test\n");
        bd.append(METHOD_SPACE);
        bd.append("public void " + NOT_GENERATED_TEST_NAME + "() {\n");
        bd.append(BLOCK_SPACE);
        bd.append("// EvoSuite did not generate any tests\n");
        bd.append(METHOD_SPACE);
        bd.append("}\n");
        return bd.toString();
    }

    private void removeUnnecessaryDownCasts(List<ExecutionResult> results) {
        for (ExecutionResult result : results) {
            if (result.test instanceof DefaultTestCase) {
                ((DefaultTestCase) result.test).removeDownCasts();
            }
        }
    }

    private void removeAssertionsAfterException(List<ExecutionResult> results) {
        for (ExecutionResult result : results) {
            if (result.noThrownExceptions())
                continue;
            int exceptionPosition = result.getFirstPositionOfThrownException();
            // TODO: Not clear how that can happen...
            if (result.test.size() > exceptionPosition)
                result.test.getStatement(exceptionPosition).removeAssertions();
        }
    }


    /**
     * Create JUnit file for given class name
     *
     * @param name Name of the class file
     * @return String representation of JUnit test file
     */
    private String getUnitTestsAllInSameFile(String name, List<ExecutionResult> results) {

        /*
         * if there was any security exception, then we need to scaffold the
         * test cases with a sandbox
         */
        boolean wasSecurityException = TestSuiteWriterUtils.hasAnySecurityException(results);

        StringBuilder builder = new StringBuilder();

        builder.append(getHeader(name, name, results));

        if (!Properties.TEST_SCAFFOLDING && !Properties.NO_RUNTIME_DEPENDENCY) {
            builder.append(new Scaffolding().getBeforeAndAfterMethods(name, wasSecurityException, results));
        }

        if (testCases.isEmpty()) {
            builder.append(getEmptyTest());
        } else {
            for (int i = 0; i < testCases.size(); i++) {
                builder.append(testToString(i, i, results.get(i)));
            }
        }
        builder.append(getFooter());

        return builder.toString();
    }

    /**
     * Create JUnit file for given class name
     *
     * @param name   Name of the class file
     * @param testId a int.
     * @return String representation of JUnit test file
     */
    private String getOneUnitTestInAFile(String name, int testId, List<ExecutionResult> results) {

        boolean wasSecurityException = results.get(testId).hasSecurityException();

        StringBuilder builder = new StringBuilder();

        builder.append(getHeader(name + "_" + testId, name, results));

        if (!Properties.TEST_SCAFFOLDING) {
            builder.append(new Scaffolding().getBeforeAndAfterMethods(name + "_" + testId, wasSecurityException, results));
        }

        builder.append(testToString(testId, testId, results.get(testId)));
        builder.append(getFooter());

        return builder.toString();
    }

    /**
     * <p>
     * runTest
     * </p>
     *
     * @param test a {@link org.evosuite.testcase.TestCase} object.
     * @return a {@link org.evosuite.testcase.execution.ExecutionResult} object.
     */
    protected ExecutionResult runTest(TestCase test) {

        ExecutionResult result = new ExecutionResult(test, null);

        try {
            logger.debug("Executing test");
            result = executor.execute(test);
        } catch (Exception e) {
            throw new Error(e);
        }

        return result;
    }


    // -----------------------------------------------------------
    // --------------   code generation methods ------------------
    // -----------------------------------------------------------


    /**
     * Determine packages that need to be imported in the JUnit file
     *
     * @param results a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    protected String getImports(List<ExecutionResult> results) {
        StringBuilder builder = new StringBuilder();
        Set<Class<?>> imports = new HashSet<>();
        Set<Class<?>> accessedClasses = new HashSet<>();
        boolean wasSecurityException = TestSuiteWriterUtils.hasAnySecurityException(results);
        boolean hasException = false;

        for (ExecutionResult result : results) {
            visitor.clearExceptions();
            visitor.setExceptions(result.exposeExceptionMapping());
            result.test.accept(visitor);
            imports.addAll(visitor.getImports());
            accessedClasses.addAll(result.test.getAccessedClasses());
            if (!hasException)
                hasException = !result.noThrownExceptions();
        }
        visitor.clearExceptions();

        if (doesUseMocks(results)) {
            String mockito = Mockito.class.getCanonicalName();
            builder.append("import static " + mockito + ".*;" + NEWLINE);
            // MockitoExtension is now deprecated
            //String extension = MockitoExtension.class.getCanonicalName();
            //builder.append("import static "+extension+".*;"+NEWLINE);
            imports.add(ViolatedAssumptionAnswer.class);
        }

        if (hasException && !Properties.NO_RUNTIME_DEPENDENCY) {
            builder.append("import static " + EvoAssertions.class.getCanonicalName() + ".*;" + NEWLINE);
        }

        if (Properties.RESET_STANDARD_STREAMS) {
            imports.add(PrintStream.class);
            imports.add(DebugGraphics.class);
        }

        if (TestSuiteWriterUtils.needToUseAgent() && !Properties.NO_RUNTIME_DEPENDENCY) {
            imports.add(EvoRunnerParameters.class);
            if (Properties.TEST_FORMAT == Properties.OutputFormat.JUNIT5) {
                imports.add(EvoRunnerJUnit5.class);
                imports.add(RegisterExtension.class);
            } else {
                imports.add(RunWith.class);
                imports.add(EvoRunner.class);
            }
        }

        Set<String> importNames = new HashSet<>();
        for (Class<?> imp : imports) {
            while (imp.isArray())
                imp = imp.getComponentType();
            if (imp.isPrimitive())
                continue;
            if (imp.getName().startsWith("java.lang")) {
                String name = imp.getName().replace("java.lang.", "");
                if (!name.contains("."))
                    continue;
            }
            if (!imp.getName().contains("."))
                continue;
            // TODO: Check for anonymous type?
            if (imp.getName().contains("$"))
                importNames.add(imp.getName().replace("$", "."));
            else
                importNames.add(imp.getName());
        }

        for (Class<?> klass : EnvironmentDataList.getListOfClasses()) {
            //TODO: not paramount, but best if could check if actually used in the test suite
            if (accessedClasses.contains(klass))
                importNames.add(klass.getCanonicalName());
        }

        if (wasSecurityException) {
            //Add import info for EvoSuite classes used in the generated test suite
            importNames.add(java.util.concurrent.ExecutorService.class.getCanonicalName());
            importNames.add(java.util.concurrent.Executors.class.getCanonicalName());
            importNames.add(java.util.concurrent.Future.class.getCanonicalName());
            importNames.add(java.util.concurrent.TimeUnit.class.getCanonicalName());
        }

        if (!Properties.TEST_SCAFFOLDING && !Properties.NO_RUNTIME_DEPENDENCY) {
            importNames.addAll(Scaffolding.getScaffoldingImports(wasSecurityException, results));
        }

        // If a CodeUnderTestException happens, the test will be chopped before that exception
        // but it would still be in the imports
        importNames.remove(CodeUnderTestException.class.getCanonicalName());

        List<String> importsSorted = new ArrayList<>(importNames);

        Collections.sort(importsSorted);
        for (String imp : importsSorted) {
            builder.append("import ");
            builder.append(imp);
            builder.append(";");
            builder.append(NEWLINE);
        }

        builder.append(NEWLINE);

        return builder.toString();
    }


    /**
     * JUnit file header
     *
     * @param test_name        a {@link java.lang.String} object.
     * @param scaffolding_name a {@link java.lang.String} object.
     * @param results          a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    protected String getHeader(String test_name, String scaffolding_name, List<ExecutionResult> results) {
        StringBuilder builder = new StringBuilder();
        builder.append("/*");
        builder.append(NEWLINE);
        builder.append(" * This file was automatically generated by EvoSuite");
        builder.append(NEWLINE);
        builder.append(" * " + new Date());
        builder.append(NEWLINE);
        builder.append(" */");
        builder.append(NEWLINE);
        builder.append(NEWLINE);

        if (!Properties.CLASS_PREFIX.equals("")) {
            builder.append("package ");
            builder.append(Properties.CLASS_PREFIX);
            builder.append(";");
            builder.append(NEWLINE);
        }
        builder.append(NEWLINE);

        builder.append(adapter.getImports());
        builder.append(getImports(results));

        if (TestSuiteWriterUtils.needToUseAgent() && !Properties.NO_RUNTIME_DEPENDENCY) {
            builder.append(getRunner());
        }

        builder.append(adapter.getClassDefinition(test_name));

        if (Properties.TEST_SCAFFOLDING && !Properties.NO_RUNTIME_DEPENDENCY) {
            builder.append(" extends ").append(Scaffolding.getFileName(scaffolding_name));
        }

        builder.append(" {");
        builder.append(NEWLINE);
        if (Properties.TEST_FORMAT == Properties.OutputFormat.JUNIT5) {
            builder.append("@RegisterExtension").append(NEWLINE);
            builder.append(METHOD_SPACE).append("static EvoRunnerJUnit5 runner = new EvoRunnerJUnit5(").append(test_name).append(".class);").append(NEWLINE);
        }
        return builder.toString();
    }

    private Object getRunner() {


        String s = Properties.TEST_FORMAT == Properties.OutputFormat.JUNIT5 ? "@EvoRunnerParameters("
                : "@RunWith(EvoRunner.class) @EvoRunnerParameters(";
        List<String> list = new ArrayList<>();

        if (Properties.REPLACE_CALLS) {
            list.add("mockJVMNonDeterminism = true");
        }

        if (Properties.VIRTUAL_FS) {
            list.add("useVFS = true");
        }

        if (Properties.VIRTUAL_NET) {
            list.add("useVNET = true");
        }

        if (Properties.RESET_STATIC_FIELDS) {
            list.add("resetStaticState = true");
        }

        if (Properties.USE_SEPARATE_CLASSLOADER) {
            list.add("separateClassLoader = true");
        }

        if (Properties.REPLACE_GUI) {
            list.add("mockGUI = true");
        }

        if (!list.isEmpty()) {
            s += list.get(0);

            for (int i = 1; i < list.size(); i++) {
                s += ", " + list.get(i);
            }
        }

        s += ") " + NEWLINE;

        return s;
    }

    /**
     * JUnit file footer
     *
     * @return a {@link java.lang.String} object.
     */
    protected String getFooter() {
        return "}" + NEWLINE;
    }


    /**
     * Convert one test case to a Java method
     *
     * @param id     Index of the test case
     * @param result a {@link org.evosuite.testcase.execution.ExecutionResult} object.
     * @return String representation of test case
     */
    protected String testToString(int number, int id, ExecutionResult result) {

        boolean wasSecurityException = result.hasSecurityException();

        String testInfo = getInformation(id);

        StringBuilder builder = new StringBuilder();
        builder.append(NEWLINE);
        if (Properties.TEST_COMMENTS || testComment.containsKey(id)) {
            builder.append(METHOD_SPACE);
            builder.append("//");
            builder.append(testInfo);
            builder.append(NEWLINE);
        }

        // Get the test method name generated in TestNameGenerator
        String methodName = nameGenerator.getName(testCases.get(id));
        if (methodName == null) {
            // if TestNameGenerator did not generate a name, fall back to original naming
            methodName = TestSuiteWriterUtils.getNameOfTest(testCases, number);
        }
        builder.append(adapter.getMethodDefinition(methodName));

        /*
         * A test case might throw a lot of different kinds of exceptions.
         * These might come from SUT, and might also come from the framework itself (eg, see ExecutorService.submit).
         * Regardless of whether they are declared or not, an exception that propagates to the JUnit framework will
         * result in a failure for the test case. However, there might be some checked exceptions, and for those
         * we need to declare them in the signature with "throws". So, the easiest (but still correct) option
         * is to just declare once to throw any generic Exception, and be done with it once and for all
         */
        builder.append(" throws Throwable ");
        builder.append(" {");
        builder.append(NEWLINE);

        // ---------   start with the body -------------------------
        String CODE_SPACE = INNER_BLOCK_SPACE;

        // No code after an exception should be printed as it would break compilability
        TestCase test = testCases.get(id);

        Integer pos = result.getFirstPositionOfThrownException();
        if (pos != null) {
            if (result.getExceptionThrownAtPosition(pos) instanceof CodeUnderTestException) {
                test.chop(pos);
            } else {
                test.chop(pos + 1);
            }
        }

        if (wasSecurityException) {
            builder.append(BLOCK_SPACE);
            builder.append("Future<?> future = " + Scaffolding.EXECUTOR_SERVICE
                    + ".submit(new Runnable(){ ");
            builder.append(NEWLINE);
            builder.append(INNER_BLOCK_SPACE);
            builder.append(INNER_BLOCK_SPACE);
            builder.append("@Override public void run() { ");
            builder.append(NEWLINE);
            Set<Class<?>> exceptions = test.getDeclaredExceptions();
            if (!exceptions.isEmpty()) {
                builder.append(INNER_INNER_BLOCK_SPACE);
                builder.append("try {");
                builder.append(NEWLINE);
            }
            CODE_SPACE = INNER_INNER_INNER_BLOCK_SPACE;
        }

        for (String line : adapter.getTestString(id, test,
                result.exposeExceptionMapping(), visitor).split("\\r?\\n")) {
            builder.append(CODE_SPACE);
            builder.append(line);
            builder.append(NEWLINE);
        }

        if (wasSecurityException) {
            Set<Class<?>> exceptions = test.getDeclaredExceptions();
            if (!exceptions.isEmpty()) {
                builder.append(INNER_INNER_BLOCK_SPACE);
                builder.append("} catch(Throwable t) {");
                builder.append(NEWLINE);
                builder.append(INNER_INNER_INNER_BLOCK_SPACE);
                builder.append("  // Need to catch declared exceptions");
                builder.append(NEWLINE);
                builder.append(INNER_INNER_BLOCK_SPACE);
                builder.append("}");
                builder.append(NEWLINE);
            }

            builder.append(INNER_BLOCK_SPACE);
            builder.append("} "); //closing run(){
            builder.append(NEWLINE);
            builder.append(BLOCK_SPACE);
            builder.append("});"); //closing submit
            builder.append(NEWLINE);

            long time = Properties.TIMEOUT + 1000; // we add one second just to be sure, that to avoid issues with test cases taking exactly TIMEOUT ms
            builder.append(BLOCK_SPACE);
            builder.append("future.get(" + time + ", TimeUnit.MILLISECONDS);");
            builder.append(NEWLINE);
        }

        // ---------   end of the body ----------------------------

        builder.append(METHOD_SPACE);
        builder.append("}");
        builder.append(NEWLINE);

        String testCode = builder.toString();
        TestGenerationResultBuilder.getInstance().setTestCase(methodName, testCode, test,
                testInfo, result);
        return testCode;
    }

    /**
     * When writing out the JUnit test file, each test can have a text comment
     *
     * @param num Index of test case
     * @return Comment for test case
     */
    protected String getInformation(int num) {

        if (testComment.containsKey(num)) {
            String comment = testComment.get(num);
            if (!comment.endsWith("\n"))
                comment = comment + NEWLINE;
            return comment;
        }

        TestCase test = testCases.get(num);
        Set<TestFitnessFunction> coveredGoals = test.getCoveredGoals();

        StringBuilder builder = new StringBuilder();
        builder.append("Test case number: " + num);

        if (!coveredGoals.isEmpty()) {
            builder.append(NEWLINE);
            builder.append("  /*");
            builder.append(NEWLINE);
            builder.append("   * ");
            builder.append(coveredGoals.size() + " covered goal");
            if (coveredGoals.size() != 1)
                builder.append("s");
            builder.append(":");
            int nr = 1;
            for (TestFitnessFunction goal : coveredGoals) {
                builder.append(NEWLINE);
                builder.append("   * Goal " + nr + ". " + goal.toString());
                // TODO only for debugging purposes
                if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)
                        && (goal instanceof DefUseCoverageTestFitness)) {
                    DefUseCoverageTestFitness duGoal = (DefUseCoverageTestFitness) goal;
                    if (duGoal.getCoveringTrace() != null) {
                        String traceInformation = duGoal.getCoveringTrace().toDefUseTraceInformation(duGoal.getGoalVariable(),
                                duGoal.getCoveringObjectId());
                        traceInformation = traceInformation.replaceAll("\n", "");
                        builder.append(NEWLINE);
                        builder.append("     * DUTrace: " + traceInformation);
                    }
                }
                nr++;
            }

            builder.append(NEWLINE);
            builder.append("   */");
            builder.append(NEWLINE);
        }

        return builder.toString();
    }

    private void writeCoveredGoalsFile() {
        if (Properties.WRITE_COVERED_GOALS_FILE) {
            StringBuilder builder = new StringBuilder();
            File file = new File(Properties.COVERED_GOALS_FILE);
            for (int i = 0; i < testCases.size(); i++) {
                TestCase test = testCases.get(i);
                String generatedName = nameGenerator.getName(test);
                String testName = (generatedName != null) ? generatedName : TestSuiteWriterUtils.getNameOfTest(testCases, i);
                Set<TestFitnessFunction> coveredGoals = test.getCoveredGoals();
                for (TestFitnessFunction goal : coveredGoals) {
                    builder.append(testName + "," + goal.toString() + NEWLINE);
                }
            }
            FileIOUtils.writeFile(builder.toString(), file);
        }
    }
}
