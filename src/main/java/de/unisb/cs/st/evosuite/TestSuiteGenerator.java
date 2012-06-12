/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.vfs2.FileSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;
import de.unisb.cs.st.evosuite.Properties.AssertionStrategy;
import de.unisb.cs.st.evosuite.Properties.Criterion;
import de.unisb.cs.st.evosuite.Properties.Strategy;
import de.unisb.cs.st.evosuite.Properties.TheReplacementFunction;
import de.unisb.cs.st.evosuite.assertion.AssertionGenerator;
import de.unisb.cs.st.evosuite.assertion.CompleteAssertionGenerator;
import de.unisb.cs.st.evosuite.assertion.MutationAssertionGenerator;
import de.unisb.cs.st.evosuite.assertion.UnitAssertionGenerator;
import de.unisb.cs.st.evosuite.classcreation.ClassFactory;
import de.unisb.cs.st.evosuite.contracts.ContractChecker;
import de.unisb.cs.st.evosuite.contracts.FailingTestSet;
import de.unisb.cs.st.evosuite.coverage.FitnessLogger;
import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.coverage.dataflow.AllDefsCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.dataflow.AllDefsCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageTestFitness;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseFitnessCalculator;
import de.unisb.cs.st.evosuite.coverage.exception.ExceptionCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJ;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJCoverageTestFitness;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationFactory;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationPool;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationTestPool;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import de.unisb.cs.st.evosuite.coverage.mutation.StrongMutationSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.mutation.WeakMutationSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.path.PrimePathCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.path.PrimePathSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.statement.StatementCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.statement.StatementCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.ga.CrossOverFunction;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.FitnessProportionateSelection;
import de.unisb.cs.st.evosuite.ga.FitnessReplacementFunction;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.IndividualPopulationLimit;
import de.unisb.cs.st.evosuite.ga.MinimizeSizeSecondaryObjective;
import de.unisb.cs.st.evosuite.ga.MuPlusLambdaGA;
import de.unisb.cs.st.evosuite.ga.OnePlusOneEA;
import de.unisb.cs.st.evosuite.ga.PopulationLimit;
import de.unisb.cs.st.evosuite.ga.RandomSearch;
import de.unisb.cs.st.evosuite.ga.RankSelection;
import de.unisb.cs.st.evosuite.ga.SecondaryObjective;
import de.unisb.cs.st.evosuite.ga.SelectionFunction;
import de.unisb.cs.st.evosuite.ga.SinglePointCrossOver;
import de.unisb.cs.st.evosuite.ga.SinglePointFixedCrossOver;
import de.unisb.cs.st.evosuite.ga.SinglePointRelativeCrossOver;
import de.unisb.cs.st.evosuite.ga.SizePopulationLimit;
import de.unisb.cs.st.evosuite.ga.StandardGA;
import de.unisb.cs.st.evosuite.ga.SteadyStateGA;
import de.unisb.cs.st.evosuite.ga.TournamentChromosomeFactory;
import de.unisb.cs.st.evosuite.ga.TournamentSelection;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.GlobalTimeStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxFitnessEvaluationsStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxGenerationStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition;
import de.unisb.cs.st.evosuite.graphs.LCSAJGraph;
import de.unisb.cs.st.evosuite.io.IOWrapper;
import de.unisb.cs.st.evosuite.junit.TestSuiteWriter;
import de.unisb.cs.st.evosuite.primitives.ObjectPool;
import de.unisb.cs.st.evosuite.runtime.FileSystem;
import de.unisb.cs.st.evosuite.sandbox.PermissionStatistics;
import de.unisb.cs.st.evosuite.testcase.AllMethodsTestChromosomeFactory;
import de.unisb.cs.st.evosuite.testcase.ConstantInliner;
import de.unisb.cs.st.evosuite.testcase.DefaultTestCase;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.JUnitTestChromosomeFactory;
import de.unisb.cs.st.evosuite.testcase.RandomLengthTestFactory;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testcase.TestCaseMinimizer;
import de.unisb.cs.st.evosuite.testcase.TestCaseReplacementFunction;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testcase.ValueMinimizer;
import de.unisb.cs.st.evosuite.testsuite.AbstractFitnessFactory;
import de.unisb.cs.st.evosuite.testsuite.CoverageCrossOver;
import de.unisb.cs.st.evosuite.testsuite.CoverageStatistics;
import de.unisb.cs.st.evosuite.testsuite.FixedSizeTestSuiteChromosomeFactory;
import de.unisb.cs.st.evosuite.testsuite.MinimizeAverageLengthSecondaryObjective;
import de.unisb.cs.st.evosuite.testsuite.MinimizeExceptionsSecondaryObjective;
import de.unisb.cs.st.evosuite.testsuite.MinimizeMaxLengthSecondaryObjective;
import de.unisb.cs.st.evosuite.testsuite.MinimizeTotalLengthSecondaryObjective;
import de.unisb.cs.st.evosuite.testsuite.RelativeSuiteLengthBloatControl;
import de.unisb.cs.st.evosuite.testsuite.SearchStatistics;
import de.unisb.cs.st.evosuite.testsuite.StatementsPopulationLimit;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosomeFactory;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteMinimizer;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteReplacementFunction;
import de.unisb.cs.st.evosuite.utils.LoggingUtils;
import de.unisb.cs.st.evosuite.utils.Randomness;
import de.unisb.cs.st.evosuite.utils.ResourceController;
import de.unisb.cs.st.evosuite.utils.Utils;

