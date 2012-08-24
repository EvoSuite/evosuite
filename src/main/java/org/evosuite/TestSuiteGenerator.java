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
package org.evosuite;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.vfs2.FileSystemException;
import org.evosuite.Properties.AssertionStrategy;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.Strategy;
import org.evosuite.Properties.TheReplacementFunction;
import org.evosuite.assertion.AssertionGenerator;
import org.evosuite.assertion.CompleteAssertionGenerator;
import org.evosuite.assertion.MutationAssertionGenerator;
import org.evosuite.assertion.UnitAssertionGenerator;
import org.evosuite.classcreation.ClassFactory;
import org.evosuite.contracts.ContractChecker;
import org.evosuite.contracts.FailingTestSet;
import org.evosuite.coverage.FitnessLogger;
import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.dataflow.AllDefsCoverageFactory;
import org.evosuite.coverage.dataflow.AllDefsCoverageSuiteFitness;
import org.evosuite.coverage.dataflow.DefUseCoverageFactory;
import org.evosuite.coverage.dataflow.DefUseCoverageSuiteFitness;
import org.evosuite.coverage.dataflow.DefUseCoverageTestFitness;
import org.evosuite.coverage.dataflow.DefUseFitnessCalculator;
import org.evosuite.coverage.exception.ExceptionCoverageSuiteFitness;
import org.evosuite.coverage.lcsaj.LCSAJ;
import org.evosuite.coverage.lcsaj.LCSAJCoverageFactory;
import org.evosuite.coverage.lcsaj.LCSAJCoverageSuiteFitness;
import org.evosuite.coverage.lcsaj.LCSAJCoverageTestFitness;
import org.evosuite.coverage.mutation.MutationFactory;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.coverage.mutation.MutationTestPool;
import org.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import org.evosuite.coverage.mutation.StrongMutationSuiteFitness;
import org.evosuite.coverage.mutation.WeakMutationSuiteFitness;
import org.evosuite.coverage.path.PrimePathCoverageFactory;
import org.evosuite.coverage.path.PrimePathSuiteFitness;
import org.evosuite.coverage.statement.StatementCoverageFactory;
import org.evosuite.coverage.statement.StatementCoverageSuiteFitness;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.CrossOverFunction;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.FitnessProportionateSelection;
import org.evosuite.ga.FitnessReplacementFunction;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.ga.IndividualPopulationLimit;
import org.evosuite.ga.MinimizeSizeSecondaryObjective;
import org.evosuite.ga.MuPlusLambdaGA;
import org.evosuite.ga.OnePlusOneEA;
import org.evosuite.ga.PopulationLimit;
import org.evosuite.ga.RandomSearch;
import org.evosuite.ga.RankSelection;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.ga.SelectionFunction;
import org.evosuite.ga.SinglePointCrossOver;
import org.evosuite.ga.SinglePointFixedCrossOver;
import org.evosuite.ga.SinglePointRelativeCrossOver;
import org.evosuite.ga.SizePopulationLimit;
import org.evosuite.ga.StandardGA;
import org.evosuite.ga.SteadyStateGA;
import org.evosuite.ga.TournamentChromosomeFactory;
import org.evosuite.ga.TournamentSelection;
import org.evosuite.ga.stoppingconditions.GlobalTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxFitnessEvaluationsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxGenerationStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition;
import org.evosuite.ga.stoppingconditions.SocketStoppingCondition;
import org.evosuite.ga.stoppingconditions.StoppingCondition;
import org.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition;
import org.evosuite.graphs.LCSAJGraph;
import org.evosuite.junit.TestSuiteWriter;
import org.evosuite.primitives.ObjectPool;
import org.evosuite.runtime.FileSystem;
import org.evosuite.sandbox.PermissionStatistics;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.TestCluster;
import org.evosuite.setup.TestClusterGenerator;
import org.evosuite.testcarver.capture.CaptureLog;
import org.evosuite.testcarver.capture.Capturer;
import org.evosuite.testcarver.codegen.CaptureLogAnalyzer;
import org.evosuite.testcarver.testcase.EvoTestCaseCodeGenerator;
import org.evosuite.testcarver.testcase.TestCarvingExecutionObserver;
import org.evosuite.testcase.AllMethodsTestChromosomeFactory;
import org.evosuite.testcase.ConstantInliner;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.JUnitTestChromosomeFactory;
import org.evosuite.testcase.RandomLengthTestFactory;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCaseExecutor;
import org.evosuite.testcase.TestCaseMinimizer;
import org.evosuite.testcase.TestCaseReplacementFunction;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.ValueMinimizer;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.evosuite.testsuite.CoverageCrossOver;
import org.evosuite.testsuite.CoverageStatistics;
import org.evosuite.testsuite.FixedSizeTestSuiteChromosomeFactory;
import org.evosuite.testsuite.MinimizeAverageLengthSecondaryObjective;
import org.evosuite.testsuite.MinimizeExceptionsSecondaryObjective;
import org.evosuite.testsuite.MinimizeMaxLengthSecondaryObjective;
import org.evosuite.testsuite.MinimizeTotalLengthSecondaryObjective;
import org.evosuite.testsuite.RelativeSuiteLengthBloatControl;
import org.evosuite.testsuite.SearchStatistics;
import org.evosuite.testsuite.StatementsPopulationLimit;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteChromosomeFactory;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.evosuite.testsuite.TestSuiteMinimizer;
import org.evosuite.testsuite.TestSuiteReplacementFunction;
import org.evosuite.utils.LoggingUtils;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.ResourceController;
import org.evosuite.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;
import de.unisb.cs.st.evosuite.io.IOWrapper;

/**
 * Main entry point
 * 
 * @author Gordon Fraser
 */
public class TestSuiteGenerator {

	private static Logger logger = LoggerFactory.getLogger(TestSuiteGenerator.class);

	private final SearchStatistics statistics = SearchStatistics.getInstance();

	/** Constant <code>zero_fitness</code> */
	public final static ZeroFitnessStoppingCondition zero_fitness = new ZeroFitnessStoppingCondition();

	/** Constant <code>global_time</code> */
	public static final GlobalTimeStoppingCondition global_time = new GlobalTimeStoppingCondition();

	/** Constant <code>stopping_condition</code> */
	public static StoppingCondition stopping_condition;
	/** Constant <code>analyzing=false</code> */
	public static boolean analyzing = false;

	private final ProgressMonitor progressMonitor = new ProgressMonitor();

	/*
	 * FIXME: a field is needed for "ga" to avoid a large re-factoring of the code.
	 * "ga" is given as input to many functions, but there are side-effects
	 * like "ga = setup()" that are not propagated to the "ga" reference
	 * of the top-function caller
	 */
	private GeneticAlgorithm ga;

	/**
	 * Generate a test suite for the target class
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String generateTestSuite() {

		LoggingUtils.getEvoLogger().info("* Analyzing classpath: ");
		try {
			DependencyAnalysis.analyze(Properties.TARGET_CLASS,
			                           Arrays.asList(Properties.CP.split(":")));
		} catch (Exception e) {
			LoggingUtils.getEvoLogger().info("* Error while initializing target class: "
			                                         + e.getMessage() + ", " + e);
			e.printStackTrace();
			return "";
		}
		TestCaseExecutor.initExecutor();
		setupProgressMonitor();

		Utils.addURL(ClassFactory.getStubDir() + "/classes/");

		LoggingUtils.getEvoLogger().info("* Generating tests for class "
		                                         + Properties.TARGET_CLASS);
		printTestCriterion();

		if (Properties.getTargetClass() == null)
			return "";

		if (Properties.CRITERION == Criterion.ANALYZE)
			analyzeCriteria();
		else
			generateTests();

		TestCaseExecutor.pullDown();
		/*
		 * TODO: when we will have several processes running in parallel, we ll
		 * need to handle the gathering of the statistics. 
		 */
		statistics.writeReport();
		statistics.writeStatistics();
		PermissionStatistics.getInstance().printStatistics();

