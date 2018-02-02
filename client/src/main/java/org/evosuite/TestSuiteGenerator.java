/**
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
package org.evosuite;

import org.evosuite.Properties.AssertionStrategy;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.TestFactory;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.classpath.ResourceList;
import org.evosuite.contracts.ContractChecker;
import org.evosuite.contracts.FailingTestSet;
import org.evosuite.coverage.CoverageCriteriaAnalyzer;
import org.evosuite.coverage.FitnessFunctions;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.dataflow.DefUseCoverageSuiteFitness;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.junit.JUnitAnalyzer;
import org.evosuite.junit.writer.TestSuiteWriter;
import org.evosuite.regression.bytecode.RegressionClassDiff;
import org.evosuite.regression.RegressionSuiteMinimizer;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.result.TestGenerationResultBuilder;
import org.evosuite.rmi.ClientServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.runtime.LoopCounter;
import org.evosuite.runtime.sandbox.PermissionStatistics;
import org.evosuite.seeding.ObjectPool;
import org.evosuite.seeding.ObjectPoolManager;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.statistics.StatisticsSender;
import org.evosuite.strategy.*;
import org.evosuite.symbolic.DSEStats;
import org.evosuite.testcase.ConstantInliner;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.execution.reset.ClassReInitializer;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.statements.StringPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.BooleanPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.*;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.*;

/**
 * Main entry point. Does all the static analysis, invokes a test generation
 * strategy, and then applies postprocessing.
 * 
 * @author Gordon Fraser
 */
public class TestSuiteGenerator {

	private static final String FOR_NAME = "forName";
	private static Logger logger = LoggerFactory.getLogger(TestSuiteGenerator.class);


	private void initializeTargetClass() throws Throwable {
		String cp = ClassPathHandler.getInstance().getTargetProjectClasspath();
		// Here is where the <clinit> code should be invoked for the first time
		DefaultTestCase test = buildLoadTargetClassTestCase(Properties.TARGET_CLASS);
		ExecutionResult execResult = TestCaseExecutor.getInstance().execute(test, Integer.MAX_VALUE);

		if (hasThrownInitializerError(execResult)) {
			// create single test suite with Class.forName()
			writeJUnitTestSuiteForFailedInitialization();
			ExceptionInInitializerError ex = getInitializerError(execResult);
			throw ex;
		} else if (!execResult.getAllThrownExceptions().isEmpty()) {
			// some other exception has been thrown during initialization
			Throwable t = execResult.getAllThrownExceptions().iterator().next();
			throw t;
		}

		DependencyAnalysis.analyzeClass(Properties.TARGET_CLASS, Arrays.asList(cp.split(File.pathSeparator)));
		LoggingUtils.getEvoLogger().info("* Finished analyzing classpath");
	}