/**
 * Main entry point
 * 
 * @author Gordon Fraser
 * 
 */
@SuppressWarnings("restriction")
public class TestSuiteGenerator {

	private static Logger logger = LoggerFactory.getLogger(TestSuiteGenerator.class);

	private final SearchStatistics statistics = SearchStatistics.getInstance();

	public final static ZeroFitnessStoppingCondition zero_fitness = new ZeroFitnessStoppingCondition();

	public static final GlobalTimeStoppingCondition global_time = new GlobalTimeStoppingCondition();

	public static StoppingCondition stopping_condition;
	public static boolean analyzing = false;

	/*
	 * FIXME: a field is needed for "ga" to avoid a large re-factoring of the code.
	 * "ga" is given as input to many functions, but there are side-effects
	 * like "ga = setup()" that are not propagated to the "ga" reference
	 * of the top-function caller
	 */
	private GeneticAlgorithm ga;

	/**
	 * Generate a test suite for the target class
	 */
	public String generateTestSuite() {

		TestCaseExecutor.initExecutor();

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
			if (Properties.CRITERION == Criterion.MUTATION
			        || Properties.CRITERION == Criterion.STRONGMUTATION) {
				handleMutations(tests);
			} else {
				// If we're not using mutation testing, we need to re-instrument
				addAssertions(tests);
			}
		}

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
			suite.writeTestSuiteMainFile(testDir);
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
			suite.writeTestSuiteMainFile(testDir);
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

				TestCluster.getInstance().resetCluster();