		LoggingUtils.getEvoLogger().info("* Done!");
		LoggingUtils.getEvoLogger().info("");

		return "";
	}

	/*
	 * return reference of the GA used in the most recent generateTestSuite()
	 */
	/**
	 * <p>
	 * getEmployedGeneticAlgorithm
	 * </p>
	 * 
	 * @return a {@link org.evosuite.ga.GeneticAlgorithm} object.
	 */
	public GeneticAlgorithm getEmployedGeneticAlgorithm() {
		return ga;
	}

	private void analyzeCriteria() {
		analyzing = true;
		for (Criterion criterion : CoverageStatistics.supportedCriteria) {
			Properties.CRITERION = criterion;
			LoggingUtils.getEvoLogger().info("* Analyzing Criterion: "
			                                         + Properties.CRITERION);
			generateTests();

			// TODO reset method?
			TestCaseExecutor.timeExecuted = 0l;
			AbstractFitnessFactory.goalComputationTime = 0l;
			GlobalTimeStoppingCondition.forceReset();
		}
		Properties.CRITERION = Criterion.ANALYZE;
		CoverageStatistics.computeCombinedCoverages();
		CoverageStatistics.writeCSV();
	}

	private void setupProgressMonitor() {
		int phases = 1;
		if (Properties.ASSERTIONS)
			phases++;
		//if (Properties.JUNIT_TESTS)
		//	phases++;
		if (Properties.MINIMIZE || Properties.INLINE || Properties.MINIMIZE_VALUES)
			phases++;

		progressMonitor.setNumberOfPhases(phases);
	}

	private List<TestCase> generateTests() {
		List<TestCase> tests;
		// Make sure target class is loaded at this point
		TestCluster.getInstance();

		if (TestCluster.getInstance().getTestCalls().isEmpty()) {
			LoggingUtils.getEvoLogger().info("* Found no testable methods in the target class "
			                                         + Properties.TARGET_CLASS);
			return new ArrayList<TestCase>();
		}

		if (Properties.STRATEGY == Strategy.EVOSUITE)
			tests = generateWholeSuite();
		else if (Properties.STRATEGY == Strategy.RANDOM)
			tests = generateRandomTests();
		else
			tests = generateIndividualTests();

		LoggingUtils.getEvoLogger().info("* Time spent executing tests: "
		                                         + TestCaseExecutor.timeExecuted + "ms");

		if (Properties.CRITERION == Criterion.DEFUSE) {
			if (Properties.ENABLE_ALTERNATIVE_FITNESS_CALCULATION)
				LoggingUtils.getEvoLogger().info("* Time spent calculating alternative fitness: "
				                                         + DefUseFitnessCalculator.alternativeTime
				                                         + "ms");
			LoggingUtils.getEvoLogger().info("* Time spent calculating single fitnesses: "
			                                         + DefUseCoverageTestFitness.singleFitnessTime
			                                         + "ms");
		}

		if (Properties.ASSERTIONS) {
			LoggingUtils.getEvoLogger().info("* Generating assertions");
			progressMonitor.setCurrentPhase("Generating assertions");
			if (Properties.CRITERION == Criterion.MUTATION
			        || Properties.CRITERION == Criterion.STRONGMUTATION) {
				handleMutations(tests);
			} else {
				// If we're not using mutation testing, we need to re-instrument
				addAssertions(tests);
			}
		}

		//progressMonitor.setCurrentPhase("Writing JUnit test cases");
		writeJUnitTests(tests);

		if (Properties.CHECK_CONTRACTS) {
			LoggingUtils.getEvoLogger().info("* Writing failing test cases");
			FailingTestSet.writeJUnitTestSuite();
		}

		writeObjectPool(tests);

		if (analyzing)
			LoggingUtils.getEvoLogger().info("");

		/*
		PUTGeneralizer generalizer = new PUTGeneralizer();
		for (TestCase test : tests) {
			generalizer.generalize(test);
			//			ParameterizedTestCase put = new ParameterizedTestCase(test);
		}
		*/

		return tests;
	}

	/**
	 * <p>
	 * writeJUnitTests
	 * </p>
	 * 
	 * @param tests
	 *            a {@link java.util.List} object.
	 */
	public static void writeJUnitTests(List<TestCase> tests) {
		if (Properties.JUNIT_TESTS) {
			TestSuiteWriter suite = new TestSuiteWriter();
			suite.insertTests(tests);
			String name = Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.lastIndexOf(".") + 1);
			String testDir = Properties.TEST_DIR;
			if (analyzing)
				testDir = testDir + "/" + Properties.CRITERION;
			LoggingUtils.getEvoLogger().info("* Writing JUnit test cases to " + testDir);
			suite.writeTestSuite("Test" + name, testDir);
			// suite.writeTestSuiteMainFile(testDir);
		}
	}

	/**
	 * If Properties.JUNIT_TESTS is set, this method writes the given test cases
	 * to the default directory Properties.TEST_DIR. Unlike its twin
	 * writeJUnitTests(tests) this method adds a given tag to the default file
	 * name, allowing several test files per class. Instead of TestMyClass.java
	 * the test cases are written to TestMayClassmytag.java.
	 * 
	 * @param tests
	 *            the test cases which should be written to file
	 * @param tag
	 *            the appendix which should be added to the default file name
	 */
	public static void writeJUnitTests(List<TestCase> tests, String tag) {
		if (Properties.JUNIT_TESTS) {
			TestSuiteWriter suite = new TestSuiteWriter();
			suite.insertTests(tests);
			String name = Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.lastIndexOf(".") + 1);
			String testDir = Properties.TEST_DIR;
			if (analyzing)
				testDir = testDir + "/" + Properties.CRITERION;
			LoggingUtils.getEvoLogger().info("* Writing JUnit test cases to " + testDir);
			suite.writeTestSuite("Test" + name + tag, testDir);
			// suite.writeTestSuiteMainFile(testDir);
		}
	}

	private void addAssertions(List<TestCase> tests) {
		AssertionGenerator asserter;
		ContractChecker.setActive(false);

		if (Properties.ASSERTION_STRATEGY == AssertionStrategy.MUTATION) {
			Criterion oldCriterion = Properties.CRITERION;
			if (Properties.CRITERION != Criterion.MUTATION
			        && Properties.CRITERION != Criterion.WEAKMUTATION
			        && Properties.CRITERION != Criterion.STRONGMUTATION) {
				Properties.CRITERION = Criterion.MUTATION;

				try {
					TestClusterGenerator.resetCluster();
				} catch (Exception e) {
					LoggingUtils.getEvoLogger().info("Error while instrumenting for assertion generation: "
					                                         + e.getMessage());
					return;
				}

				// TODO: Now all existing test cases have reflection objects pointing to the wrong classloader
				for (TestCase test : tests) {
					DefaultTestCase dtest = (DefaultTestCase) test;
					dtest.changeClassLoader(TestCluster.classLoader);
				}
			}

			long startTime = System.currentTimeMillis();

			MutationAssertionGenerator masserter = new MutationAssertionGenerator();
			Set<Integer> tkilled = new HashSet<Integer>();
			int numTest = 0;
			for (TestCase test : tests) {
				long currentTime = System.currentTimeMillis();
				if (currentTime - startTime > Properties.ASSERTION_TIMEOUT)
					break;
				//Set<Integer> killed = new HashSet<Integer>();
				masserter.addAssertions(test, tkilled);
				progressMonitor.updateStatus((100 * numTest++) / tests.size());
				//tkilled.addAll(killed);
			}
			Properties.CRITERION = oldCriterion;
			double score = (double) tkilled.size()
			        / (double) MutationPool.getMutantCounter();
			SearchStatistics.getInstance().mutationScore(score);
			LoggingUtils.getEvoLogger().info("* Resulting test suite's mutation score: "
			                                         + NumberFormat.getPercentInstance().format(score));

			return;

		} else if (Properties.ASSERTION_STRATEGY == AssertionStrategy.ALL) {
			asserter = new CompleteAssertionGenerator();
		} else
			asserter = new UnitAssertionGenerator();

		for (TestCase test : tests) {
			asserter.addAssertions(test);
		}
	}

	private void handleMutations(List<TestCase> tests) {
		// TODO better method name?
		MutationAssertionGenerator asserter = new MutationAssertionGenerator();
		Set<Integer> tkilled = new HashSet<Integer>();
		int num = 0;
		for (TestCase test : tests) {
			progressMonitor.updateStatus((100 * num++) / tests.size());

			//Set<Integer> killed = new HashSet<Integer>();
			asserter.addAssertions(test, tkilled);
			//tkilled.addAll(killed);
		}
		double score = (double) tkilled.size() / (double) MutationPool.getMutantCounter();
		SearchStatistics.getInstance().mutationScore(score);
		// asserter.writeStatistics();
		//LoggingUtils.getEvoLogger().info("Killed: " + tkilled.size() + "/" + asserter.numMutants());
	}

	private void writeObjectPool(List<TestCase> tests) {
		if (Properties.WRITE_POOL) {
			LoggingUtils.getEvoLogger().info("* Writing sequences to pool");
			ObjectPool pool = ObjectPool.getInstance();
			for (TestCase test : tests) {
				pool.storeSequence(Properties.getTargetClass(), test);
			}
		}
	}

	/**
	 * Executes all given test cases and carves their execution. Note that the
	 * accessed classes of a TestCase are considered as classes to be observed
	 * (if they do not represent primitive types).
	 * 
	 * @param testsToBeCarved
	 *            list of test cases
	 * @return list of test cases carved from the execution of the given test
	 *         cases
	 */
	private List<TestCase> carveTests(List<TestCase> testsToBeCarved) {
		final ArrayList<TestCase> result = new ArrayList<TestCase>(testsToBeCarved.size());
		final TestCaseExecutor executor = TestCaseExecutor.getInstance();
		final TestCarvingExecutionObserver execObserver = new TestCarvingExecutionObserver();
		executor.addObserver(execObserver);

		final HashSet<Class<?>> allAccessedClasses = new HashSet<Class<?>>();
		final Logger logger = LoggingUtils.getEvoLogger();

		// variables needed in loop
		CaptureLog log;
		TestCase carvedTestCase;

		final CaptureLogAnalyzer analyzer = new CaptureLogAnalyzer();
		final EvoTestCaseCodeGenerator codeGen = new EvoTestCaseCodeGenerator();

		for (TestCase t : testsToBeCarved) {
			// collect all accessed classes ( = classes to be observed)
			allAccessedClasses.addAll(t.getAccessedClasses());

			// start capture before genetic algorithm is applied so that all interactions can be captured
			Capturer.startCapture();

			// execute test case
			executor.execute(t);

			// stop capture after best individual has been determined and obtain corresponding capture log
			log = Capturer.stopCapture();

			//----- filter accessed classes

			// remove all classes representing primitive types
			Class<?> c;
			final Iterator<Class<?>> iter = allAccessedClasses.iterator();
			while (iter.hasNext()) {
				c = iter.next();
				if (c.isPrimitive()) {
					iter.remove();
				}
			}

			if (allAccessedClasses.isEmpty()) {
				logger.warn("There are no classes which can be observed in test\n{}\n --> no test carving performed",
				            t);
				Capturer.clear();
				continue;
			}

			//----- generate code out of capture log

			logger.debug("Evosuite Test:\n{}", t);

			// generate carved test with the currently captured log and allAccessedlasses as classes to be observed

			analyzer.analyze(log,
			                 codeGen,
			                 allAccessedClasses.toArray(new Class[allAccessedClasses.size()]));
			carvedTestCase = codeGen.getCode();
			codeGen.clear();

			logger.debug("Carved Test:\n{}", carvedTestCase);
			result.add(carvedTestCase);

			// reuse helper data structures
			allAccessedClasses.clear();

			// clear Capturer content to save memory
			Capturer.clear();
		}

		executor.removeObserver(execObserver);

		return result;
	}

	/**
	 * Use the EvoSuite approach (Whole test suite generation)
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public List<TestCase> generateWholeSuite() {
		// Set up search algorithm
		if (ga == null || ga.getAge() == 0) {
			LoggingUtils.getEvoLogger().info("* Setting up search algorithm for whole suite generation");
			ga = setup();
		} else {
			LoggingUtils.getEvoLogger().info("* Resuming search algorithm at generation "
			                                         + ga.getAge()
			                                         + " for whole suite generation");
		}
		long start_time = System.currentTimeMillis() / 1000;

		// What's the search target
		FitnessFunction fitness_function = getFitnessFunction();
		ga.setFitnessFunction(fitness_function);
		if (Properties.CRITERION == Criterion.STRONGMUTATION) {
			ga.addListener((StrongMutationSuiteFitness) fitness_function);
		}

		ga.setChromosomeFactory(getChromosomeFactory(fitness_function));
		//if (Properties.SHOW_PROGRESS && !logger.isInfoEnabled())
		ga.addListener(progressMonitor);

		if (Properties.CRITERION == Criterion.DEFUSE
		        || Properties.CRITERION == Criterion.ALLDEFS
		        || Properties.CRITERION == Criterion.STATEMENT)
			ExecutionTracer.enableTraceCalls();

		//TODO: why it was only if "analyzing"???
		//if (analyzing)
		ga.resetStoppingConditions();

		TestFitnessFactory goal_factory = getFitnessFactory();
		List<TestFitnessFunction> goals = goal_factory.getCoverageGoals();
		LoggingUtils.getEvoLogger().info("* Total number of test goals: " + goals.size());

		// Perform search
		LoggingUtils.getEvoLogger().info("* Starting evolution");
		progressMonitor.setCurrentPhase("Generating test cases");

		ga.generateSolution();
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		if (best == null) {
			LoggingUtils.getEvoLogger().warn("Could not find any suiteable chromosome");
			return Collections.emptyList();
		}

		long end_time = System.currentTimeMillis() / 1000;

		// Newline after progress bar
		if (Properties.SHOW_PROGRESS)
			LoggingUtils.getEvoLogger().info("");
		LoggingUtils.getEvoLogger().info("* Search finished after "
		                                         + (end_time - start_time)
		                                         + "s and "
		                                         + ga.getAge()
		                                         + " generations, "
		                                         + MaxStatementsStoppingCondition.getNumExecutedStatements()
		                                         + " statements, best individual has fitness "
		                                         + best.getFitness());

		double fitness = best.getFitness();

		// TODO also consider time for test carving in end_time?
		if (Properties.TEST_CARVING) {
			// execute all tests to carve them
			final List<TestCase> carvedTests = this.carveTests(best.getTests());

			// replace chromosome test cases with carved tests
			best.clearTests();
			for (TestCase t : carvedTests) {
				best.addTest(t);
			}
		}

		progressMonitor.setCurrentPhase("Reducing tests");
		if (Properties.MINIMIZE_VALUES) {
			LoggingUtils.getEvoLogger().info("* Minimizing values");
			ValueMinimizer minimizer = new ValueMinimizer();
			minimizer.minimize(best, (TestSuiteFitnessFunction) fitness_function);
			assert (fitness >= best.getFitness());
		}
		progressMonitor.updateStatus(33);

		if (Properties.INLINE) {
			ConstantInliner inliner = new ConstantInliner();
			// progressMonitor.setCurrentPhase("Inlining constants");
			inliner.inline(best);
			assert (fitness >= best.getFitness());
		}
		progressMonitor.updateStatus(66);

		if (Properties.MINIMIZE) {
			LoggingUtils.getEvoLogger().info("* Minimizing result");
			// progressMonitor.setCurrentPhase("Minimizing test cases");
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer(getFitnessFactory());
			minimizer.minimize(best);
		}
		progressMonitor.updateStatus(99);

		statistics.iteration(ga);
		statistics.minimized(best);
		LoggingUtils.getEvoLogger().info("* Generated " + best.size()
		                                         + " tests with total length "
		                                         + best.totalLengthOfTestCases());

		if (analyzing)
			CoverageStatistics.analyzeCoverage(best);
		else {
			LoggingUtils.getEvoLogger().info("* Resulting test suite's coverage: "
			                                         + NumberFormat.getPercentInstance().format(best.getCoverage()));
		}

		ga.printBudget();
		if (Properties.CRITERION == Criterion.DEFUSE)
			DefUseCoverageSuiteFitness.printCoverage();

		return best.getTests();
	}

	private void printTestCriterion() {
		switch (Properties.CRITERION) {
		case WEAKMUTATION:
			LoggingUtils.getEvoLogger().info("* Test criterion: Mutation testing (weak)");
			break;
		case STRONGMUTATION:
		case MUTATION:
			LoggingUtils.getEvoLogger().info("* Test criterion: Mutation testing (strong)");
			break;
		case LCSAJ:
			LoggingUtils.getEvoLogger().info("* Test criterion: LCSAJ");
			break;
		case DEFUSE:
			LoggingUtils.getEvoLogger().info("* Test criterion: All DU Pairs");
			break;
		case PATH:
			LoggingUtils.getEvoLogger().info("* Test criterion: Prime Path");
			break;
		case STATEMENT:
			LoggingUtils.getEvoLogger().info("* Test Criterion: Statement Coverage");
			break;
		case ANALYZE:
			LoggingUtils.getEvoLogger().info("* Test Criterion: Analyzing");
			break;
		case ALLDEFS:
			LoggingUtils.getEvoLogger().info("* Test Criterion: All Definitions");
			break;
		case BEHAVIORAL:
			LoggingUtils.getEvoLogger().info("* Test criterion: Behavioral coverage");
			break;
		case EXCEPTION:
			LoggingUtils.getEvoLogger().info("* Test Criterion: Exception");
			break;
		default:
			LoggingUtils.getEvoLogger().info("* Test criterion: Branch coverage");
		}
	}

	/**
	 * <p>
	 * getFitnessFunction
	 * </p>
	 * 
	 * @return a {@link org.evosuite.testsuite.TestSuiteFitnessFunction} object.
	 */
	public static TestSuiteFitnessFunction getFitnessFunction() {
		return getFitnessFunction(Properties.CRITERION);
	}

	/**
	 * <p>
	 * getFitnessFunction
	 * </p>
	 * 
	 * @param criterion
	 *            a {@link org.evosuite.Properties.Criterion} object.
	 * @return a {@link org.evosuite.testsuite.TestSuiteFitnessFunction} object.
	 */
	public static TestSuiteFitnessFunction getFitnessFunction(Criterion criterion) {
		switch (criterion) {
		case STRONGMUTATION:
			return new StrongMutationSuiteFitness();
		case WEAKMUTATION:
			return new WeakMutationSuiteFitness();
		case MUTATION:
			return new StrongMutationSuiteFitness();
		case LCSAJ:
			return new LCSAJCoverageSuiteFitness();
		case DEFUSE:
			return new DefUseCoverageSuiteFitness();
		case PATH:
			return new PrimePathSuiteFitness();
		case BRANCH:
			return new BranchCoverageSuiteFitness();
		case STATEMENT:
			return new StatementCoverageSuiteFitness();
		case ALLDEFS:
			return new AllDefsCoverageSuiteFitness();
		case EXCEPTION:
			return new ExceptionCoverageSuiteFitness();
		case LOOP_INV_CANDIDATE_FALSE_BRANCH:
			return new BranchCoverageSuiteFitness();
		default:
			logger.warn("No TestSuiteFitnessFunction defined for " + Properties.CRITERION
			        + " using default one (BranchCoverageSuiteFitness)");
			return new BranchCoverageSuiteFitness();
		}
	}

	/**
	 * <p>
	 * getFitnessFactory
	 * </p>
	 * 
	 * @return a {@link org.evosuite.coverage.TestFitnessFactory} object.
	 */
	public static TestFitnessFactory getFitnessFactory() {
		return getFitnessFactory(Properties.CRITERION);
	}

	/**
	 * <p>
	 * getFitnessFactory
	 * </p>
	 * 
	 * @param crit
	 *            a {@link org.evosuite.Properties.Criterion} object.
	 * @return a {@link org.evosuite.coverage.TestFitnessFactory} object.
	 */
	public static TestFitnessFactory getFitnessFactory(Criterion crit) {
		switch (crit) {
		case STRONGMUTATION:
		case MUTATION:
			return new MutationFactory();
		case WEAKMUTATION:
			return new MutationFactory(false);
		case LCSAJ:
			return new LCSAJCoverageFactory();
		case DEFUSE:
			return new DefUseCoverageFactory();
		case PATH:
			return new PrimePathCoverageFactory();
		case BRANCH:
			return new BranchCoverageFactory();
		case STATEMENT:
			return new StatementCoverageFactory();
		case ALLDEFS:
			return new AllDefsCoverageFactory();
		case EXCEPTION:
			return new BranchCoverageFactory();
			//			return new ExceptionCoverageFactory();
		default:
			logger.warn("No TestFitnessFactory defined for " + crit
			        + " using default one (BranchCoverageFactory)");
			return new BranchCoverageFactory();
		}
	}

	/**
	 * Cover the easy targets first with a set of random tests, so that the
	 * actual search can focus on the non-trivial test goals
	 * 
	 * @return
	 */
	private TestSuiteChromosome bootstrapRandomSuite(FitnessFunction fitness,
	        TestFitnessFactory goals) {

		if (Properties.CRITERION == Criterion.DEFUSE
		        || Properties.CRITERION == Criterion.ALLDEFS) {
			LoggingUtils.getEvoLogger().info("* Disabled random bootstraping for dataflow criterion");
			Properties.RANDOM_TESTS = 0;
		}

		if (Properties.RANDOM_TESTS > 0) {
			LoggingUtils.getEvoLogger().info("* Bootstrapping initial random test suite");
		} //else
		  //	LoggingUtils.getEvoLogger().info("* Bootstrapping initial random test suite disabled!");

		FixedSizeTestSuiteChromosomeFactory factory = new FixedSizeTestSuiteChromosomeFactory(
		        Properties.RANDOM_TESTS);

		TestSuiteChromosome suite = factory.getChromosome();
		if (Properties.RANDOM_TESTS > 0) {
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer(goals);
			minimizer.minimize(suite);
			LoggingUtils.getEvoLogger().info("* Initial test suite contains "
			                                         + suite.size() + " tests");
		}

		return suite;
	}

	private boolean isFinished(TestSuiteChromosome chromosome) {
		if (stopping_condition.isFinished())
			return true;

		if (Properties.STOP_ZERO) {
			if (chromosome.getFitness() == 0.0)
				return true;
		}

		if (!(stopping_condition instanceof MaxTimeStoppingCondition)) {
			if (global_time.isFinished())
				return true;
		}

		return false;
	}

	/**
	 * Generate one random test at a time and check if adding it improves
	 * fitness (1+1)RT
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public List<TestCase> generateRandomTests() {
		LoggingUtils.getEvoLogger().info("* Using random test generation");

		TestSuiteChromosome suite = new TestSuiteChromosome();
		TestSuiteFitnessFunction fitnessFunction = getFitnessFunction();

		// The GA is not actually used, except to provide the same statistics as during search
		GeneticAlgorithm suiteGA = getGeneticAlgorithm(new TestSuiteChromosomeFactory());
		// GeneticAlgorithm suiteGA = setup();
		suiteGA.setFitnessFunction(fitnessFunction);
		statistics.searchStarted(suiteGA);

		ga = suiteGA;

		RandomLengthTestFactory factory = new RandomLengthTestFactory();

		// TODO: Shutdown hook?

		stopping_condition = getStoppingCondition();
		fitnessFunction.getFitness(suite);

		while (!isFinished(suite)) {
			TestChromosome test = factory.getChromosome();
			TestSuiteChromosome clone = suite.clone();
			clone.addTest(test);
			fitnessFunction.getFitness(clone);
			logger.debug("Old fitness: {}, new fitness: {}", suite.getFitness(),
			             clone.getFitness());
			if (clone.compareTo(suite) < 0) {
				suite = clone;
			}
		}
		suiteGA.getPopulation().add(suite);
		statistics.searchFinished(suiteGA);
		suiteGA.printBudget();

		if (Properties.MINIMIZE) {
			LoggingUtils.getEvoLogger().info("* Minimizing result");
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer(getFitnessFactory());
			minimizer.minimize((TestSuiteChromosome) suiteGA.getBestIndividual());
		}
		statistics.minimized(suiteGA.getBestIndividual());

		return suite.getTests();
	}

	/**
	 * Use the OneBranch approach: The budget for the search is split equally
	 * among all test goals, and then search is attempted for each goal. If a
	 * goal is covered, the remaining budget will be used in the next iteration.
	 * 
	 * @return a {@link java.util.List} object.
	 */
	public List<TestCase> generateIndividualTests() {
		// Set up search algorithm
		LoggingUtils.getEvoLogger().info("* Setting up search algorithm for individual test generation");
		ExecutionTracer.enableTraceCalls();
		if (ga == null)
			ga = setup();
		if (analyzing)
			ga.resetStoppingConditions();

		GeneticAlgorithm suiteGA = getGeneticAlgorithm(new TestSuiteChromosomeFactory());
		FitnessFunction suite_fitness = getFitnessFunction();
		suiteGA.setFitnessFunction(suite_fitness);

		if (analyzing)
			suiteGA.resetStoppingConditions();

		long start_time = System.currentTimeMillis() / 1000;
		FitnessLogger fitness_logger = new FitnessLogger();
		if (Properties.LOG_GOALS) {
			ga.addListener(fitness_logger);
		}

		// Get list of goals
		TestFitnessFactory goal_factory = getFitnessFactory();
		long goalComputationStart = System.currentTimeMillis();
		List<TestFitnessFunction> goals = goal_factory.getCoverageGoals();
		if (AbstractFitnessFactory.goalComputationTime != 0l)
			AbstractFitnessFactory.goalComputationTime = System.currentTimeMillis()
			        - goalComputationStart;
		// Need to shuffle goals because the order may make a difference
		if (Properties.SHUFFLE_GOALS) {
			//LoggingUtils.getEvoLogger().info("* Shuffling goals");
			Randomness.shuffle(goals);
		}
		if (Properties.PREORDER_GOALS_BY_DIFFICULTY) {
			orderGoalsByDifficulty(goals);
			//LoggingUtils.getEvoLogger().info("* Time taken for difficulty computation: "
			//        + DefUseCoverageTestFitness.difficulty_time + "ms");
		}// else
		 //	LoggingUtils.getEvoLogger().info("* Goal preordering by difficulty disabled!");
		 //if (!Properties.RECYCLE_CHROMOSOMES)
		 //	LoggingUtils.getEvoLogger().info("* ChromosomeRecycler disabled!");

		LoggingUtils.getEvoLogger().info("* Total number of test goals: " + goals.size());

		// Bootstrap with random testing to cover easy goals
		statistics.searchStarted(suiteGA);

		TestSuiteChromosome suite = bootstrapRandomSuite(suite_fitness, goal_factory);
		suiteGA.getPopulation().add(suite);
		Set<Integer> covered = new HashSet<Integer>();
		int covered_goals = 0;
		int num = 0;

		for (TestFitnessFunction fitness_function : goals) {
			if (fitness_function.isCovered(suite.getTests())) {
				covered.add(num);
				covered_goals++;
			}
			num++;
		}
		if (covered_goals > 0)
			LoggingUtils.getEvoLogger().info("* Random bootstrapping covered "
			                                         + covered_goals + " test goals");

		int total_goals = goals.size();
		if (covered_goals == total_goals)
			zero_fitness.setFinished();

		int current_budget = 0;

		long total_budget = Properties.SEARCH_BUDGET;
		LoggingUtils.getEvoLogger().info("* Budget: "
		                                         + NumberFormat.getIntegerInstance().format(total_budget));

		while (current_budget < total_budget && covered_goals < total_goals
		        && !global_time.isFinished() && !ShutdownTestWriter.isInterrupted()) {
			long budget = (total_budget - current_budget) / (total_goals - covered_goals);
			logger.info("Budget: " + budget + "/" + (total_budget - current_budget));
			logger.info("Statements: " + current_budget + "/" + total_budget);
			logger.info("Goals covered: " + covered_goals + "/" + total_goals);
			stopping_condition.setLimit(budget);

			num = 0;
			// int num_statements = 0;
			// //MaxStatementsStoppingCondition.getNumExecutedStatements();
			for (TestFitnessFunction fitness_function : goals) {

				if (covered.contains(num)) {
					num++;
					continue;
				}

				ga.resetStoppingConditions();
				ga.clearPopulation();
				ga.setChromosomeFactory(getChromosomeFactory(fitness_function));

				if (Properties.PRINT_CURRENT_GOALS)
					LoggingUtils.getEvoLogger().info("* Searching for goal "
					                                         + num
					                                         + ": "
					                                         + fitness_function.toString());
				logger.info("Goal " + num + "/" + (total_goals - covered_goals) + ": "
				        + fitness_function);

				if (ShutdownTestWriter.isInterrupted()) {
					num++;
					continue;
				}
				if (global_time.isFinished()) {
					LoggingUtils.getEvoLogger().info("Skipping goal because time is up");
					num++;
					continue;
				}

				// FitnessFunction fitness_function = new
				ga.setFitnessFunction(fitness_function);

				// Perform search
				logger.info("Starting evolution for goal " + fitness_function);
				ga.generateSolution();

				if (ga.getBestIndividual().getFitness() == 0.0) {
					if (Properties.PRINT_COVERED_GOALS)
						LoggingUtils.getEvoLogger().info("* Covered!"); // : " +
					// fitness_function.toString());
					logger.info("Found solution, adding to test suite at "
					        + MaxStatementsStoppingCondition.getNumExecutedStatements());
					TestChromosome best = (TestChromosome) ga.getBestIndividual();
					if (Properties.MINIMIZE && !Properties.MINIMIZE_OLD) {
						TestCaseMinimizer minimizer = new TestCaseMinimizer(
						        fitness_function);
						minimizer.minimize(best);
					}
					best.getTestCase().addCoveredGoal(fitness_function);
					suite.addTest(best);
					suiteGA.getPopulation().set(0, suite);
					// Calculate and keep track of overall fitness
					suite_fitness.getFitness(suite);

					covered_goals++;
					covered.add(num);

					// experiment:
					if (Properties.SKIP_COVERED) {
						Set<Integer> additional_covered_nums = getAdditionallyCoveredGoals(goals,
						                                                                   covered,
						                                                                   best);
						// LoggingUtils.getEvoLogger().info("Additionally covered: "+additional_covered_nums.size());
						for (Integer covered_num : additional_covered_nums) {
							covered_goals++;
							covered.add(covered_num);
						}
					}

				} else {
					logger.info("Found no solution for " + fitness_function + " at "
					        + MaxStatementsStoppingCondition.getNumExecutedStatements());
				}

				statistics.iteration(suiteGA);
				if (Properties.REUSE_BUDGET)
					current_budget += stopping_condition.getCurrentValue();
				else
					current_budget += budget + 1;

				// print console progress bar
				if (Properties.SHOW_PROGRESS
				        && !(Properties.PRINT_COVERED_GOALS || Properties.PRINT_CURRENT_GOALS)) {
					double percent = current_budget;
					percent = percent / total_budget * 100;

					double coverage = covered_goals;
					coverage = coverage / total_goals * 100;

					// ConsoleProgressBar.printProgressBar((int) percent, (int) coverage);
				}

				if (current_budget > total_budget)
					break;
				num++;

				// break;
			}
		}
		if (Properties.SHOW_PROGRESS)
			LoggingUtils.getEvoLogger().info("");

		// for testing purposes
		if (global_time.isFinished())
			LoggingUtils.getEvoLogger().info("! Timeout reached");
		if (current_budget >= total_budget)
			LoggingUtils.getEvoLogger().info("! Budget exceeded");
		else
			LoggingUtils.getEvoLogger().info("* Remaining budget: "
			                                         + (total_budget - current_budget));

		stopping_condition.setLimit(Properties.SEARCH_BUDGET);
		stopping_condition.forceCurrentValue(current_budget);
		suiteGA.setStoppingCondition(stopping_condition);
		suiteGA.addStoppingCondition(global_time);
		suiteGA.printBudget();

		if (!analyzing) {
			int c = 0;
			int uncovered_goals = total_goals - covered_goals;
			if (uncovered_goals < 10)
				for (TestFitnessFunction goal : goals) {
					if (!covered.contains(c)) {
						LoggingUtils.getEvoLogger().info("! Unable to cover goal " + c
						                                         + " " + goal.toString());
					}
					c++;
				}
			else
				LoggingUtils.getEvoLogger().info("! #Goals that were not covered: "
				                                         + uncovered_goals);
		}

		if (Properties.CRITERION == Criterion.LCSAJ && Properties.WRITE_CFG) {
			int d = 0;
			for (TestFitnessFunction goal : goals) {
				if (!covered.contains(d)) {
					LCSAJCoverageTestFitness lcsajGoal = (LCSAJCoverageTestFitness) goal;
					LCSAJ l = lcsajGoal.getLcsaj();
					LCSAJGraph uncoveredGraph = new LCSAJGraph(l, true);
					uncoveredGraph.generate(new File("evosuite-graphs/LCSAJGraphs/"
					        + l.getClassName() + "/" + l.getMethodName()
					        + "/Uncovered LCSAJ No: " + l.getID()));
				}
				d++;
			}
		}

		statistics.searchFinished(suiteGA);
		long end_time = System.currentTimeMillis() / 1000;
		LoggingUtils.getEvoLogger().info("* Search finished after "
		                                         + (end_time - start_time)
		                                         + "s, "
		                                         + current_budget
		                                         + " statements, best individual has fitness "
		                                         + suite.getFitness());

		if (!analyzing) {
			LoggingUtils.getEvoLogger().info("* Covered " + covered_goals + "/"
			                                         + goals.size() + " goals");
			logger.info("Resulting test suite: " + suite.size() + " tests, length "
			        + suite.totalLengthOfTestCases());
		} else {
			CoverageStatistics.analyzeCoverage(suite);
		}

		if (Properties.INLINE) {
			ConstantInliner inliner = new ConstantInliner();
			inliner.inline(suite);
		}

		// Generate a test suite chromosome once all test cases are done?
		if (Properties.MINIMIZE && Properties.MINIMIZE_OLD) {
			LoggingUtils.getEvoLogger().info("* Minimizing result");
			logger.info("Size before: " + suite.totalLengthOfTestCases());
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer(getFitnessFactory());
			minimizer.minimize(suite);
			logger.info("Size after: " + suite.totalLengthOfTestCases());
		}

		/*
		 * if(Properties.MINIMIZE) { LoggingUtils.getEvoLogger().info("* Minimizing result");
		 * TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
		 * minimizer.minimize(suite, suite_fitness); }
		 */
		// LoggingUtils.getEvoLogger().info("Resulting test suite has fitness "+suite.getFitness());
		LoggingUtils.getEvoLogger().info("* Resulting test suite: " + suite.size()
		                                         + " tests, length "
		                                         + suite.totalLengthOfTestCases());

		// Log some stats
		statistics.iteration(suiteGA);
		statistics.minimized(suite);

		return suite.getTests();
	}

	private void orderGoalsByDifficulty(List<TestFitnessFunction> goals) {

		Collections.sort(goals);
		// for(TestFitnessFunction goal : goals)
		// LoggingUtils.getEvoLogger().info(goal.toString());
	}

	/**
	 * Returns a list containing all positions of goals in the given goalList
	 * that are covered by the given test but not already in the given
	 * coveredSet
	 * 
	 * Used to avoid unnecessary solutionGenerations in
	 * generateIndividualTests()
	 */
	private Set<Integer> getAdditionallyCoveredGoals(List<TestFitnessFunction> goals,
	        Set<Integer> covered, TestChromosome best) {

		Set<Integer> r = new HashSet<Integer>();
		ExecutionResult result = best.getLastExecutionResult();
		assert (result != null);
		// if (result == null) {
		// result = TestCaseExecutor.getInstance().execute(best.test);
		// }
		int num = -1;
		for (TestFitnessFunction goal : goals) {
			num++;
			if (covered.contains(num))
				continue;
			if (goal.isCovered(best, result)) {
				r.add(num);
				if (Properties.PRINT_COVERED_GOALS)
					LoggingUtils.getEvoLogger().info("* Additionally covered: "
					                                         + goal.toString());
			}
		}
		return r;
	}

	/*
	 * protected List<BranchCoverageGoal> getBranches() {
	 * List<BranchCoverageGoal> goals = new ArrayList<BranchCoverageGoal>();
	 * 
	 * // Branchless methods String class_name = Properties.TARGET_CLASS;
	 * logger.info("Getting branches for "+class_name); for(String method :
	 * CFGMethodAdapter.branchless_methods) { goals.add(new
	 * BranchCoverageGoal(class_name, method));
	 * logger.info("Adding new method goal for method "+method); }
	 * 
	 * // Branches for(String className : CFGMethodAdapter.branch_map.keySet())
	 * { for(String methodName :
	 * CFGMethodAdapter.branch_map.get(className).keySet()) { // Get CFG of
	 * method ControlFlowGraph cfg =
	 * ExecutionTracer.getExecutionTracer().getCFG(className, methodName);
	 * 
	 * for(Entry<Integer,Integer> entry :
	 * CFGMethodAdapter.branch_map.get(className).get(methodName).entrySet()) {
	 * // Identify vertex in CFG goals.add(new
	 * BranchCoverageGoal(entry.getValue(), entry.getKey(), true, cfg,
	 * className, methodName)); goals.add(new
	 * BranchCoverageGoal(entry.getValue(), entry.getKey(), false, cfg,
	 * className, methodName));
	 * logger.info("Adding new branch goals for method "+methodName); }
	 * 
	 * // Approach level is measured in terms of line coverage? Or possible in
	 * terms of branches... } }
	 * 
	 * return goals; }
	 */

	/**
	 * <p>
	 * getStoppingCondition
	 * </p>
	 * 
	 * @return a {@link org.evosuite.ga.stoppingconditions.StoppingCondition}
	 *         object.
	 */
	public static StoppingCondition getStoppingCondition() {
		logger.info("Setting stopping condition: " + Properties.STOPPING_CONDITION);
		switch (Properties.STOPPING_CONDITION) {
		case MAXGENERATIONS:
			return new MaxGenerationStoppingCondition();
		case MAXFITNESSEVALUATIONS:
			return new MaxFitnessEvaluationsStoppingCondition();
		case MAXTIME:
			return new MaxTimeStoppingCondition();
		case MAXTESTS:
			return new MaxTestsStoppingCondition();
		case MAXSTATEMENTS:
			return new MaxStatementsStoppingCondition();
		default:
			logger.warn("Unknown stopping condition: " + Properties.STOPPING_CONDITION);
			return new MaxGenerationStoppingCondition();
		}
	}

	/**
	 * <p>
	 * getCrossoverFunction
	 * </p>
	 * 
	 * @return a {@link org.evosuite.ga.CrossOverFunction} object.
	 */
	public static CrossOverFunction getCrossoverFunction() {
		switch (Properties.CROSSOVER_FUNCTION) {
		case SINGLEPOINTFIXED:
			return new SinglePointFixedCrossOver();
		case SINGLEPOINTRELATIVE:
			return new SinglePointRelativeCrossOver();
		case SINGLEPOINT:
			return new SinglePointCrossOver();
		case COVERAGE:
			if (Properties.STRATEGY != Properties.Strategy.EVOSUITE)
				throw new RuntimeException(
				        "Coverage crossover function requires test suite mode");

			return new CoverageCrossOver();
		default:
			throw new RuntimeException("Unknown crossover function: "
			        + Properties.CROSSOVER_FUNCTION);
		}
	}

	/**
	 * <p>
	 * getSelectionFunction
	 * </p>
	 * 
	 * @return a {@link org.evosuite.ga.SelectionFunction} object.
	 */
	public static SelectionFunction getSelectionFunction() {
		switch (Properties.SELECTION_FUNCTION) {
		case ROULETTEWHEEL:
			return new FitnessProportionateSelection();
		case TOURNAMENT:
			return new TournamentSelection();
		default:
			return new RankSelection();
		}
	}

	/**
	 * <p>
	 * getChromosomeFactory
	 * </p>
	 * 
	 * @param fitness
	 *            a {@link org.evosuite.ga.FitnessFunction} object.
	 * @return a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	protected static ChromosomeFactory<? extends Chromosome> getChromosomeFactory(
	        FitnessFunction fitness) {

		switch (Properties.STRATEGY) {
		case EVOSUITE:
			switch (Properties.TEST_FACTORY) {
			case ALLMETHODS:
				logger.info("Using all methods chromosome factory");
				return new TestSuiteChromosomeFactory(
				        new AllMethodsTestChromosomeFactory());
			case RANDOM:
				logger.info("Using random chromosome factory");
				return new TestSuiteChromosomeFactory(new RandomLengthTestFactory());
			case TOURNAMENT:
				logger.info("Using tournament chromosome factory");
				return new TournamentChromosomeFactory<TestSuiteChromosome>(fitness,
				        new TestSuiteChromosomeFactory());
			case JUNIT:
				logger.info("Using seeding chromosome factory");
				JUnitTestChromosomeFactory factory = new JUnitTestChromosomeFactory(
				        new RandomLengthTestFactory());
				return new TestSuiteChromosomeFactory(factory);
			default:
				throw new RuntimeException("Unsupported test factory: "
				        + Properties.TEST_FACTORY);
			}
		default:
			switch (Properties.TEST_FACTORY) {
			case ALLMETHODS:
				logger.info("Using all methods chromosome factory");
				return new AllMethodsTestChromosomeFactory();
			case RANDOM:
				logger.info("Using random chromosome factory");
				return new RandomLengthTestFactory();
			case TOURNAMENT:
				logger.info("Using tournament chromosome factory");
				return new TournamentChromosomeFactory<TestChromosome>(fitness,
				        new RandomLengthTestFactory());
			case JUNIT:
				logger.info("Using seeding chromosome factory");
				return new JUnitTestChromosomeFactory(new RandomLengthTestFactory());
			default:
				throw new RuntimeException("Unsupported test factory: "
				        + Properties.TEST_FACTORY);
			}
		}
	}

	/**
	 * <p>
	 * getDefaultChromosomeFactory
	 * </p>
	 * 
	 * @return a {@link org.evosuite.ga.ChromosomeFactory} object.
	 */
	protected static ChromosomeFactory<? extends Chromosome> getDefaultChromosomeFactory() {
		switch (Properties.STRATEGY) {
		case EVOSUITE:
			return new TestSuiteChromosomeFactory(new RandomLengthTestFactory());
		default:
			return new RandomLengthTestFactory();
		}
	}

	/**
	 * <p>
	 * getSecondaryTestObjective
	 * </p>
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.ga.SecondaryObjective} object.
	 */
	public static SecondaryObjective getSecondaryTestObjective(String name) {
		if (name.equalsIgnoreCase("size"))
			return new MinimizeSizeSecondaryObjective();
		else if (name.equalsIgnoreCase("exceptions"))
			return new org.evosuite.testcase.MinimizeExceptionsSecondaryObjective();
		else
			throw new RuntimeException("ERROR: asked for unknown secondary objective \""
			        + name + "\"");
	}

	/**
	 * <p>
	 * getSecondarySuiteObjective
	 * </p>
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @return a {@link org.evosuite.ga.SecondaryObjective} object.
	 */
	public static SecondaryObjective getSecondarySuiteObjective(String name) {
		if (name.equalsIgnoreCase("size"))
			return new MinimizeSizeSecondaryObjective();
		else if (name.equalsIgnoreCase("maxlength"))
			return new MinimizeMaxLengthSecondaryObjective();
		else if (name.equalsIgnoreCase("averagelength"))
			return new MinimizeAverageLengthSecondaryObjective();
		else if (name.equalsIgnoreCase("exceptions"))
			return new MinimizeExceptionsSecondaryObjective();
		else if (name.equalsIgnoreCase("totallength"))
			return new MinimizeTotalLengthSecondaryObjective();
		else
			throw new RuntimeException("ERROR: asked for unknown secondary objective \""
			        + name + "\"");
	}

	/**
	 * <p>
	 * getSecondaryObjectives
	 * </p>
	 * 
	 * @param algorithm
	 *            a {@link org.evosuite.ga.GeneticAlgorithm} object.
	 */
	public static void getSecondaryObjectives(GeneticAlgorithm algorithm) {
		String objectives = Properties.SECONDARY_OBJECTIVE;

		// check if there are no secondary objectives to optimize
		if (objectives == null || objectives.trim().length() == 0
		        || objectives.trim().equalsIgnoreCase("none"))
			return;

		for (String name : objectives.split(":")) {
			try {
				TestChromosome.addSecondaryObjective(getSecondaryTestObjective(name.trim()));
			} catch (Throwable t) {
			} // Not all objectives make sense for tests
			TestSuiteChromosome.addSecondaryObjective(getSecondarySuiteObjective(name.trim()));
		}
	}

	/**
	 * <p>
	 * getPopulationLimit
	 * </p>
	 * 
	 * @return a {@link org.evosuite.ga.PopulationLimit} object.
	 */
	public static PopulationLimit getPopulationLimit() {
		switch (Properties.POPULATION_LIMIT) {
		case INDIVIDUALS:
			return new IndividualPopulationLimit();
		case TESTS:
			return new SizePopulationLimit();
		case STATEMENTS:
			return new StatementsPopulationLimit();
		default:
			throw new RuntimeException("Unsupported population limit");
		}
	}

	/**
	 * <p>
	 * getGeneticAlgorithm
	 * </p>
	 * 
	 * @param factory
	 *            a {@link org.evosuite.ga.ChromosomeFactory} object.
	 * @return a {@link org.evosuite.ga.GeneticAlgorithm} object.
	 */
	public static GeneticAlgorithm getGeneticAlgorithm(
	        ChromosomeFactory<? extends Chromosome> factory) {
		switch (Properties.ALGORITHM) {
		case ONEPLUSONEEA:
			logger.info("Chosen search algorithm: (1+1)EA");
			return new OnePlusOneEA(factory);
		case STEADYSTATEGA:
			logger.info("Chosen search algorithm: SteadyStateGA");
			{
				SteadyStateGA ga = new SteadyStateGA(factory);
				if (Properties.REPLACEMENT_FUNCTION == TheReplacementFunction.FITNESSREPLACEMENT) {
					//user has explicitly asked for this replacement function
					ga.setReplacementFunction(new FitnessReplacementFunction());
				} else {
					//use default
					if (Properties.STRATEGY == Strategy.EVOSUITE)
						ga.setReplacementFunction(new TestSuiteReplacementFunction());
					else
						ga.setReplacementFunction(new TestCaseReplacementFunction());
				}
				return ga;
			}
		case MUPLUSLAMBDAGA:
			logger.info("Chosen search algorithm: MuPlusLambdaGA");
			{
				MuPlusLambdaGA ga = new MuPlusLambdaGA(factory);
				if (Properties.REPLACEMENT_FUNCTION == TheReplacementFunction.FITNESSREPLACEMENT) {
					//user has explicitly asked for this replacement function
					ga.setReplacementFunction(new FitnessReplacementFunction());
				} else {
					//use default
					if (Properties.STRATEGY == Strategy.EVOSUITE)
						ga.setReplacementFunction(new TestSuiteReplacementFunction());
					else
						ga.setReplacementFunction(new TestCaseReplacementFunction());
				}
				return ga;
			}
		case RANDOM:
			logger.info("Chosen search algorithm: Random");
			return new RandomSearch(factory);
		default:
			logger.info("Chosen search algorithm: StandardGA");
			return new StandardGA(factory);
		}

	}

	/**
	 * Factory method for search algorithm
	 * 
	 * @return a {@link org.evosuite.ga.GeneticAlgorithm} object.
	 */
	public GeneticAlgorithm setup() {

		ChromosomeFactory<? extends Chromosome> factory = getDefaultChromosomeFactory();
		GeneticAlgorithm ga = getGeneticAlgorithm(factory);

		// How to select candidates for reproduction
		SelectionFunction selection_function = getSelectionFunction();
		selection_function.setMaximize(false);
		ga.setSelectionFunction(selection_function);

		// When to stop the search
		stopping_condition = getStoppingCondition();
		ga.setStoppingCondition(stopping_condition);
		// ga.addListener(stopping_condition);
		if (Properties.STOP_ZERO) {
			ga.addStoppingCondition(zero_fitness);
		}

		if (!(stopping_condition instanceof MaxTimeStoppingCondition)) {
			ga.addStoppingCondition(global_time);
		}

		if (Properties.CRITERION == Criterion.MUTATION
		        || Properties.CRITERION == Criterion.STRONGMUTATION) {
			if (Properties.STRATEGY == Strategy.ONEBRANCH)
				ga.addStoppingCondition(new MutationTimeoutStoppingCondition());
			else
				ga.addListener(new MutationTestPool());
		}
		ga.resetStoppingConditions();
		ga.setPopulationLimit(getPopulationLimit());

		// How to cross over
		CrossOverFunction crossover_function = getCrossoverFunction();
		ga.setCrossOverFunction(crossover_function);

		// What to do about bloat
		// MaxLengthBloatControl bloat_control = new MaxLengthBloatControl();
		// ga.setBloatControl(bloat_control);

		if (Properties.CHECK_BEST_LENGTH) {
			if (Properties.STRATEGY == Strategy.EVOSUITE) {
				RelativeSuiteLengthBloatControl bloat_control = new org.evosuite.testsuite.RelativeSuiteLengthBloatControl();
				ga.addBloatControl(bloat_control);
				ga.addListener(bloat_control);
			} else {
				org.evosuite.testcase.RelativeTestLengthBloatControl bloat_control = new org.evosuite.testcase.RelativeTestLengthBloatControl();
				ga.addBloatControl(bloat_control);
				ga.addListener(bloat_control);
			}
		}
		// ga.addBloatControl(new MaxLengthBloatControl());

		getSecondaryObjectives(ga);

		// Some statistics
		if (Properties.STRATEGY == Strategy.EVOSUITE)
			ga.addListener(SearchStatistics.getInstance());
		//ga.addListener(new MemoryMonitor());
		// ga.addListener(MutationStatistics.getInstance());
		// ga.addListener(BestChromosomeTracker.getInstance());

		if (Properties.DYNAMIC_LIMIT) {
			// max_s = GAProperties.generations * getBranches().size();
			// TODO: might want to make this dependent on the selected coverage
			// criterion
			// TODO also, question: is branchMap.size() really intended here?
			// I think BranchPool.getBranchCount() was intended
			Properties.SEARCH_BUDGET = Properties.SEARCH_BUDGET
			        * (BranchPool.getNumBranchlessMethods(Properties.TARGET_CLASS) + BranchPool.getBranchCountForClass(Properties.TARGET_CLASS) * 2);
			stopping_condition.setLimit(Properties.SEARCH_BUDGET);
			logger.info("Setting dynamic length limit to " + Properties.SEARCH_BUDGET);
		}

		if (Properties.SHUTDOWN_HOOK) {
			//ShutdownTestWriter writer = new ShutdownTestWriter(Thread.currentThread());
			ShutdownTestWriter writer = new ShutdownTestWriter();
			ga.addStoppingCondition(writer);

			if (Properties.STOPPING_PORT != -1) {
				SocketStoppingCondition ss = new SocketStoppingCondition();
				ss.accept();
				ga.addStoppingCondition(ss);
			}

			//Runtime.getRuntime().addShutdownHook(writer);
			Signal.handle(new Signal("INT"), writer);
		}

		ga.addListener(new ResourceController());

		return ga;
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
		if (Properties.VIRTUAL_FS) {
			try {
				FileSystem.manager = IOWrapper.initVFS();
			} catch (FileSystemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		TestSuiteGenerator generator = new TestSuiteGenerator();
		generator.generateTestSuite();
		System.exit(0);
	}

}
