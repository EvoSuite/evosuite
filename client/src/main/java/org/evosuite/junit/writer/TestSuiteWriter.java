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
package org.evosuite.junit.writer;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DebugGraphics;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.evosuite.Properties;
import org.evosuite.Properties.AssertionStrategy;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.OutputGranularity;
import org.evosuite.coverage.dataflow.DefUseCoverageTestFitness;
import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.evosuite.junit.UnitTestAdapter;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.agent.InstrumentingAgent;
import org.evosuite.runtime.reset.ClassResetter;
import org.evosuite.runtime.reset.ResetManager;
import org.evosuite.runtime.sandbox.Sandbox;
import org.evosuite.runtime.util.SystemInUtil;
import org.evosuite.testcase.CodeUnderTestException;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.StructuredTestCase;
import org.evosuite.testcase.StructuredTestCodeVisitor;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.Utils;
import org.objectweb.asm.Opcodes;
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

	private final UnitTestAdapter adapter = TestSuiteWriterUtils.getAdapter();

	private TestCodeVisitor visitor = Properties.ASSERTION_STRATEGY == AssertionStrategy.STRUCTURED ? visitor = new StructuredTestCodeVisitor()
	        : new TestCodeVisitor();

	private static final String METHOD_SPACE = "  ";
	private static final String BLOCK_SPACE = "    ";
	private static final String INNER_BLOCK_SPACE = "      ";
	private static final String INNER_INNER_BLOCK_SPACE = "        ";
	private static final String INNER_INNER_INNER_BLOCK_SPACE = "          ";

	private static final String EXECUTOR_SERVICE = "executor";
	private static final String DEFAULT_PROPERTIES = "defaultProperties";

	private final Map<String, Integer> testMethodNumber = new HashMap<String, Integer>();


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
	 * Create JUnit test suite for class
	 * 
	 * @param name
	 *            Name of the class
	 * @param directory
	 *            Output directory
	 */
	public List<File> writeTestSuite(String name, String directory) throws IllegalArgumentException {

        if(name==null || name.isEmpty()){
            throw new IllegalArgumentException("Empty test class name");
        }
        if(!name.endsWith("Test")){
            /*
             * This is VERY important, as otherwise tests can get ignored by "mvn test"
             */
            throw new IllegalArgumentException("Test classes should have name ending with 'Test'. Invalid input name: "+name);
        }

		List<File> generated = new ArrayList<File>();
		String dir = TestSuiteWriterUtils.makeDirectory(directory);
		String content = "";

		if (Properties.OUTPUT_GRANULARITY == OutputGranularity.MERGED) {
			File file = new File(dir + "/" + name + ".java");
			executor.newObservers();
			content = getUnitTest(name);
			Utils.writeFile(content, file);
			generated.add(file);
		} else {
			for (int i = 0; i < testCases.size(); i++) {
				String testSuiteName = name.substring(0,name.length()-"Test".length()) + "_" + i+"_Test";
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
	
	/**
	 * Create JUnit file for given class name
	 * 
	 * @param name
	 *            Name of the class file
	 * @return String representation of JUnit test file
	 */
	private String getUnitTest(String name) {
		List<ExecutionResult> results = new ArrayList<ExecutionResult>();

		for (int i = 0; i < testCases.size(); i++) {
			ExecutionResult result = runTest(testCases.get(i));
			results.add(result);
		}

		/*
		 * if there was any security exception, then we need to scaffold the
		 * test cases with a sandbox
		 */
		boolean wasSecurityException = TestSuiteWriterUtils.hasAnySecurityException(results);

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
	private String getUnitTest(String name, int testId) {
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
	 * <p>
	 * runTest
	 * </p>
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestCase} object.
	 * @return a {@link org.evosuite.testcase.ExecutionResult} object.
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
	 * @param results
	 *            a {@link java.util.List} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getImports(List<ExecutionResult> results) {
		StringBuilder builder = new StringBuilder();
		Set<Class<?>> imports = new HashSet<Class<?>>();
		boolean wasSecurityException = TestSuiteWriterUtils.hasAnySecurityException(results);

		for (ExecutionResult result : results) {
			result.test.accept(visitor);

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
				import_names.add(imp.getName().replace("$", "."));
			else
				import_names.add(imp.getName());
		}
		List<String> imports_sorted = new ArrayList<String>(import_names);

		if (Properties.REPLACE_CALLS || Properties.VIRTUAL_FS
		        || Properties.RESET_STATIC_FIELDS || wasSecurityException
		        || SystemInUtil.getInstance().hasBeenUsed()) {
			imports_sorted.add(org.junit.BeforeClass.class.getCanonicalName());
			imports_sorted.add(org.junit.Before.class.getCanonicalName());
			imports_sorted.add(org.junit.After.class.getCanonicalName());
		}

		if (wasSecurityException || TestSuiteWriterUtils.shouldResetProperties(results)) {
			imports_sorted.add(org.junit.AfterClass.class.getCanonicalName());
		}

		if (Properties.VIRTUAL_FS) {
			imports_sorted.add(org.evosuite.runtime.EvoSuiteFile.class.getCanonicalName());
		}

		if (wasSecurityException) {
			//Add import info for EvoSuite classes used in the generated test suite
			imports_sorted.add(Sandbox.class.getCanonicalName());
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
		builder.append(" * "+new Date()+"\n");
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

		//TODO: see comment in @Before 
		bd.append(BLOCK_SPACE);
		bd.append(org.evosuite.runtime.GuiSupport.class.getName()+".restoreHeadlessMode(); \n");

		
		bd.append(METHOD_SPACE);
		bd.append("} \n");

		bd.append("\n");
	}

	private void generateBefore(StringBuilder bd, boolean wasSecurityException,
	        List<ExecutionResult> results) {

		if (!Properties.RESET_STANDARD_STREAMS && !TestSuiteWriterUtils.shouldResetProperties(results)
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

		if (TestSuiteWriterUtils.shouldResetProperties(results)) {
			bd.append(BLOCK_SPACE);
			bd.append("setSystemProperties();");
			bd.append(" \n");
		}

		/*
		 * We do not mock GUI yet, but still we need to make the JUnit tests to 
		 * run in headless mode. Checking if SUT needs headless is tricky: check
		 * for headless exception is brittle if those exceptions are caught before
		 * propagating to test.
		 * 
		 * TODO: These things would be handled once we mock GUI. For the time being
		 * we just always include a reset call if @Before/@After methods are
		 * generated
		 */
		bd.append(BLOCK_SPACE);
		bd.append(org.evosuite.runtime.GuiSupport.class.getName()+".setHeadless(); \n");
		
		
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

	

	private String getResetPropertiesCommand() {
		return "java.lang.System.setProperties((java.util.Properties)" + " "
		        + DEFAULT_PROPERTIES + ".clone());";
	}

	private void generateAfterClass(StringBuilder bd, boolean wasSecurityException,
	        List<ExecutionResult> results) {

		if (wasSecurityException || TestSuiteWriterUtils.shouldResetProperties(results)) {
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

			if (TestSuiteWriterUtils.shouldResetProperties(results)) {
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
		if (TestSuiteWriterUtils.shouldResetProperties(results)) {
			/*
			 * even if we set all the properties that were read, we still need
			 * to reset everything to handle the properties that were written 
			 */
			bd.append(BLOCK_SPACE);
			bd.append(getResetPropertiesCommand());
			bd.append(" \n");

			Set<String> readProperties = TestSuiteWriterUtils.mergeProperties(results);
			for (String prop : readProperties) {
				bd.append(BLOCK_SPACE);
				String currentValue = System.getProperty(prop);
				String escaped_prop = StringEscapeUtils.escapeJava(prop);
				if (currentValue != null) {
					String escaped_currentValue = StringEscapeUtils.escapeJava(currentValue);
					bd.append("java.lang.System.setProperty(\"" + escaped_prop + "\", \""
					        + escaped_currentValue + "\"); \n");
				} else {
					/*
					 * In theory, we do not need to clear properties, as that is done with the reset to default.
					 * Avoiding doing the clear is not only good for readability (ie, less commands) but also
					 * to avoid crashes when properties are set based on SUT inputs. Eg, in classes like
					 *  SassToCssBuilder in 108_liferay we ended up with hundreds of thousands set properties... 
					 */
					//bd.append("java.lang.System.clearProperty(\"" + escaped_prop + "\"); \n");
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

            if (Properties.RESET_STATIC_FIELDS) {
				bd.append(BLOCK_SPACE);
				bd.append(RuntimeSettings.class.getName()+".resetStaticState = true; \n");
			}

			bd.append(BLOCK_SPACE);
			bd.append(InstrumentingAgent.class.getName()+".initialize(); \n");

		}

		if (wasSecurityException) {
			//need to setup the Sandbox mode
			bd.append(BLOCK_SPACE);
			bd.append(RuntimeSettings.class.getName()+".sandboxMode = "+
                    Sandbox.SandboxMode.class.getCanonicalName() + "." + Properties.SANDBOX_MODE + "; \n");

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

		if (TestSuiteWriterUtils.shouldResetProperties(results)) {
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
			methodName = TestSuiteWriterUtils.getNameOfTest(testCases, number);
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

}