	/**
	 * Generate a test suite for the target class
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public TestGenerationResult generateTestSuite() {

		LoggingUtils.getEvoLogger().info("* Analyzing classpath: ");

		ClientServices.getInstance().getClientNode().changeState(ClientState.INITIALIZATION);

		// Deactivate loop counter to make sure classes initialize properly
		LoopCounter.getInstance().setActive(false);

		TestCaseExecutor.initExecutor();
		try {
			initializeTargetClass();
		} catch (Throwable e) {

			// If the bytecode for a method exceeds 64K, Java will complain
			// Very often this is due to mutation instrumentation, so this dirty
			// hack adds a fallback mode without mutation.
			// This currently breaks statistics and assertions, so we have to also set these properties
			boolean error = true;

			String message = e.getMessage();
			if (message != null && (message.contains("Method code too large") || message.contains("Class file too large"))) {
				LoggingUtils.getEvoLogger().info("* Instrumentation exceeds Java's 64K limit per method in target class");
				Properties.Criterion[] newCriteria = Arrays.stream(Properties.CRITERION).filter(t -> !t.equals(Properties.Criterion.STRONGMUTATION) && !t.equals(Properties.Criterion.WEAKMUTATION) && !t.equals(Properties.Criterion.MUTATION)).toArray(Properties.Criterion[]::new);
				if(newCriteria.length < Properties.CRITERION.length) {
					TestGenerationContext.getInstance().resetContext();
					LoggingUtils.getEvoLogger().info("* Attempting re-instrumentation without mutation");
					Properties.CRITERION = newCriteria;
					if(Properties.NEW_STATISTICS) {
						LoggingUtils.getEvoLogger().info("* Deactivating EvoSuite statistics because of instrumentation problem");
						Properties.NEW_STATISTICS = false;
					}

					try {
						initializeTargetClass();
						error = false;
					} catch(Throwable t) {
						// No-op, error handled below
					}
					if(Properties.ASSERTIONS && Properties.ASSERTION_STRATEGY == AssertionStrategy.MUTATION) {
						LoggingUtils.getEvoLogger().info("* Deactivating assertion minimization because mutation instrumentation does not work");
						Properties.ASSERTION_STRATEGY = AssertionStrategy.ALL;
					}
				}
			}

		    if(error) {
				LoggingUtils.getEvoLogger().error("* Error while initializing target class: "
						+ (e.getMessage() != null ? e.getMessage() : e.toString()));
				logger.error("Problem for " + Properties.TARGET_CLASS + ". Full stack:", e);
				return TestGenerationResultBuilder.buildErrorResult(e.getMessage() != null ? e.getMessage() : e.toString());
			}

		} finally {
			if (Properties.RESET_STATIC_FIELDS) {
				configureClassReInitializer();

			}
			// Once class loading is complete we can start checking loops
			// without risking to interfere with class initialisation
			LoopCounter.getInstance().setActive(true);
		}

		/*
		 * Initialises the object pool with objects carved from SELECTED_JUNIT
		 * classes
		 */
		// TODO: Do parts of this need to be wrapped into sandbox statements?
		ObjectPoolManager.getInstance();

		LoggingUtils.getEvoLogger().info("* Generating tests for class " + Properties.TARGET_CLASS);
		TestSuiteGeneratorHelper.printTestCriterion();

		if (!Properties.hasTargetClassBeenLoaded()) {
			// initialization failed, then build error message
			return TestGenerationResultBuilder.buildErrorResult("Could not load target class");
		}

		if (Properties.isRegression() && Properties.REGRESSION_SKIP_SIMILAR) {
			// Sanity checks
			if (Properties.getTargetClassRegression(true) == null) {
			    Properties.IGNORE_MISSING_STATISTICS = false;
				logger.error("class {} was not on the regression projectCP", Properties.TARGET_CLASS);
				return TestGenerationResultBuilder.buildErrorResult("Could not load target regression class");
			}
			if (!ResourceList.getInstance(TestGenerationContext.getInstance().getRegressionClassLoaderForSUT())
					.hasClass(Properties.TARGET_CLASS)) {
			    Properties.IGNORE_MISSING_STATISTICS = false;
				logger.error("class {} was not on the regression_cp", Properties.TARGET_CLASS);
				return TestGenerationResultBuilder.buildErrorResult(
						"Class " + Properties.TARGET_CLASS + " did not exist on regression classpath");

			}

			boolean areDifferent = RegressionClassDiff.differentAcrossClassloaders(Properties.TARGET_CLASS);

			// If classes are different, no point in continuing.
			// TODO: report it to master to create a nice regression report
			if (!areDifferent) {
			    Properties.IGNORE_MISSING_STATISTICS = false;
				logger.error("class {} was equal on both versions", Properties.TARGET_CLASS);
				return TestGenerationResultBuilder.buildErrorResult(
						"Class " + Properties.TARGET_CLASS + " was not changed between the two versions");
			}
		}

		if (Properties.isRegression() && Properties.REGRESSION_SKIP_DIFFERENT_CFG) {
			// Does the class have the same CFG across the two versions of the program?
			boolean sameBranches = RegressionClassDiff.sameCFG();

			if (!sameBranches) {
				Properties.IGNORE_MISSING_STATISTICS = false;
				logger.error("Could not match the branches across the two versions.");
				return TestGenerationResultBuilder.buildErrorResult("Could not match the branches across the two versions.");
			}
		}

