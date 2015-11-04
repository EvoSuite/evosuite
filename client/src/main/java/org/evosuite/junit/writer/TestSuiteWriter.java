/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 *
 */
package org.evosuite.junit.writer;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.evosuite.Properties;
import org.evosuite.Properties.AssertionStrategy;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.OutputGranularity;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.dataflow.DefUseCoverageTestFitness;
import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.input.InputCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.coverage.output.OutputCoverageTestFitness;
import org.evosuite.idNaming.TestNameGenerator;
import org.evosuite.junit.UnitTestAdapter;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.runtime.EvoAssertions;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.evosuite.runtime.LoopCounter;
import org.evosuite.runtime.testdata.EnvironmentDataList;
import org.evosuite.testcase.*;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.statements.FunctionalMockStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.Utils;
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
    protected final static Logger logger = LoggerFactory.getLogger(TestSuiteWriter.class);

    protected TestCaseExecutor executor = TestCaseExecutor.getInstance();

    protected List<TestCase> testCases = new ArrayList<TestCase>();

    protected Map<Integer, String> testComment = new HashMap<Integer, String>();

    private final UnitTestAdapter adapter = TestSuiteWriterUtils.getAdapter();

    private TestCodeVisitor visitor = Properties.ASSERTION_STRATEGY == AssertionStrategy.STRUCTURED ? visitor = new StructuredTestCodeVisitor()
            : new TestCodeVisitor();

    private final Map<String, Integer> testMethodNumber = new HashMap<String, Integer>();

    private final static String NEWLINE = System.getProperty("line.separator");

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
     * @param optimizeIDNaming Optimize identifier names or not
     */
    public List<File> writeTestSuite(String name, String directory, boolean optimizeIDNaming) throws IllegalArgumentException {
        // Argument optimizeIDNaming needed to ensure optimized names are only generated
        // in the last call to this method

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Empty test class name");
        }
        if (!name.endsWith("Test")) {
            /*
             * This is VERY important, as otherwise tests can get ignored by "mvn test"
             */
            throw new IllegalArgumentException("Test classes should have name ending with 'Test'. Invalid input name: " + name);
        }

        List<File> generated = new ArrayList<File>();
        String dir = TestSuiteWriterUtils.makeDirectory(directory);
        String content = "";

        // Execute all tests
        executor.newObservers();
        List<ExecutionResult> results = new ArrayList<>();
        for (int i = 0; i < testCases.size(); i++) {
            LoopCounter.getInstance().setActive(true); //be sure it is active here, as JUnit checks might have left it to false
            ExecutionResult result = runTest(testCases.get(i));
            results.add(result);
        }
        
        if (Properties.ID_NAMING && optimizeIDNaming) {
            TestNameGenerator.getInstance().execute(testCases,results);
        }

        if (Properties.OUTPUT_GRANULARITY == OutputGranularity.MERGED) {
            File file = new File(dir + "/" + name + ".java");
            content = getUnitTestsAllInSameFile(name, results);
            Utils.writeFile(content, file);
            generated.add(file);
        } else {
            for (int i = 0; i < testCases.size(); i++) {
                String testSuiteName = name.substring(0, name.length() - "Test".length()) + "_" + i + "_Test";
                File file = new File(dir + "/" + testSuiteName + ".java");
                String testCode = getOneUnitTestInAFile(name, i, results);
                Utils.writeFile(testCode, file);
                content += testCode;
                generated.add(file);
            }
        }

        if (Properties.TEST_SCAFFOLDING) {
            String scaffoldingName = Scaffolding.getFileName(name);
            File file = new File(dir + "/" + scaffoldingName + ".java");
            String scaffoldingContent = Scaffolding.getScaffoldingFileContent(name, results,
                    TestSuiteWriterUtils.hasAnySecurityException(results));
            Utils.writeFile(scaffoldingContent, file);
            generated.add(file);
            content += scaffoldingContent;
        }

        TestGenerationResultBuilder.getInstance().setTestSuiteCode(content);
        return generated;
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

        builder.append(getHeader(name, results));

        if (!Properties.TEST_SCAFFOLDING) {
            builder.append(new Scaffolding().getBeforeAndAfterMethods(name, wasSecurityException, results));
        }

        for (int i = 0; i < testCases.size(); i++) {
            builder.append(testToString(i, i, results.get(i)));
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

        builder.append(getHeader(name + "_" + testId, results));

        if (!Properties.TEST_SCAFFOLDING) {
            builder.append(new Scaffolding().getBeforeAndAfterMethods(name, wasSecurityException, results));
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
        Set<Class<?>> imports = new HashSet<Class<?>>();
        Set<Class<?>> accessedClasses = new HashSet<Class<?>>();
        boolean wasSecurityException = TestSuiteWriterUtils.hasAnySecurityException(results);
        boolean hasException = false;

        for (ExecutionResult result : results) {
        	visitor.clearExceptions();
        	visitor.setExceptions(result.exposeExceptionMapping());
            result.test.accept(visitor);
            imports.addAll(visitor.getImports());
            accessedClasses.addAll(result.test.getAccessedClasses());
            if(!hasException)
            	hasException = !result.noThrownExceptions();
        }
        visitor.clearExceptions();

        if(doesUseMocks(results)){
            String mockito = Mockito.class.getCanonicalName();
            builder.append("import static "+mockito+".*;"+NEWLINE);
        }

        if(hasException) {
        	builder.append("import static "+ EvoAssertions.class.getCanonicalName()+".*;"+NEWLINE);
        }

        if (Properties.RESET_STANDARD_STREAMS) {
            imports.add(PrintStream.class);
            imports.add(DebugGraphics.class);
        }

        if (TestSuiteWriterUtils.needToUseAgent()) {
            imports.add(EvoRunner.class);
            imports.add(EvoRunnerParameters.class);
            imports.add(RunWith.class);
        }

        Set<String> importNames = new HashSet<String>();
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
        	if(accessedClasses.contains(klass))
        		importNames.add(klass.getCanonicalName());
        }

        if (wasSecurityException) {
            //Add import info for EvoSuite classes used in the generated test suite
            importNames.add(java.util.concurrent.ExecutorService.class.getCanonicalName());
            importNames.add(java.util.concurrent.Executors.class.getCanonicalName());
            importNames.add(java.util.concurrent.Future.class.getCanonicalName());
            importNames.add(java.util.concurrent.TimeUnit.class.getCanonicalName());
        }

        if (!Properties.TEST_SCAFFOLDING) {
            importNames.addAll(Scaffolding.getScaffoldingImports(wasSecurityException, results));
        }

        List<String> importsSorted = new ArrayList<String>(importNames);

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

    private boolean doesUseMocks(List<ExecutionResult> results) {
        for(ExecutionResult er : results){
            for(Statement st : er.test){
                if(st instanceof FunctionalMockStatement){
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * JUnit file header
     *
     * @param name    a {@link java.lang.String} object.
     * @param results a {@link java.util.List} object.
     * @return a {@link java.lang.String} object.
     */
    protected String getHeader(String name, List<ExecutionResult> results) {
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

        if (TestSuiteWriterUtils.needToUseAgent()) {
            builder.append(getRunner());
        }

        builder.append(adapter.getClassDefinition(name));

        if (Properties.TEST_SCAFFOLDING) {
            builder.append(" extends " + Scaffolding.getFileName(name));
        }

        builder.append(" {");
        builder.append(NEWLINE);
        return builder.toString();
    }

    private Object getRunner() {

        String s = "@RunWith(EvoRunner.class) @EvoRunnerParameters(";
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

        if(Properties.JEE){
            list.add("useJEE = true");
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

        StringBuilder builder = new StringBuilder();
        builder.append(NEWLINE);
        if (Properties.TEST_COMMENTS || testComment.containsKey(id)) {
            builder.append(METHOD_SPACE);
            builder.append("//");
            builder.append(getInformation(id));
            builder.append(NEWLINE);
        }

        // Get the test method name generated in TestNameGenerator
        String methodName = TestNameGenerator.getInstance().getNameGeneratedFor(testCases.get(id));

        if (Properties.ASSERTION_STRATEGY == AssertionStrategy.STRUCTURED) {
            StructuredTestCase structuredTest = (StructuredTestCase) testCases.get(id);
            String targetMethod = structuredTest.getTargetMethods().iterator().next();
            targetMethod = targetMethod.replace("<init>", "Constructor");
            if (targetMethod.indexOf('(') != -1)
                targetMethod = targetMethod.substring(0, targetMethod.indexOf('('));
            targetMethod = StringUtils.capitalize(targetMethod);
            int num = 0;
            if (testMethodNumber.containsKey(targetMethod)) {
                num = testMethodNumber.get(targetMethod);
                testMethodNumber.put(targetMethod, num + 1);
            } else {
                testMethodNumber.put(targetMethod, 1);
            }
            if (methodName == null) {
                // if TestNameGenerator did not generate a name, fall back to original naming
                methodName = "test" + targetMethod + num;
            }
            builder.append(adapter.getMethodDefinition(methodName));
        } else {
            if (methodName == null) {
                // if TestNameGenerator did not generate a name, fall back to original naming
                methodName = TestSuiteWriterUtils.getNameOfTest(testCases, number);
            }
            builder.append(adapter.getMethodDefinition(methodName));
        }

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
        if (Properties.ID_NAMING) {
            List<String> namesWithExceptions = new ArrayList<String>();

            String newMethodName = TestNameGenerator.getInstance().checkExeptionInTest(testCode, methodName);
            String[] tokens = newMethodName.split("_");

            newMethodName = tokens[0];
            for (int i = 1; i < tokens.length; i++) {
                if (i == tokens.length - 1) {
                    if (tokens[i].contains("Exception")) {
                        //newMethodName += "Throwing" + WordUtils.capitalize(tokens[i]);
                    	newMethodName += WordUtils.capitalize(tokens[i]);
                    } else {
                        newMethodName += WordUtils.capitalize(tokens[i]);
                    }
                } else {
                    newMethodName += WordUtils.capitalize(tokens[i]);
                }

            }
            builder.replace(builder.indexOf(methodName), builder.indexOf("()  throws Throwable  {"), newMethodName);
            testCode = builder.toString();
        }
        TestGenerationResultBuilder.getInstance().setTestCase(methodName, testCode, test,
                getInformation(id), result);
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
                builder.append("   * " + "Goal " + nr + ". " + getGoalPrefix(goal) + goal.toString());
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

    private String getGoalPrefix(TestFitnessFunction f) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (f instanceof MethodCoverageTestFitness)
            builder.append(Criterion.METHOD.toString());
        else if (f instanceof BranchCoverageTestFitness)
            builder.append(Criterion.BRANCH.toString());
        else if (f instanceof InputCoverageTestFitness)
            builder.append(Criterion.INPUT.toString());
        else if (f instanceof OutputCoverageTestFitness)
            builder.append(Criterion.OUTPUT.toString());
        else if (f instanceof ExceptionCoverageTestFitness)
            builder.append(Criterion.EXCEPTION.toString());

        builder.append("] ");
        return builder.toString();
    }


}
