/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.junit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DebugGraphics;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.evosuite.Properties;
import org.evosuite.Properties.AssertionStrategy;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.OutputFormat;
import org.evosuite.Properties.OutputGranularity;
import org.evosuite.coverage.dataflow.DefUseCoverageTestFitness;
import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.agent.InstrumentingAgent;
import org.evosuite.runtime.reset.ClassResetter;
import org.evosuite.runtime.reset.ResetManager;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.runtime.util.SystemInUtil;
import org.evosuite.testcase.CodeUnderTestException;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.StructuredTestCase;
import org.evosuite.testcase.StructuredTestCodeVisitor;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.Utils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *   Class used to generate the source code of the JUnit test cases.
 *   
 *   <p>
 *   NOTE: a test case should only access to the following packages
 *   <ul>
 *   <li> Java API
 *   <li> Junit
 *   <li> org.evosuite.runtime.*
 * 
 * @author Gordon Fraser
 */
public class TestSuiteWriter implements Opcodes {

	/** Constant <code>logger</code> */
	protected final static Logger logger = LoggerFactory.getLogger(TestSuiteWriter.class);

	protected TestCaseExecutor executor = TestCaseExecutor.getInstance();

	protected List<TestCase> testCases = new ArrayList<TestCase>();

	protected Map<Integer, String> testComment = new HashMap<Integer, String>();

	private final UnitTestAdapter adapter = TestSuiteWriter.getAdapter();

	private TestCodeVisitor visitor = Properties.ASSERTION_STRATEGY == AssertionStrategy.STRUCTURED ? visitor = new StructuredTestCodeVisitor()
	        : new TestCodeVisitor();

	private static final String METHOD_SPACE = "  ";
	private static final String BLOCK_SPACE = "    ";
	private static final String INNER_BLOCK_SPACE = "      ";
	private static final String INNER_INNER_BLOCK_SPACE = "        ";
	private static final String INNER_INNER_INNER_BLOCK_SPACE = "          ";

	private final String EXECUTOR_SERVICE = "executor";

	private final String DEFAULT_PROPERTIES = "defaultProperties";

	/**
	 * FIXME: this filter assumes "Test" as prefix, but would be better to have
	 * it as postfix (and as a variable)
	 * 
	 */
	class TestFilter implements IOFileFilter {
		@Override
		public boolean accept(File f, String s) {
			return s.toLowerCase().endsWith(".java") && s.startsWith("Test");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.commons.io.filefilter.IOFileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept(File file) {
			return file.getName().toLowerCase().endsWith(".java")
			        && file.getName().startsWith("Test");
		}
	}

	/**
	 * Check if there are test cases
	 * 
	 * @return True if there are no test cases
	 */
	public boolean isEmpty() {
		return testCases.isEmpty();
	}

	/**
	 * <p>
	 * size
	 * </p>
	 * 
	 * @return a int.
	 */
	public int size() {
		return testCases.size();
	}

	/**
	 * Check if test suite has a test case that is a prefix of test.
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @return a boolean.
	 */
	public boolean hasPrefix(TestCase test) {
		for (TestCase t : testCases) {
			if (t.isPrefix(test))
				return true;
		}
		return false;
	}