				// TODO: Now all existing test cases have reflection objects pointing to the wrong classloader
				for (TestCase test : tests) {
					DefaultTestCase dtest = (DefaultTestCase) test;
					dtest.changeClassLoader(TestCluster.classLoader);
				}
			}
			MutationAssertionGenerator masserter = new MutationAssertionGenerator();
			Set<Integer> tkilled = new HashSet<Integer>();
			for (TestCase test : tests) {
				//Set<Integer> killed = new HashSet<Integer>();
				masserter.addAssertions(test, tkilled);
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
		for (TestCase test : tests) {
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
	 * Use the EvoSuite approach (Whole test suite generation)
	 * 
	 * @return
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
		ga.setChromosomeFactory(getChromosomeFactory(fitness_function));
		//if (Properties.SHOW_PROGRESS && !logger.isInfoEnabled())
		ga.addListener(new ProgressMonitor());

		if (Properties.CRITERION == Criterion.DEFUSE
		        || Properties.CRITERION == Criterion.ALLDEFS
		        || Properties.CRITERION == Criterion.STATEMENT)
			ExecutionTrace.enableTraceCalls();

		//TODO: why it was only if "analyzing"???
		//if (analyzing)
		ga.resetStoppingConditions();

		TestFitnessFactory goal_factory = getFitnessFactory();
		List<TestFitnessFunction> goals = goal_factory.getCoverageGoals();
		LoggingUtils.getEvoLogger().info("* Total number of test goals: " + goals.size());

		// Perform search
		LoggingUtils.getEvoLogger().info("* Starting evolution");
		ga.generateSolution();

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		long end_time = System.currentTimeMillis() / 1000;
		LoggingUtils.getEvoLogger().info("* Search finished after "
		                                         + (end_time - start_time)
		                                         + "s and "
		                                         + ga.getAge()
		                                         + " generations, "
		                                         + MaxStatementsStoppingCondition.getNumExecutedStatements()
		                                         + " statements, best individual has fitness "
		                                         + best.getFitness());

		double fitness = best.getFitness();

		if (Properties.MINIMIZE_VALUES) {
			LoggingUtils.getEvoLogger().info("* Minimizing values");
			ValueMinimizer minimizer = new ValueMinimizer();
			minimizer.minimize(best, (TestSuiteFitnessFunction) fitness_function);
			assert (fitness >= best.getFitness());
		}

		if (Properties.INLINE) {
			ConstantInliner inliner = new ConstantInliner();
			inliner.inline(best);
			assert (fitness >= best.getFitness());
		}

		if (Properties.MINIMIZE) {
			LoggingUtils.getEvoLogger().info("* Minimizing result");
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer(getFitnessFactory());
			minimizer.minimize((TestSuiteChromosome) ga.getBestIndividual());
		}

		statistics.iteration(ga);
		statistics.minimized(ga.getBestIndividual());
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

	public static TestSuiteFitnessFunction getFitnessFunction() {
		return getFitnessFunction(Properties.CRITERION);
	}

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
		default:
			logger.warn("No TestSuiteFitnessFunction defined for " + Properties.CRITERION
			        + " using default one (BranchCoverageSuiteFitness)");
			return new BranchCoverageSuiteFitness();
		}
	}

	public static TestFitnessFactory getFitnessFactory() {
		return getFitnessFactory(Properties.CRITERION);
	}

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
	 * @return
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
	 * @return
	 */
	public List<TestCase> generateIndividualTests() {
		// Set up search algorithm
		LoggingUtils.getEvoLogger().info("* Setting up search algorithm for individual test generation");
		ExecutionTrace.enableTraceCalls();
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

	private static CrossOverFunction getCrossoverFunction() {
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

	protected static ChromosomeFactory<? extends Chromosome> getDefaultChromosomeFactory() {
		switch (Properties.STRATEGY) {
		case EVOSUITE:
			return new TestSuiteChromosomeFactory(new RandomLengthTestFactory());
		default:
			return new RandomLengthTestFactory();
		}
	}

	public static SecondaryObjective getSecondaryTestObjective(String name) {
		if (name.equalsIgnoreCase("size"))
			return new MinimizeSizeSecondaryObjective();
		else if (name.equalsIgnoreCase("exceptions"))
			return new de.unisb.cs.st.evosuite.testcase.MinimizeExceptionsSecondaryObjective();
		else
			throw new RuntimeException("ERROR: asked for unknown secondary objective \""
			        + name + "\"");
	}

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
	 * @return
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
				RelativeSuiteLengthBloatControl bloat_control = new de.unisb.cs.st.evosuite.testsuite.RelativeSuiteLengthBloatControl();
				ga.addBloatControl(bloat_control);
				ga.addListener(bloat_control);
			} else {
				de.unisb.cs.st.evosuite.testcase.RelativeTestLengthBloatControl bloat_control = new de.unisb.cs.st.evosuite.testcase.RelativeTestLengthBloatControl();
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

			//Runtime.getRuntime().addShutdownHook(writer);
			Signal.handle(new Signal("INT"), writer);
		}

		ga.addListener(new ResourceController());
		
		return ga;
	}

	/**
	 * @param args
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