		TestSuiteChromosome testCases = generateTests();

		postProcessTests(testCases);
		ClientServices.getInstance().getClientNode().publishPermissionStatistics();
		PermissionStatistics.getInstance().printStatistics(LoggingUtils.getEvoLogger());

		// progressMonitor.setCurrentPhase("Writing JUnit test cases");
		TestGenerationResult result = writeJUnitTestsAndCreateResult(testCases);
		writeJUnitFailingTests();
		TestCaseExecutor.pullDown();
		/*
		 * TODO: when we will have several processes running in parallel, we ll
		 * need to handle the gathering of the statistics.
		 */
		ClientServices.getInstance().getClientNode().changeState(ClientState.WRITING_STATISTICS);

		LoggingUtils.getEvoLogger().info("* Done!");
		LoggingUtils.getEvoLogger().info("");

		return result;
	}

	/**
	 * Returns true iif the test case execution has thrown an instance of ExceptionInInitializerError
	 * 
	 * @param execResult of the test case execution
	 * @return true if the test case has thrown an ExceptionInInitializerError
	 */
	private static boolean hasThrownInitializerError(ExecutionResult execResult) {
		for (Throwable t : execResult.getAllThrownExceptions()) {
			if (t instanceof ExceptionInInitializerError) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Returns the initialized  error from the test case execution
	 * 
	 * @param execResult of the test case execution
	 * @return null if there were no thrown instances of ExceptionInInitializerError
	 */
	private static ExceptionInInitializerError getInitializerError(ExecutionResult execResult) {
		for (Throwable t : execResult.getAllThrownExceptions()) {
			if (t instanceof ExceptionInInitializerError) {
				ExceptionInInitializerError exceptionInInitializerError = (ExceptionInInitializerError)t;
				return exceptionInInitializerError;
			}
		}
		return null;
	}

	/**
	 * Reports the initialized classes during class initialization to the
	 * ClassReInitializater and configures the ClassReInitializer accordingly
	 */
	private void configureClassReInitializer() {
		// add loaded classes during building of dependency graph
		ExecutionTrace execTrace = ExecutionTracer.getExecutionTracer().getTrace();
		final List<String> initializedClasses = execTrace.getInitializedClasses();
		ClassReInitializer.getInstance().addInitializedClasses(initializedClasses);
		// set the behaviour of the ClassReInitializer
		final boolean reset_all_classes = Properties.RESET_ALL_CLASSES_DURING_TEST_GENERATION;
		ClassReInitializer.getInstance().setReInitializeAllClasses(reset_all_classes);
	}

	private static void writeJUnitTestSuiteForFailedInitialization() throws EvosuiteError {
		TestSuiteChromosome suite = new TestSuiteChromosome();
		DefaultTestCase test = buildLoadTargetClassTestCase(Properties.TARGET_CLASS);
		suite.addTest(test);
		writeJUnitTestsAndCreateResult(suite);
	}

	/**
	 * Creates a single Test Case that only loads the target class. 
	 * <code>
	 * Thread currentThread = Thread.currentThread();
	 * ClassLoader classLoader = currentThread.getClassLoader();
	 * classLoader.load(className);
	 * </code>
	 * @param className the class to be loaded
	 * @return
	 * @throws EvosuiteError if a reflection error happens while creating the test case
	 */
	private static DefaultTestCase buildLoadTargetClassTestCase(String className) throws EvosuiteError {
		DefaultTestCase test = new DefaultTestCase();

		StringPrimitiveStatement stmt0 = new StringPrimitiveStatement(test, className);
		VariableReference string0 = test.addStatement(stmt0);
		try {
			Method currentThreadMethod = Thread.class.getMethod("currentThread");
			Statement currentThreadStmt = new MethodStatement(test,
					new GenericMethod(currentThreadMethod, currentThreadMethod.getDeclaringClass()), null,
					Collections.emptyList());
			VariableReference currentThreadVar = test.addStatement(currentThreadStmt);

			Method getContextClassLoaderMethod = Thread.class.getMethod("getContextClassLoader");
			Statement getContextClassLoaderStmt = new MethodStatement(test,
					new GenericMethod(getContextClassLoaderMethod, getContextClassLoaderMethod.getDeclaringClass()),
					currentThreadVar, Collections.emptyList());
			VariableReference contextClassLoaderVar = test.addStatement(getContextClassLoaderStmt);

//			Method loadClassMethod = ClassLoader.class.getMethod("loadClass", String.class);
//			Statement loadClassStmt = new MethodStatement(test,
//					new GenericMethod(loadClassMethod, loadClassMethod.getDeclaringClass()), contextClassLoaderVar,
//					Collections.singletonList(string0));
//			test.addStatement(loadClassStmt);

			BooleanPrimitiveStatement stmt1 = new BooleanPrimitiveStatement(test, true);
			VariableReference boolean0 = test.addStatement(stmt1);
			
			Method forNameMethod = Class.class.getMethod("forName",String.class, boolean.class, ClassLoader.class);
			Statement forNameStmt = new MethodStatement(test,
					new GenericMethod(forNameMethod, forNameMethod.getDeclaringClass()), null,
					Arrays.<VariableReference>asList(string0, boolean0, contextClassLoaderVar));
			test.addStatement(forNameStmt);

			return test;
		} catch (NoSuchMethodException | SecurityException e) {
			throw new EvosuiteError("Unexpected exception while creating Class Initializer Test Case");
		}
	}

	/**
	 * Apply any readability optimizations and other techniques that should use
	 * or modify the generated tests
	 * 
	 * @param testSuite
	 */
	protected void postProcessTests(TestSuiteChromosome testSuite) {

		// If overall time is short, the search might not have had enough time
		// to come up with a suite without timeouts. However, they will slow
		// down
		// the rest of the process, and may lead to invalid tests
		testSuite.getTestChromosomes()
				.removeIf(t -> t.getLastExecutionResult() != null && (t.getLastExecutionResult().hasTimeout() ||
																	  t.getLastExecutionResult().hasTestException()));

		if (Properties.CTG_SEEDS_FILE_OUT != null) {
			TestSuiteSerialization.saveTests(testSuite, new File(Properties.CTG_SEEDS_FILE_OUT));
		} else if (Properties.TEST_FACTORY == TestFactory.SERIALIZATION) {
			TestSuiteSerialization.saveTests(testSuite,
					new File(Properties.SEED_DIR + File.separator + Properties.TARGET_CLASS));
		}

		/*
		 * Remove covered goals that are not part of the minimization targets,
		 * as they might screw up coverage analysis when a minimization timeout
		 * occurs. This may happen e.g. when MutationSuiteFitness calls
		 * BranchCoverageSuiteFitness which adds branch goals.
		 */
		// TODO: This creates an inconsistency between
		// suite.getCoveredGoals().size() and suite.getNumCoveredGoals()
		// but it is not clear how to update numcoveredgoals
		List<TestFitnessFunction> goals = new ArrayList<>();
		for (TestFitnessFactory<?> ff : getFitnessFactories()) {
			goals.addAll(ff.getCoverageGoals());
		}
		for (TestFitnessFunction f : testSuite.getCoveredGoals()) {
			if (!goals.contains(f)) {
				testSuite.removeCoveredGoal(f);
			}
		}

		if (Properties.INLINE) {
			ClientServices.getInstance().getClientNode().changeState(ClientState.INLINING);
			ConstantInliner inliner = new ConstantInliner();
			// progressMonitor.setCurrentPhase("Inlining constants");

			// Map<FitnessFunction<? extends TestSuite<?>>, Double> fitnesses =
			// testSuite.getFitnesses();

			inliner.inline(testSuite);
		}

		if (Properties.MINIMIZE) {
			ClientServices.getInstance().getClientNode().changeState(ClientState.MINIMIZATION);
			// progressMonitor.setCurrentPhase("Minimizing test cases");
			if (!TimeController.getInstance().hasTimeToExecuteATestCase()) {
				LoggingUtils.getEvoLogger().info("* Skipping minimization because not enough time is left");
				ClientServices.track(RuntimeVariable.Result_Size, testSuite.size());
				ClientServices.track(RuntimeVariable.Minimized_Size, testSuite.size());
				ClientServices.track(RuntimeVariable.Result_Length, testSuite.totalLengthOfTestCases());
				ClientServices.track(RuntimeVariable.Minimized_Length, testSuite.totalLengthOfTestCases());
			} else if (Properties.isRegression()) {
				RegressionSuiteMinimizer minimizer = new RegressionSuiteMinimizer();
				minimizer.minimize(testSuite);
			} else {

				double before = testSuite.getFitness();

				TestSuiteMinimizer minimizer = new TestSuiteMinimizer(getFitnessFactories());

				LoggingUtils.getEvoLogger().info("* Minimizing test suite");
				minimizer.minimize(testSuite, true);

				double after = testSuite.getFitness();
				if (after > before + 0.01d) { // assume minimization
					throw new Error("EvoSuite bug: minimization lead fitness from " + before + " to " + after);
				}
			}
		} else {
			if (!TimeController.getInstance().hasTimeToExecuteATestCase()) {
				LoggingUtils.getEvoLogger().info("* Skipping minimization because not enough time is left");
			}

			ClientServices.track(RuntimeVariable.Result_Size, testSuite.size());
			ClientServices.track(RuntimeVariable.Minimized_Size, testSuite.size());
			ClientServices.track(RuntimeVariable.Result_Length, testSuite.totalLengthOfTestCases());
			ClientServices.track(RuntimeVariable.Minimized_Length, testSuite.totalLengthOfTestCases());
		}

		if (Properties.COVERAGE) {
			ClientServices.getInstance().getClientNode().changeState(ClientState.COVERAGE_ANALYSIS);
			CoverageCriteriaAnalyzer.analyzeCoverage(testSuite);
		}

		double coverage = testSuite.getCoverage();

		if (ArrayUtil.contains(Properties.CRITERION, Criterion.MUTATION)
				|| ArrayUtil.contains(Properties.CRITERION, Criterion.STRONGMUTATION)) {
			// SearchStatistics.getInstance().mutationScore(coverage);
		}

		StatisticsSender.executedAndThenSendIndividualToMaster(testSuite);
		LoggingUtils.getEvoLogger().info(
				"* Generated " + testSuite.size() + " tests with total length " + testSuite.totalLengthOfTestCases());

		// TODO: In the end we will only need one analysis technique
		if (!Properties.ANALYSIS_CRITERIA.isEmpty()) {
			// SearchStatistics.getInstance().addCoverage(Properties.CRITERION.toString(),
			// coverage);
			CoverageCriteriaAnalyzer.analyzeCriteria(testSuite, Properties.ANALYSIS_CRITERIA);
			// FIXME: can we send all bestSuites?
		}
		if (Properties.CRITERION.length > 1)
			LoggingUtils.getEvoLogger()
					.info("* Resulting test suite's coverage: " + NumberFormat.getPercentInstance().format(coverage)
							+ " (average coverage for all fitness functions)");
		else
			LoggingUtils.getEvoLogger()
					.info("* Resulting test suite's coverage: " + NumberFormat.getPercentInstance().format(coverage));

		// printBudget(ga); // TODO - need to move this somewhere else
		if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE) && Properties.ANALYSIS_CRITERIA.isEmpty())
			DefUseCoverageSuiteFitness.printCoverage();

		DSEStats.getInstance().trackConstraintTypes();

		DSEStats.getInstance().trackSolverStatistics();

		if (Properties.DSE_PROBABILITY > 0.0 && Properties.LOCAL_SEARCH_RATE > 0
				&& Properties.LOCAL_SEARCH_PROBABILITY > 0.0) {
			DSEStats.getInstance().logStatistics();
		}

		if (Properties.FILTER_SANDBOX_TESTS) {
			for (TestChromosome test : testSuite.getTestChromosomes()) {
				// delete all statements leading to security exceptions
				ExecutionResult result = test.getLastExecutionResult();
				if (result == null) {
					result = TestCaseExecutor.runTest(test.getTestCase());
				}
				if (result.hasSecurityException()) {
					int position = result.getFirstPositionOfThrownException();
					if (position > 0) {
						test.getTestCase().chop(position);
						result = TestCaseExecutor.runTest(test.getTestCase());
						test.setLastExecutionResult(result);
					}
				}
			}
		}

		if (Properties.ASSERTIONS && !Properties.isRegression()) {
			LoggingUtils.getEvoLogger().info("* Generating assertions");
			// progressMonitor.setCurrentPhase("Generating assertions");
			ClientServices.getInstance().getClientNode().changeState(ClientState.ASSERTION_GENERATION);
			if (!TimeController.getInstance().hasTimeToExecuteATestCase()) {
				LoggingUtils.getEvoLogger().info("* Skipping assertion generation because not enough time is left");
			} else {
				TestSuiteGeneratorHelper.addAssertions(testSuite);
			}
			StatisticsSender.sendIndividualToMaster(testSuite); // FIXME: can we
																// pass the list
																// of
																// testsuitechromosomes?
		}

		if(Properties.NO_RUNTIME_DEPENDENCY) {
			LoggingUtils.getEvoLogger().info("* Property NO_RUNTIME_DEPENDENCY is set to true - skipping JUnit compile check");
			LoggingUtils.getEvoLogger().info("* WARNING: Not including the runtime dependencies is likely to lead to flaky tests!");
		}
		else if (Properties.JUNIT_TESTS && Properties.JUNIT_CHECK) {
			compileAndCheckTests(testSuite);
		}

		if (Properties.SERIALIZE_REGRESSION_TEST_SUITE) {
			RegressionSuiteSerializer.appendToRegressionTestSuite(testSuite);
		}

		if(Properties.isRegression() && Properties.KEEP_REGRESSION_ARCHIVE){
			RegressionSuiteSerializer.storeRegressionArchive();
		}
	}

	/**
	 * Compile and run the given tests. Remove from input list all tests that do
	 * not compile, and handle the cases of instability (either remove tests or
	 * comment out failing assertions)
	 *
	 * @param chromosome
	 */
	private void compileAndCheckTests(TestSuiteChromosome chromosome) {
		LoggingUtils.getEvoLogger().info("* Compiling and checking tests");

		if (!JUnitAnalyzer.isJavaCompilerAvailable()) {
			String msg = "No Java compiler is available. Make sure to run EvoSuite with the JDK and not the JRE."
					+ "You can try to setup the JAVA_HOME system variable to point to it, as well as to make sure that the PATH "
					+ "variable points to the JDK before any JRE.";
			logger.error(msg);
			throw new RuntimeException(msg);
		}

		ClientServices.getInstance().getClientNode().changeState(ClientState.JUNIT_CHECK);

		// Store this value; if this option is true then the JUnit check
		// would not succeed, as the JUnit classloader wouldn't find the class
		boolean junitSeparateClassLoader = Properties.USE_SEPARATE_CLASSLOADER;
		Properties.USE_SEPARATE_CLASSLOADER = false;

		int numUnstable = 0;

		// note: compiling and running JUnit tests can be very time consuming
		if (!TimeController.getInstance().isThereStillTimeInThisPhase()) {
			Properties.USE_SEPARATE_CLASSLOADER = junitSeparateClassLoader;
			return;
		}

		List<TestCase> testCases = chromosome.getTests(); // make copy of
															// current tests

		// first, let's just get rid of all the tests that do not compile
		JUnitAnalyzer.removeTestsThatDoNotCompile(testCases);

		// compile and run each test one at a time. and keep track of total time
		long start = java.lang.System.currentTimeMillis();
		Iterator<TestCase> iter = testCases.iterator();
		while (iter.hasNext()) {
			if (!TimeController.getInstance().hasTimeToExecuteATestCase()) {
				break;
			}
			TestCase tc = iter.next();
			List<TestCase> list = new ArrayList<>();
			list.add(tc);
			numUnstable += JUnitAnalyzer.handleTestsThatAreUnstable(list);
			if (list.isEmpty()) {
				// if the test was unstable and deleted, need to remove it from
				// final testSuite
				iter.remove();
			}
		}
		/*
		 * compiling and running each single test individually will take more
		 * than compiling/running everything in on single suite. so it can be
		 * used as an upper bound
		 */
		long delta = java.lang.System.currentTimeMillis() - start;

		numUnstable += checkAllTestsIfTime(testCases, delta);

		// second passage on reverse order, this is to spot dependencies among
		// tests
		if (testCases.size() > 1) {
			Collections.reverse(testCases);
			numUnstable += checkAllTestsIfTime(testCases, delta);
		}

		chromosome.clearTests(); // remove all tests
		for (TestCase testCase : testCases) {
			chromosome.addTest(testCase); // add back the filtered tests
		}

		boolean unstable = (numUnstable > 0);

		if (!TimeController.getInstance().isThereStillTimeInThisPhase()) {
			logger.warn("JUnit checking timed out");
		}

		ClientServices.track(RuntimeVariable.HadUnstableTests, unstable);
		ClientServices.track(RuntimeVariable.NumUnstableTests, numUnstable);
		Properties.USE_SEPARATE_CLASSLOADER = junitSeparateClassLoader;

	}

	private static int checkAllTestsIfTime(List<TestCase> testCases, long delta) {
		if (TimeController.getInstance().hasTimeToExecuteATestCase()
				&& TimeController.getInstance().isThereStillTimeInThisPhase(delta)) {
			return JUnitAnalyzer.handleTestsThatAreUnstable(testCases);
		}
		return 0;
	}

	private TestSuiteChromosome generateTests() {
		// Make sure target class is loaded at this point
		TestCluster.getInstance();

		ContractChecker checker = null;
		if (Properties.CHECK_CONTRACTS) {
			checker = new ContractChecker();
			TestCaseExecutor.getInstance().addObserver(checker);
		}

		TestGenerationStrategy strategy = TestSuiteGeneratorHelper.getTestGenerationStrategy();
		TestSuiteChromosome testSuite = strategy.generateTests();

		if (Properties.CHECK_CONTRACTS) {
			TestCaseExecutor.getInstance().removeObserver(checker);
		}

		StatisticsSender.executedAndThenSendIndividualToMaster(testSuite);
		TestSuiteGeneratorHelper.getBytecodeStatistics();

		ClientServices.getInstance().getClientNode().publishPermissionStatistics();

		writeObjectPool(testSuite);

		/*
		 * PUTGeneralizer generalizer = new PUTGeneralizer(); for (TestCase test
		 * : tests) { generalizer.generalize(test); // ParameterizedTestCase put
		 * = new ParameterizedTestCase(test); }
		 */

		return testSuite;
	}

	/**
	 * <p>
	 * If Properties.JUNIT_TESTS is set, this method writes the given test cases
	 * to the default directory Properties.TEST_DIR.
	 * 
	 * <p>
	 * The name of the test will be equal to the SUT followed by the given
	 * suffix
	 * 
	 * @param testSuite
	 *            a test suite.
	 */
	public static TestGenerationResult writeJUnitTestsAndCreateResult(TestSuiteChromosome testSuite, String suffix) {
		List<TestCase> tests = testSuite.getTests();
		if (Properties.JUNIT_TESTS) {
			ClientServices.getInstance().getClientNode().changeState(ClientState.WRITING_TESTS);

			TestSuiteWriter suiteWriter = new TestSuiteWriter();
			suiteWriter.insertTests(tests);

			String name = Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.lastIndexOf(".") + 1);
			String testDir = Properties.TEST_DIR;

			LoggingUtils.getEvoLogger().info("* Writing JUnit test case '" + (name + suffix) + "' to " + testDir);
			suiteWriter.writeTestSuite(name + suffix, testDir, testSuite.getLastExecutionResults());
		}
		return TestGenerationResultBuilder.buildSuccessResult();
	}

	/**
	 * 
	 * @param testSuite
	 *            the test cases which should be written to file
	 */
	public static TestGenerationResult writeJUnitTestsAndCreateResult(TestSuiteChromosome testSuite) {
		return writeJUnitTestsAndCreateResult(testSuite, Properties.JUNIT_SUFFIX);
	}

	public void writeJUnitFailingTests() {
		if (!Properties.CHECK_CONTRACTS)
			return;

		FailingTestSet.sendStatistics();

		if (Properties.JUNIT_TESTS) {

			TestSuiteWriter suiteWriter = new TestSuiteWriter();
			//suiteWriter.insertTests(FailingTestSet.getFailingTests());

			TestSuiteChromosome suite = new TestSuiteChromosome();
			for(TestCase test : FailingTestSet.getFailingTests()) {
				test.setFailing();
				suite.addTest(test);
			}

			String name = Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.lastIndexOf(".") + 1);
			String testDir = Properties.TEST_DIR;
			LoggingUtils.getEvoLogger().info("* Writing failing test cases '" + (name + Properties.JUNIT_SUFFIX) + "' to " + testDir);
			suiteWriter.insertAllTests(suite.getTests());
			FailingTestSet.writeJUnitTestSuite(suiteWriter);

			suiteWriter.writeTestSuite(name + Properties.JUNIT_FAILED_SUFFIX, testDir, suite.getLastExecutionResults());
		}
	}

	private void writeObjectPool(TestSuiteChromosome suite) {
		if (!Properties.WRITE_POOL.isEmpty()) {
			LoggingUtils.getEvoLogger().info("* Writing sequences to pool");
			ObjectPool pool = ObjectPool.getPoolFromTestSuite(suite);
			pool.writePool(Properties.WRITE_POOL);
		}
	}

	/**
	 * <p>
	 * getFitnessFunctions
	 * </p>
	 * 
	 * @return a list of {@link org.evosuite.testsuite.TestSuiteFitnessFunction}
	 *         objects.
	 */
	public static List<TestSuiteFitnessFunction> getFitnessFunctions() {
		List<TestSuiteFitnessFunction> ffs = new ArrayList<TestSuiteFitnessFunction>();
		for (int i = 0; i < Properties.CRITERION.length; i++) {
			ffs.add(FitnessFunctions.getFitnessFunction(Properties.CRITERION[i]));
		}

		return ffs;
	}

	/**
	 * Prints out all information regarding this GAs stopping conditions
	 * 
	 * So far only used for testing purposes in TestSuiteGenerator
	 */
	public void printBudget(GeneticAlgorithm<?> algorithm) {
		LoggingUtils.getEvoLogger().info("* Search Budget:");
		for (StoppingCondition sc : algorithm.getStoppingConditions())
			LoggingUtils.getEvoLogger().info("\t- " + sc.toString());
	}

	/**
	 * <p>
	 * getBudgetString
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String getBudgetString(GeneticAlgorithm<?> algorithm) {
		String r = "";
		for (StoppingCondition sc : algorithm.getStoppingConditions())
			r += sc.toString() + " ";

		return r;
	}

	/**
	 * <p>
	 * getFitnessFactories
	 * </p>
	 * 
	 * @return a list of {@link org.evosuite.coverage.TestFitnessFactory}
	 *         objects.
	 */
	public static List<TestFitnessFactory<? extends TestFitnessFunction>> getFitnessFactories() {
		List<TestFitnessFactory<? extends TestFitnessFunction>> goalsFactory = new ArrayList<TestFitnessFactory<? extends TestFitnessFunction>>();
		for (int i = 0; i < Properties.CRITERION.length; i++) {
			goalsFactory.add(FitnessFunctions.getFitnessFactory(Properties.CRITERION[i]));
		}

		return goalsFactory;
	}

	/**
	 * <p>
	 * main
	 * </p>
	 * 
	 * @param args
	 *            an array of {@link java.lang.String} objects.
	 */
	public static void main(String[] args) {
		TestSuiteGenerator generator = new TestSuiteGenerator();
		generator.generateTestSuite();
		System.exit(0);
	}

}