	/**
	 * Add test to suite. If the test is a prefix of an existing test, just keep
	 * existing test. If an existing test is a prefix of the test, replace the
	 * existing test.
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
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
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @param comment
	 *            a {@link java.lang.String} object.
	 * @return a int.
	 */
	public int insertTest(TestCase test, String comment) {
		int id = insertTest(test);
		if (testComment.containsKey(id)) {
			if (!testComment.get(id).contains(comment))
				testComment.put(id, testComment.get(id) + "\n" + METHOD_SPACE + "//"
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
	 * @param tests
	 *            a {@link java.util.List} object.
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
	 * @param tests
	 *            a {@link java.util.List} object.
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
	 * <p>
	 * runTest
	 * </p>
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @return a {@link org.evosuite.testcase.ExecutionResult} object.
	 */
	ExecutionResult runTest(TestCase test) {

		ExecutionResult result = new ExecutionResult(test, null);

		try {
			logger.debug("Executing test");
			result = executor.execute(test);
		} catch (Exception e) {
			throw new Error(e);
		}

		return result;
	}

	/**
	 * Create subdirectory for package in test directory
	 * 
	 * @param directory
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String makeDirectory(String directory) {
		String dirname = directory + File.separator
		        + Properties.CLASS_PREFIX.replace('.', File.separatorChar); // +"/GeneratedTests";
		File dir = new File(dirname);
		logger.debug("Target directory: " + dirname);
		dir.mkdirs();
		return dirname;
	}

	/**
	 * Create subdirectory for package in test directory
	 * 
	 * @param directory
	 *            a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String mainDirectory(String directory) {
		String dirname = directory + File.separator
		        + Properties.PROJECT_PREFIX.replace('.', File.separatorChar); // +"/GeneratedTests";
		File dir = new File(dirname);
		logger.debug("Target directory: " + dirname);
		dir.mkdirs();
		return dirname;
	}

	protected static boolean hasAnySecurityException(List<ExecutionResult> results) {
		for (ExecutionResult result : results) {
			if (result.hasSecurityException()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine packages that need to be imported in the JUnit file
	 * 
	 * @param results
	 *            a {@link java.util.List} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getImports(List<ExecutionResult> results) {
		StringBuilder builder = new StringBuilder();
		Set<Class<?>> imports = new HashSet<Class<?>>();
		boolean wasSecurityException = hasAnySecurityException(results);

		for (ExecutionResult result : results) {
			result.test.accept(visitor);

			// TODO: This should be unnecessary 
			// Iterate over declared exceptions to make sure they are known to the visitor
			/*
			Set<Class<?>> exceptions = result.test.getDeclaredExceptions();
			if (!exceptions.isEmpty()) {
				for (Class<?> exception : exceptions) {
					visitor.getClassName(exception);
				}
			}
			 */

			// Also include thrown exceptions
			for (Throwable t : result.getAllThrownExceptions()) {
				visitor.getClassName(t.getClass());
			}

			imports.addAll(visitor.getImports());
		}

		if (Properties.RESET_STANDARD_STREAMS) {
			imports.add(PrintStream.class);
			imports.add(DebugGraphics.class);
		}

		Set<String> import_names = new HashSet<String>();
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
				//	import_names.add(imp.getName().substring(0, imp.getName().indexOf("$")));
				import_names.add(imp.getName().replace("$", "."));
			else
				import_names.add(imp.getName());
		}
		List<String> imports_sorted = new ArrayList<String>(import_names);

		// FIXME: I disagree - it should be covered by the below branches
		//we always need this one, due to for example logging setup

		if (Properties.REPLACE_CALLS || Properties.VIRTUAL_FS
		        || Properties.RESET_STATIC_FIELDS || wasSecurityException
		        || SystemInUtil.getInstance().hasBeenUsed()) {
			imports_sorted.add(org.junit.BeforeClass.class.getCanonicalName());
			imports_sorted.add(org.junit.Before.class.getCanonicalName());
			imports_sorted.add(org.junit.After.class.getCanonicalName());
		}

		if (wasSecurityException || shouldResetProperties(results)) {
			imports_sorted.add(org.junit.AfterClass.class.getCanonicalName());
		}

		if (Properties.VIRTUAL_FS) {
			imports_sorted.add(org.evosuite.runtime.EvoSuiteFile.class.getCanonicalName());
		}

		if (wasSecurityException) {
			//Add import info for EvoSuite classes used in the generated test suite
			imports_sorted.add(Sandbox.class.getCanonicalName());
			// imports_sorted.add(Properties.class.getCanonicalName());
			imports_sorted.add(Sandbox.SandboxMode.class.getCanonicalName());
			imports_sorted.add(java.util.concurrent.ExecutorService.class.getCanonicalName());
			imports_sorted.add(java.util.concurrent.Executors.class.getCanonicalName());
			imports_sorted.add(java.util.concurrent.Future.class.getCanonicalName());
			imports_sorted.add(java.util.concurrent.TimeUnit.class.getCanonicalName());

		}

		Collections.sort(imports_sorted);
		for (String imp : imports_sorted) {
			builder.append("import ");
			builder.append(imp);
			builder.append(";\n");
		}
		builder.append("\n");
		return builder.toString();
	}

	/**
	 * When writing out the JUnit test file, each test can have a text comment
	 * 
	 * @param num
	 *            Index of test case
	 * @return Comment for test case
	 */
	protected String getInformation(int num) {

		if (testComment.containsKey(num)) {
			String comment = testComment.get(num);
			if (!comment.endsWith("\n"))
				comment = comment + "\n";
			return comment;
		}

		TestCase test = testCases.get(num);
		Set<TestFitnessFunction> coveredGoals = test.getCoveredGoals();

		StringBuilder builder = new StringBuilder();
		builder.append("Test case number: " + num);

		if (!coveredGoals.isEmpty()) {
			builder.append("\n  /*\n");
			builder.append("   * ");
			builder.append(coveredGoals.size() + " covered goal");
			if (coveredGoals.size() != 1)
				builder.append("s");
			builder.append(":");
			int nr = 1;
			for (TestFitnessFunction goal : coveredGoals) {
				builder.append("\n   * " + nr + " " + goal.toString());
				// TODO only for debugging purposes
				if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)
				        && (goal instanceof DefUseCoverageTestFitness)) {
					DefUseCoverageTestFitness duGoal = (DefUseCoverageTestFitness) goal;
					if (duGoal.getCoveringTrace() != null) {
						String traceInformation = duGoal.getCoveringTrace().toDefUseTraceInformation(duGoal.getGoalVariable(),
						                                                                             duGoal.getCoveringObjectId());
						traceInformation = traceInformation.replaceAll("\n", "");
						builder.append("\n     * DUTrace: " + traceInformation);
					}
				}
				nr++;
			}

			builder.append("\n   */\n");
		}

		return builder.toString();
	}

	private static UnitTestAdapter getAdapter() {
		if (Properties.TEST_FORMAT == OutputFormat.JUNIT3)
			return new JUnit3TestAdapter();
		else if (Properties.TEST_FORMAT == OutputFormat.JUNIT4)
			return new JUnit4TestAdapter();
		else
			throw new RuntimeException("Unknown output format: " + Properties.TEST_FORMAT);
	}

	/**
	 * JUnit file header
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @param results
	 *            a {@link java.util.List} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getHeader(String name, List<ExecutionResult> results) {
		StringBuilder builder = new StringBuilder();
		builder.append("/*\n");
		builder.append(" * This file was automatically generated by EvoSuite\n");
		builder.append(" */\n\n");

		if (!Properties.CLASS_PREFIX.equals("")) {
			builder.append("package ");
			builder.append(Properties.CLASS_PREFIX);
			builder.append(";\n");
		}
		builder.append("\n");

		builder.append(adapter.getImports());
		builder.append(getImports(results));

		builder.append(adapter.getClassDefinition(name));
		builder.append(" {\n");
		return builder.toString();
	}

	/**
	 * JUnit file footer
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	protected String getFooter() {
		return "}\n";
	}

	/**
	 * Create JUnit file for given class name
	 * 
	 * @param name
	 *            Name of the class file
	 * @return String representation of JUnit test file
	 */
	public String getUnitTest(String name) {
		List<ExecutionResult> results = new ArrayList<ExecutionResult>();

		for (int i = 0; i < testCases.size(); i++) {
			ExecutionResult result = runTest(testCases.get(i));
			results.add(result);
		}

		/*
		 * if there was any security exception, then we need to scaffold the
		 * test cases with a sandbox
		 */
		boolean wasSecurityException = hasAnySecurityException(results);

		StringBuilder builder = new StringBuilder();

		builder.append(getHeader(name, results));
		builder.append(getBeforeAndAfterMethods(name, wasSecurityException, results));

		for (int i = 0; i < testCases.size(); i++) {
			builder.append(testToString(i, i, results.get(i)));
		}
		builder.append(getFooter());

		return builder.toString();
	}

	/**
	 * Create JUnit file for given class name
	 * 
	 * @param name
	 *            Name of the class file
	 * @return String representation of JUnit test file
	 * @param testId
	 *            a int.
	 */
	public String getUnitTest(String name, int testId) {
		List<ExecutionResult> results = new ArrayList<ExecutionResult>();
		ExecutionResult result = runTest(testCases.get(testId));
		results.add(result);
		boolean wasSecurityException = result.hasSecurityException();

		StringBuilder builder = new StringBuilder();

		builder.append(getHeader(name + "_" + testId, results));
		builder.append(getBeforeAndAfterMethods(name, wasSecurityException, results));
		builder.append(testToString(testId, testId, results.get(0)));
		builder.append(getFooter());

		return builder.toString();
	}

	/**
	 * Get the code of methods for @BeforeClass, @Before, @AfterClass and
	 * 
	 * @After.
	 * 
	 *         <p>
	 *         In those methods, the EvoSuite framework for running the
	 *         generated test cases is handled (e.g., use of customized
	 *         SecurityManager and runtime bytecode replacement)
	 * 
	 * @return
	 */
	protected String getBeforeAndAfterMethods(String name, boolean wasSecurityException,
	        List<ExecutionResult> results) {

		/*
		 * Usually, we need support methods (ie @BeforeClass,@Before,@After and @AfterClass)
		 * only if there was a security exception (and so we need EvoSuite security manager,
		 * and test runs on separated thread) or if we are doing bytecode replacement (and
		 * so we need to activate JavaAgent).
		 * 
		 * But there are cases that we might always want: eg, setup logging
		 */

		StringBuilder bd = new StringBuilder("");
		bd.append("\n");

		/*
		 * Because this method is perhaps called only once per SUT,
		 * not much of the point to try to optimize it 
		 */

		generateFields(bd, wasSecurityException, results);

		generateBeforeClass(bd, wasSecurityException);

		generateAfterClass(bd, wasSecurityException, results);

		generateBefore(bd, wasSecurityException, results);

		generateAfter(bd, wasSecurityException);

		generateSetSystemProperties(bd, results);

		if (Properties.RESET_STATIC_FIELDS) {
			generateInitializeClasses(name, bd);

			generateResetClasses(bd);
		}

		return bd.toString();
	}

	private void generateResetClasses(StringBuilder bd) {
		List<String> classesToReset = ResetManager.getInstance().getClassResetOrder();

		bd.append(METHOD_SPACE);
		bd.append("private static void resetClasses() {\n");

		bd.append(BLOCK_SPACE);
		bd.append("String[] classNames = new String[" + classesToReset.size() + "];\n");

		for (int i = 0; i < classesToReset.size(); i++) {
			String className = classesToReset.get(i);
			bd.append(BLOCK_SPACE);
			bd.append(String.format("classNames[%s] =\"%s\";\n", i, className));
		}

		bd.append(BLOCK_SPACE);
		bd.append("for (int i=0; i< classNames.length;i++) {\n");

		bd.append(INNER_BLOCK_SPACE);
		bd.append("String classNameToReset = classNames[i];\n");

		bd.append(INNER_BLOCK_SPACE);
		bd.append("try {" + "\n");

		bd.append(INNER_INNER_BLOCK_SPACE);
		bd.append(ClassResetter.class.getCanonicalName()
		        + ".getInstance().reset(classNameToReset); \n");

		bd.append(INNER_BLOCK_SPACE);
		bd.append("} catch (Throwable t) {" + "\n");

		bd.append(INNER_BLOCK_SPACE);
		bd.append("}\n");

		bd.append(BLOCK_SPACE);
		bd.append("}\n");

		bd.append(METHOD_SPACE);
		bd.append("}" + "\n");

	}

	private void generateInitializeClasses(String testClassName, StringBuilder bd) {

		List<String> classesToBeReset = ResetManager.getInstance().getClassResetOrder();
		bd.append(METHOD_SPACE);
		bd.append("private static void initializeClasses() {\n");

		bd.append(BLOCK_SPACE);
		bd.append("String[] classNames = new String[" + classesToBeReset.size() + "];\n");

		for (int i = 0; i < classesToBeReset.size(); i++) {
			String className = classesToBeReset.get(i);
			if (BytecodeInstrumentation.checkIfCanInstrument(className)) {
				bd.append(BLOCK_SPACE);
				bd.append(String.format("classNames[%s] =\"%s\";\n", i, className));
			}
		}

		if (Properties.REPLACE_CALLS || Properties.VIRTUAL_FS
		        || Properties.RESET_STATIC_FIELDS) {
			bd.append(BLOCK_SPACE);
			bd.append(InstrumentingAgent.class.getName()+".activate(); \n");
		}

		bd.append(BLOCK_SPACE);
		bd.append("for (int i=0; i< classNames.length;i++) {\n");

		if (Properties.REPLACE_CALLS || Properties.VIRTUAL_FS
		        || Properties.RESET_STATIC_FIELDS) {
			bd.append(INNER_BLOCK_SPACE);
			bd.append(org.evosuite.runtime.Runtime.class.getName()+".getInstance().resetRuntime(); \n");
		}

		bd.append(INNER_BLOCK_SPACE);
		bd.append("String classNameToLoad = classNames[i];\n");

		bd.append(INNER_BLOCK_SPACE);
		bd.append("ClassLoader classLoader = " + testClassName
		        + ".class.getClassLoader();\n");

		bd.append(INNER_BLOCK_SPACE);
		bd.append("try {" + "\n");

		bd.append(INNER_INNER_BLOCK_SPACE);
		bd.append("Class.forName(classNameToLoad, true, classLoader);\n");

		bd.append(INNER_BLOCK_SPACE);
		bd.append("} catch (ExceptionInInitializerError ex) {" + "\n");

		bd.append(INNER_INNER_BLOCK_SPACE);
		bd.append("java.lang.System.err.println(\"Could not initialize \" + classNameToLoad);\n");

		bd.append(INNER_BLOCK_SPACE);
		bd.append("} catch (Throwable t) {" + "\n");

		bd.append(INNER_BLOCK_SPACE);
		bd.append("}\n");

		bd.append(BLOCK_SPACE);
		bd.append("}\n");

		if (Properties.REPLACE_CALLS || Properties.VIRTUAL_FS
		        || Properties.RESET_STATIC_FIELDS) {
			bd.append(BLOCK_SPACE);
			bd.append(InstrumentingAgent.class.getName()+".deactivate(); \n");
		}

		bd.append(METHOD_SPACE);
		bd.append("}" + "\n");

	}

	private void generateAfter(StringBuilder bd, boolean wasSecurityException) {

		if (!Properties.RESET_STANDARD_STREAMS && !wasSecurityException
		        && !Properties.REPLACE_CALLS && !Properties.VIRTUAL_FS
		        && !Properties.RESET_STATIC_FIELDS) {
			return;
		}

		bd.append(METHOD_SPACE);
		bd.append("@After \n");
		bd.append(METHOD_SPACE);
		bd.append("public void doneWithTestCase(){ \n");

		if (Properties.RESET_STANDARD_STREAMS) {
			bd.append(BLOCK_SPACE);
			bd.append("java.lang.System.setErr(systemErr); \n");

			bd.append(BLOCK_SPACE);
			bd.append("java.lang.System.setOut(systemOut); \n");

			bd.append(BLOCK_SPACE);
			bd.append("DebugGraphics.setLogStream(logStream); \n");
		}

		if (wasSecurityException) {
			bd.append(BLOCK_SPACE);
			bd.append(Sandbox.class.getName()+".doneWithExecutingSUTCode(); \n");
		}

		if (Properties.RESET_STATIC_FIELDS) {
			bd.append(BLOCK_SPACE);
			bd.append("resetClasses(); \n");
		}

		if (Properties.REPLACE_CALLS || Properties.VIRTUAL_FS
		        || Properties.RESET_STATIC_FIELDS) {
			bd.append(BLOCK_SPACE);
			bd.append(InstrumentingAgent.class.getName()+".deactivate(); \n");
		}

		bd.append(METHOD_SPACE);
		bd.append("} \n");

		bd.append("\n");
	}

	private void generateBefore(StringBuilder bd, boolean wasSecurityException,
	        List<ExecutionResult> results) {

		if (!Properties.RESET_STANDARD_STREAMS && !shouldResetProperties(results)
		        && !wasSecurityException && !Properties.REPLACE_CALLS
		        && !Properties.VIRTUAL_FS && !Properties.RESET_STATIC_FIELDS
		        && !SystemInUtil.getInstance().hasBeenUsed()) {
			return;
		}

		bd.append(METHOD_SPACE);
		bd.append("@Before \n");
		bd.append(METHOD_SPACE);
		bd.append("public void initTestCase(){ \n");

		if (Properties.RESET_STANDARD_STREAMS) {
			bd.append(BLOCK_SPACE);
			bd.append("systemErr = java.lang.System.err;");
			bd.append(" \n");

			bd.append(BLOCK_SPACE);
			bd.append("systemOut = java.lang.System.out;");
			bd.append(" \n");

			bd.append(BLOCK_SPACE);
			bd.append("logStream = DebugGraphics.logStream();");
			bd.append(" \n");
		}

		if (shouldResetProperties(results)) {
			bd.append(BLOCK_SPACE);
			bd.append("setSystemProperties();");
			bd.append(" \n");
		}

		if (wasSecurityException) {
			bd.append(BLOCK_SPACE);
			bd.append(Sandbox.class.getName()+".goingToExecuteSUTCode(); \n");
		}

		if (Properties.REPLACE_CALLS || Properties.VIRTUAL_FS
		        || Properties.RESET_STATIC_FIELDS) {
			bd.append(BLOCK_SPACE);
			bd.append(org.evosuite.runtime.Runtime.class.getName()+".getInstance().resetRuntime(); \n");
			bd.append(BLOCK_SPACE);
			bd.append(InstrumentingAgent.class.getName()+".activate(); \n");
		}

		if (SystemInUtil.getInstance().hasBeenUsed()) {
			bd.append(BLOCK_SPACE);
			bd.append(SystemInUtil.class.getName()+".getInstance().initForTestCase(); \n");
		}

		bd.append(METHOD_SPACE);
		bd.append("} \n");

		bd.append("\n");
	}

	private boolean shouldResetProperties(List<ExecutionResult> results) {
		/*
		 * Note: we need to reset the properties even if the SUT only read them. Reason is
		 * that we are modifying them in the test case in the @Before method
		 */
		Set<String> readProperties = null;
		if (Properties.REPLACE_CALLS) {
			readProperties = mergeProperties(results);
			if (readProperties.isEmpty()) {
				readProperties = null;
			}
		}

		boolean shouldResetProperties = Properties.REPLACE_CALLS
		        && (wasAnyWrittenProperty(results) || readProperties != null);

		return shouldResetProperties;
	}

	private String getResetPropertiesCommand() {
		return "java.lang.System.setProperties((java.util.Properties)" + " "
		        + DEFAULT_PROPERTIES + ".clone());";
	}

	private void generateAfterClass(StringBuilder bd, boolean wasSecurityException,
	        List<ExecutionResult> results) {

		if (wasSecurityException || shouldResetProperties(results)) {
			bd.append(METHOD_SPACE);
			bd.append("@AfterClass \n");
			bd.append(METHOD_SPACE);
			bd.append("public static void clearEvoSuiteFramework(){ \n");

			if (wasSecurityException) {
				bd.append(BLOCK_SPACE);
				bd.append(EXECUTOR_SERVICE + ".shutdownNow(); \n");
				bd.append(BLOCK_SPACE);
				bd.append("Sandbox.resetDefaultSecurityManager(); \n");
			}

			if (shouldResetProperties(results)) {
				bd.append(BLOCK_SPACE);
				bd.append(getResetPropertiesCommand());
				bd.append(" \n");
			}

			bd.append(METHOD_SPACE);
			bd.append("} \n");

			bd.append("\n");
		}

	}

	private void generateSetSystemProperties(StringBuilder bd,
	        List<ExecutionResult> results) {

		if (!Properties.REPLACE_CALLS) {
			return;
		}

		bd.append(METHOD_SPACE);
		bd.append("public void setSystemProperties() {\n");
		bd.append(" \n");
		if (shouldResetProperties(results)) {
			/*
			 * even if we set all the properties that were read, we still need
			 * to reset everything to handle the properties that were written 
			 */
			bd.append(BLOCK_SPACE);
			bd.append(getResetPropertiesCommand());
			bd.append(" \n");

			Set<String> readProperties = mergeProperties(results);
			for (String prop : readProperties) {
				bd.append(BLOCK_SPACE);
				String currentValue = System.getProperty(prop);
				String escaped_prop = StringEscapeUtils.escapeJava(prop);
				if (currentValue != null) {
					String escaped_currentValue = StringEscapeUtils.escapeJava(currentValue);
					bd.append("java.lang.System.setProperty(\"" + escaped_prop + "\", \""
					        + escaped_currentValue + "\"); \n");
				} else {
					bd.append("java.lang.System.clearProperty(\"" + escaped_prop
					        + "\"); \n");
				}
			}
		} else {
			bd.append(BLOCK_SPACE + "/*No java.lang.System property to set*/\n");
		}

		bd.append(METHOD_SPACE);
		bd.append("}\n");

	}

	private void generateBeforeClass(StringBuilder bd, boolean wasSecurityException) {

		if (!wasSecurityException && !Properties.REPLACE_CALLS && !Properties.VIRTUAL_FS
		        && !Properties.RESET_STATIC_FIELDS) {
			return;
		}

		bd.append(METHOD_SPACE);
		bd.append("@BeforeClass \n");

		bd.append(METHOD_SPACE);
		bd.append("public static void initEvoSuiteFramework() { \n");

		// FIXME: This is just commented out for experiments
		//bd.append("org.evosuite.utils.LoggingUtils.setLoggingForJUnit(); \n");

		if (Properties.REPLACE_CALLS || Properties.VIRTUAL_FS
		        || Properties.RESET_STATIC_FIELDS) {
			//need to setup REPLACE_CALLS and instrumentator

			if (Properties.REPLACE_CALLS) {
				bd.append(BLOCK_SPACE);
				bd.append(RuntimeSettings.class.getName()+".mockJVMNonDeterminism = true; \n");
			}

            if (Properties.VIRTUAL_FS) {
                bd.append(BLOCK_SPACE);
                bd.append(RuntimeSettings.class.getName()+".useVFS = true; \n");
            }

            if (Properties.REPLACE_SYSTEM_IN) {
                bd.append(BLOCK_SPACE);
                bd.append(RuntimeSettings.class.getName()+".mockSystemIn = true; \n");
            }

            /*
            Note: this one does not seem to be used inside prg.evosuite.runtime.*
            If it is needed, it should be added to RuntimeSettings

            if (Properties.RESET_STATIC_FIELDS) {
				bd.append(BLOCK_SPACE);
				bd.append(Properties.class.getName()+".RESET_STATIC_FIELDS = true; \n");
			}
            */

            //TODO sanbox mode?

			bd.append(BLOCK_SPACE);
			bd.append(InstrumentingAgent.class.getName()+".initialize(); \n");

		}

		if (wasSecurityException) {
			//need to setup the Sandbox mode
			bd.append(BLOCK_SPACE);
			bd.append(Properties.class.getName()+".SANDBOX_MODE = SandboxMode."
			        + Properties.SANDBOX_MODE + "; \n");

			bd.append(BLOCK_SPACE);
			bd.append(Sandbox.class.getName()+".initializeSecurityManagerForSUT(); \n");

			bd.append(BLOCK_SPACE);
			bd.append(EXECUTOR_SERVICE + " = Executors.newCachedThreadPool(); \n");
		}

		if (Properties.RESET_STATIC_FIELDS) {
			bd.append(BLOCK_SPACE);
			bd.append("initializeClasses();" + "\n");
		}

		if (Properties.REPLACE_CALLS || Properties.VIRTUAL_FS
		        || Properties.RESET_STATIC_FIELDS) {
			bd.append(BLOCK_SPACE);
			bd.append(org.evosuite.runtime.Runtime.class.getName()+".getInstance().resetRuntime(); \n");
		}

		bd.append(METHOD_SPACE);
		bd.append("} \n");

		bd.append("\n");
	}

	private void generateFields(StringBuilder bd, boolean wasSecurityException,
	        List<ExecutionResult> results) {

		if (Properties.RESET_STANDARD_STREAMS) {
			bd.append(METHOD_SPACE);
			bd.append("private PrintStream systemOut = null;" + '\n');

			bd.append(METHOD_SPACE);
			bd.append("private PrintStream systemErr = null;" + '\n');

			bd.append(METHOD_SPACE);
			bd.append("private PrintStream logStream = null;" + '\n');
		}

		if (wasSecurityException) {
			bd.append(METHOD_SPACE);
			bd.append("private static ExecutorService " + EXECUTOR_SERVICE + "; \n");

			bd.append("\n");
		}

		if (shouldResetProperties(results)) {
			/*
			 * some System properties were read/written. so, let's be sure we ll have the same
			 * properties in the generated JUnit file, regardless of where it will be executed
			 * (eg on a remote CI server). This is essential, as generated assertions might
			 * depend on those properties
			 */
			bd.append(METHOD_SPACE);
			bd.append("private static final java.util.Properties " + DEFAULT_PROPERTIES);
			bd.append(" = (java.util.Properties) java.lang.System.getProperties().clone(); \n");

			bd.append("\n");
		}
	}

	private boolean wasAnyWrittenProperty(List<ExecutionResult> results) {
		for (ExecutionResult res : results) {
			if (res.wasAnyPropertyWritten()) {
				return true;
			}
		}
		return false;
	}

	private Set<String> mergeProperties(List<ExecutionResult> results) {
		if (results == null) {
			return null;
		}
		Set<String> set = new LinkedHashSet<String>();
		for (ExecutionResult res : results) {
			Set<String> props = res.getReadProperties();
			if (props != null) {
				set.addAll(props);
			}
		}
		return set;
	}

	private final Map<String, Integer> testMethodNumber = new HashMap<String, Integer>();

	/**
	 * Convert one test case to a Java method
	 * 
	 * @param id
	 *            Index of the test case
	 * @return String representation of test case
	 * @param result
	 *            a {@link org.evosuite.testcase.ExecutionResult} object.
	 */
	protected String testToString(int number, int id, ExecutionResult result) {

		boolean wasSecurityException = result.hasSecurityException();

		StringBuilder builder = new StringBuilder();
		builder.append("\n");
		if (Properties.TEST_COMMENTS || testComment.containsKey(id)) {
			builder.append(METHOD_SPACE);
			builder.append("//");
			builder.append(getInformation(id));
			builder.append("\n");
		}
		String methodName;
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
			methodName = "test" + targetMethod + num;
			builder.append(adapter.getMethodDefinition(methodName));
		} else {
			methodName = getNameOfTest(testCases, number);
			builder.append(adapter.getMethodDefinition(methodName));
		}

		/*
		 * A test case might throw a lot of different kinds of exceptions. 
		 * These might come from SUT, and might also come from the framework itself (eg, see ExecutorService.submit).
		 * Regardless of whether they are declared or not, an exception that propagates to the JUnit framework will
		 * result in a failure for the test case. However, there might be some checked exceptions, and for those 
		 * we need to declare them in the signature with "throws". So, the easiest (but still correct) option
		 * is to just declare once to throw any genetic Exception, and be done with it once and for all
		 */
		builder.append(" throws Throwable ");
		builder.append(" {\n");

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
			builder.append("Future<?> future = " + EXECUTOR_SERVICE
			        + ".submit(new Runnable(){ \n");
			builder.append(INNER_BLOCK_SPACE);
			// Doesn't seem to need override?
			// builder.append("@Override \n");
			builder.append(INNER_BLOCK_SPACE);
			builder.append("public void run() { \n");
			Set<Class<?>> exceptions = test.getDeclaredExceptions();
			if (!exceptions.isEmpty()) {
				builder.append(INNER_INNER_BLOCK_SPACE);
				builder.append("try {\n");
			}
			CODE_SPACE = INNER_INNER_INNER_BLOCK_SPACE;
		}

		for (String line : adapter.getTestString(id, test,
		                                         result.exposeExceptionMapping(), visitor).split("\\r?\\n")) {
			builder.append(CODE_SPACE);
			builder.append(line);
			builder.append("\n");
		}

		if (wasSecurityException) {
			Set<Class<?>> exceptions = test.getDeclaredExceptions();
			if (!exceptions.isEmpty()) {
				builder.append(INNER_INNER_BLOCK_SPACE);
				builder.append("} catch(Throwable t) {\n");
				builder.append(INNER_INNER_INNER_BLOCK_SPACE);
				builder.append("  // Need to catch declared exceptions\n");
				builder.append(INNER_INNER_BLOCK_SPACE);
				builder.append("}\n");
			}

			builder.append(INNER_BLOCK_SPACE);
			builder.append("} \n"); //closing run(){
			builder.append(BLOCK_SPACE);
			builder.append("}); \n"); //closing submit

			long time = Properties.TIMEOUT + 1000; // we add one second just to be sure, that to avoid issues with test cases taking exactly TIMEOUT ms
			builder.append(BLOCK_SPACE);
			builder.append("future.get(" + time + ", TimeUnit.MILLISECONDS); \n");
		}

		// ---------   end of the body ----------------------------

		builder.append(METHOD_SPACE);
		builder.append("}\n");

		String testCode = builder.toString();
		TestGenerationResultBuilder.getInstance().setTestCase(methodName, testCode, test,
		                                                      getInformation(id), result);
		return testCode;
	}

	public static String getNameOfTest(List<TestCase> tests, int position) {

		if (Properties.ASSERTION_STRATEGY == AssertionStrategy.STRUCTURED) {
			throw new IllegalStateException(
			        "For the moment, structured tests are not supported");
		}

		int totalNumberOfTests = tests.size();
		String totalNumberOfTestsString = String.valueOf(totalNumberOfTests - 1);
		String testNumber = StringUtils.leftPad(String.valueOf(position),
		                                        totalNumberOfTestsString.length(), "0");
		String testName = "test" + testNumber;
		return testName;
	}

	/**
	 * Update/create the main file of the test suite. The main test file simply
	 * includes all automatically generated test suites in the same directory
	 * 
	 * @param directory
	 *            Directory of generated test files
	 */
	public void writeTestSuiteMainFile(String directory) {

		File file = new File(mainDirectory(directory) + "/GeneratedTestSuite.java");

		StringBuilder builder = new StringBuilder();
		if (!Properties.PROJECT_PREFIX.equals("")) {
			builder.append("package ");
			builder.append(Properties.PROJECT_PREFIX);
			// builder.append(".GeneratedTests;");
			builder.append(";\n\n");
		}
		List<String> suites = new ArrayList<String>();

		File basedir = new File(directory);
		Iterator<File> i = FileUtils.iterateFiles(basedir, new TestFilter(),
		                                          TrueFileFilter.INSTANCE);
		while (i.hasNext()) {
			File f = i.next();
			String name = f.getPath().replace(directory, "").replace(".java", "").replace("/",
			                                                                              ".");

			if (name.startsWith("."))
				name = name.substring(1);
			suites.add(name);
		}
		builder.append(adapter.getSuite(suites));
		Utils.writeFile(builder.toString(), file);
	}

	/**
	 * Create JUnit test suite for class
	 * 
	 * @param name
	 *            Name of the class
	 * @param directory
	 *            Output directory
	 */
	public List<File> writeTestSuite(String name, String directory) {
		List<File> generated = new ArrayList<File>();
		String dir = makeDirectory(directory);
		String content = "";

		if (Properties.OUTPUT_GRANULARITY == OutputGranularity.MERGED) {
			File file = new File(dir + "/" + name + ".java");
			executor.newObservers();
			content = getUnitTest(name);
			Utils.writeFile(content, file);
			generated.add(file);
		} else {
			for (int i = 0; i < testCases.size(); i++) {
				String testSuiteName = name + "_" + i;
				File file = new File(dir + "/" + testSuiteName + ".java");
				executor.newObservers();
				String testCode = getUnitTest(name, i);
				Utils.writeFile(testCode, file);
				content += testCode;
				generated.add(file);
			}
		}
		TestGenerationResultBuilder.getInstance().setTestSuiteCode(content);
		return generated;
	}

	private void testToBytecode(TestCase test, GeneratorAdapter mg,
	        Map<Integer, Throwable> exceptions) {
		Map<Integer, Integer> locals = new HashMap<Integer, Integer>();
		mg.visitAnnotation("Lorg/junit/Test;", true);
		int num = 0;
		for (StatementInterface statement : test) {
			logger.debug("Current statement: " + statement.getCode());
			statement.getBytecode(mg, locals, exceptions.get(num));
			num++;
		}
		mg.visitInsn(Opcodes.RETURN);
		mg.endMethod();

	}

	/**
	 * Get bytecode representation of test class
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @return an array of byte.
	 */
	public byte[] getBytecode(String name) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		String prefix = Properties.TARGET_CLASS.substring(0,
		                                                  Properties.TARGET_CLASS.lastIndexOf(".")).replace(".",
		                                                                                                    "/");
		cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, prefix + "/" + name, null,
		         "junit/framework/TestCase", null);

		Method m = Method.getMethod("void <init> ()");
		GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);
		mg.loadThis();
		mg.invokeConstructor(Type.getType(junit.framework.TestCase.class), m);
		mg.returnValue();
		mg.endMethod();

		int num = 0;
		for (TestCase test : testCases) {
			ExecutionResult result = runTest(test);
			m = Method.getMethod("void test" + num + " ()");
			mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);
			testToBytecode(test, mg, result.exposeExceptionMapping());
			num++;
		}

		// main method
		m = Method.getMethod("void main (String[])");
		mg = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, m, null, null, cw);
		mg.push(1);
		mg.newArray(Type.getType(String.class));
		mg.dup();
		mg.push(0);
		mg.push(Properties.CLASS_PREFIX + "." + name);
		mg.arrayStore(Type.getType(String.class));
		// mg.invokeStatic(Type.getType(org.junit.runner.JUnitCore.class),
		// Method.getMethod("void main (String[])"));
		mg.invokeStatic(Type.getType(junit.textui.TestRunner.class),
		                Method.getMethod("void main (String[])"));
		mg.returnValue();
		mg.endMethod();

		cw.visitEnd();
		return cw.toByteArray();
	}

	/**
	 * Create JUnit test suite in bytecode
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @param directory
	 *            a {@link java.lang.String} object.
	 */
	public void writeTestSuiteClass(String name, String directory) {
		String dir = makeDirectory(directory);
		File file = new File(dir + "/" + name + ".class");
		byte[] bytecode = getBytecode(name);
		try {
			FileOutputStream stream = new FileOutputStream(file);
			try {
				stream.write(bytecode);
			} finally {
				stream.close();
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		/*
		 * ClassReader reader = new ClassReader(bytecode); ClassVisitor cv = new
		 * TraceClassVisitor(new PrintWriter(System.out)); cv = new
		 * CheckClassAdapter(cv); reader.accept(cv, ClassReader.SKIP_FRAMES);
		 */

		// getBytecode(name);
	}
}
