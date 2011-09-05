/*
 * Copyright (C) 2010 Saarland University
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
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite;

import java.io.File;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;
import de.unisb.cs.st.evosuite.Properties.AssertionStrategy;
import de.unisb.cs.st.evosuite.Properties.Criterion;
import de.unisb.cs.st.evosuite.Properties.Strategy;
import de.unisb.cs.st.evosuite.assertion.AssertionGenerator;
import de.unisb.cs.st.evosuite.assertion.CompleteAssertionGenerator;
import de.unisb.cs.st.evosuite.assertion.MutationAssertionGenerator;
import de.unisb.cs.st.evosuite.assertion.UnitAssertionGenerator;
import de.unisb.cs.st.evosuite.cfg.LCSAJGraph;
import de.unisb.cs.st.evosuite.classcreation.ClassFactory;
import de.unisb.cs.st.evosuite.coverage.FitnessLogger;
import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.coverage.concurrency.ConcurrencySuitCoverage;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageTestFitness;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseExecutionTraceAnalyzer;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseFitnessCalculator;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJ;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJCoverageTestFitness;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationFactory;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationPool;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationTimeoutStoppingCondition;
import de.unisb.cs.st.evosuite.coverage.path.PrimePathCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.path.PrimePathSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.statement.StatementCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.statement.StatementCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.ga.CrossOverFunction;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.FitnessProportionateSelection;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.MinimizeSizeSecondaryObjective;
import de.unisb.cs.st.evosuite.ga.MuPlusLambdaGA;
import de.unisb.cs.st.evosuite.ga.OnePlusOneEA;
import de.unisb.cs.st.evosuite.ga.RankSelection;
import de.unisb.cs.st.evosuite.ga.SecondaryObjective;
import de.unisb.cs.st.evosuite.ga.SelectionFunction;
import de.unisb.cs.st.evosuite.ga.SinglePointCrossOver;
import de.unisb.cs.st.evosuite.ga.SinglePointFixedCrossOver;
import de.unisb.cs.st.evosuite.ga.SinglePointRelativeCrossOver;
import de.unisb.cs.st.evosuite.ga.StandardGA;
import de.unisb.cs.st.evosuite.ga.SteadyStateGA;
import de.unisb.cs.st.evosuite.ga.TournamentSelection;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.GlobalTimeStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxFitnessEvaluationsStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxGenerationStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition;
import de.unisb.cs.st.evosuite.junit.TestSuite;
import de.unisb.cs.st.evosuite.primitives.ObjectPool;
import de.unisb.cs.st.evosuite.sandbox.PermissionStatistics;
import de.unisb.cs.st.evosuite.testcase.ConstantInliner;
import de.unisb.cs.st.evosuite.testcase.DefaultTestCase;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
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
import de.unisb.cs.st.evosuite.testsuite.CoverageStatistics;
import de.unisb.cs.st.evosuite.testsuite.FixedSizeTestSuiteChromosomeFactory;
import de.unisb.cs.st.evosuite.testsuite.MinimizeAverageLengthSecondaryObjective;
import de.unisb.cs.st.evosuite.testsuite.MinimizeExceptionsSecondaryObjective;
import de.unisb.cs.st.evosuite.testsuite.MinimizeMaxLengthSecondaryObjective;
import de.unisb.cs.st.evosuite.testsuite.MinimizeTotalLengthSecondaryObjective;
import de.unisb.cs.st.evosuite.testsuite.RelativeSuiteLengthBloatControl;
import de.unisb.cs.st.evosuite.testsuite.SearchStatistics;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosomeFactory;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunc_to_TestFitnessFactory_Adapter;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteMinimizer;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteReplacementFunction;
import de.unisb.cs.st.evosuite.utils.Randomness;
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

	private final ZeroFitnessStoppingCondition zero_fitness = new ZeroFitnessStoppingCondition();

	private final GlobalTimeStoppingCondition global_time = new GlobalTimeStoppingCondition();

	private StoppingCondition stopping_condition;

	public static boolean analyzing = false;

	/**
	 * Generate a test suite for the target class
	 */
	public String generateTestSuite(GeneticAlgorithm ga) {
		Utils.addURL(ClassFactory.getStubDir() + "/classes/");

		System.out.println("* Generating tests for class " + Properties.TARGET_CLASS);
		printTestCriterion();

		if (Properties.CRITERION == Criterion.ANALYZE)
			analyzeCriteria(ga);
		else
			generateTests(ga);

		TestCaseExecutor.pullDown();
		statistics.writeReport();

		PermissionStatistics.getInstance().printStatistics();

		System.out.println("* Done!");
		System.out.println();

		return "";
	}

	private void analyzeCriteria(GeneticAlgorithm ga) {
		analyzing = true;
		for (Criterion criterion : CoverageStatistics.supportedCriteria) {
			Properties.CRITERION = criterion;
			System.out.println("* Analyzing Criterion: " + Properties.CRITERION);
			generateTests(ga);

			// TODO reset method?
			TestCaseExecutor.timeExecuted = 0l;
			AbstractFitnessFactory.goalComputationTime = 0l;
			GlobalTimeStoppingCondition.forceReset();
		}
		Properties.CRITERION = Criterion.ANALYZE;
		CoverageStatistics.computeCombinedCoverages();
		CoverageStatistics.writeCSV();
	}

	private List<TestCase> generateTests(GeneticAlgorithm ga) {
		List<TestCase> tests;
		if (Properties.STRATEGY == Strategy.EVOSUITE)
			tests = generateWholeSuite(ga);
		else
			tests = generateIndividualTests(ga);

		System.out.println("* Time spent executing tests: "
		        + TestCaseExecutor.timeExecuted + "ms");

		if (Properties.CRITERION == Criterion.DEFUSE) {
			if (Properties.ENABLE_ALTERNATIVE_FITNESS_CALCULATION)
				System.out.println("* Time spent calculating alternative fitness: "
				        + DefUseFitnessCalculator.alternativeTime + "ms");
			System.out.println("* Time spent calculating single fitnesses: "
			        + DefUseCoverageTestFitness.singleFitnessTime + "ms");
		}

		if (Properties.CRITERION == Criterion.MUTATION) {
			System.out.println("* Generating assertions");
			handleMutations(tests);
		} else if (Properties.ASSERTIONS) {
			System.out.println("* Generating assertions");
			addAssertions(tests);
		}

		writeJUnitTests(tests);

		writeObjectPool(tests);

		if (analyzing)
			System.out.println();

		return tests;
	}

	public static void writeJUnitTests(List<TestCase> tests) {
		if (Properties.JUNIT_TESTS) {
			TestSuite suite = new TestSuite(tests);
			String name = Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.lastIndexOf(".") + 1);
			String testDir = Properties.TEST_DIR;// + "/" + Properties.CRITERION;
			System.out.println("* Writing JUnit test cases to " + testDir);
			suite.writeTestSuite("Test" + name, testDir);
		}
	}

	private void addAssertions(List<TestCase> tests) {
		AssertionGenerator asserter;
		if (Properties.ASSERTION_STRATEGY == AssertionStrategy.MUTATION) {
			Criterion oldCriterion = Properties.CRITERION;
			if (Properties.CRITERION != Criterion.MUTATION) {
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
			System.out.println("* Resulting test suite's mutation score: "
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
			Set<Integer> killed = new HashSet<Integer>();
			asserter.addAssertions(test, killed);
			tkilled.addAll(killed);
		}
		asserter.writeStatistics();
		//System.out.println("Killed: " + tkilled.size() + "/" + asserter.numMutants());
	}

	private void writeObjectPool(List<TestCase> tests) {
		if (Properties.WRITE_POOL) {
			System.out.println("* Writing sequences to pool");
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
	public List<TestCase> generateWholeSuite(GeneticAlgorithm ga) {
		// Set up search algorithm
		if (ga == null || ga.getAge() == 0) {
			System.out.println("* Setting up search algorithm for whole suite generation");
			ga = setup();
		} else {
			System.out.println("* Resuming search algorithm at generation " + ga.getAge()
			        + " for whole suite generation");
		}
		long start_time = System.currentTimeMillis() / 1000;

		// What's the search target
		FitnessFunction fitness_function = getFitnessFunction();
		ga.setFitnessFunction(fitness_function);
		if (Properties.SHOW_PROGRESS && !logger.isInfoEnabled())
			ga.addListener(new ConsoleProgressBar());

		if (Properties.CRITERION == Criterion.DEFUSE
		        || Properties.CRITERION == Criterion.STATEMENT)
			ExecutionTrace.enableTraceCalls();

		if (analyzing)
			ga.resetStoppingConditions();

		TestFitnessFactory goal_factory = getFitnessFactory();
		List<TestFitnessFunction> goals = goal_factory.getCoverageGoals();
		System.out.println("* Total number of test goals: " + goals.size());

		// Perform search
		System.out.println("* Starting evolution");
		ga.generateSolution();

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		long end_time = System.currentTimeMillis() / 1000;
		System.out.println("* Search finished after " + (end_time - start_time)
		        + "s and " + ga.getAge() + " generations, "
		        + MaxStatementsStoppingCondition.getNumExecutedStatements()
		        + " statements, best individual has fitness " + best.getFitness());

		if (Properties.MINIMIZE_VALUES) {
			System.out.println("* Minimizing values");
			ValueMinimizer minimizer = new ValueMinimizer();
			minimizer.minimize(best, (TestSuiteFitnessFunction) fitness_function);
		}

		if (Properties.INLINE) {
			ConstantInliner inliner = new ConstantInliner();
			inliner.inline(best);
		}

		if (Properties.MINIMIZE) {
			System.out.println("* Minimizing result");
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer(getFitnessFactory());
			minimizer.minimize((TestSuiteChromosome) ga.getBestIndividual());
		}
		statistics.iteration(ga);
		statistics.minimized(ga.getBestIndividual());
		System.out.println("* Generated " + best.size() + " tests with total length "
		        + best.totalLengthOfTestCases());

		if (analyzing)
			CoverageStatistics.analyzeCoverage(best);
		else {
			System.out.println("* Resulting test suite's coverage: "
			        + NumberFormat.getPercentInstance().format(best.getCoverage()));
		}

		ga.printBudget();

		if (Properties.CRITERION == Criterion.DEFUSE) {
			int covered = DefUseCoverageSuiteFitness.mostCoveredGoals;
			int total = DefUseCoverageSuiteFitness.totalGoals;
			System.out.println("* Covered " + covered + "/" + total + " goals");
			System.out.println("* Time spent optimizing covered goals analysis: "
			        + DefUseExecutionTraceAnalyzer.timeGetCoveredGoals + "ms");
		}

		return best.getTests();
	}

	private void printTestCriterion() {
		switch (Properties.CRITERION) {
		case MUTATION:
			System.out.println("* Test criterion: Mutation testing");
			break;
		case LCSAJ:
			System.out.println("* Test criterion: LCSAJ");
			break;
		case DEFUSE:
			System.out.println("* Test criterion: All DU Pairs");
			break;
		case PATH:
			System.out.println("* Test criterion: Prime Path");
			break;
		case CONCURRENCY:
			System.out.println("* Test criterion: Concurrent Test Case *");
			break;
		case STATEMENT:
			System.out.println("* Test Criterion: Statement Coverage");
			break;
		case ANALYZE:
			System.out.println("* Test Criterion: Analyzing");
			break;
		default:
			System.out.println("* Test criterion: Branch coverage");
		}
	}

	public static TestSuiteFitnessFunction getFitnessFunction() {
		return getFitnessFunction(Properties.CRITERION);
	}

	public static TestSuiteFitnessFunction getFitnessFunction(Criterion criterion) {
		switch (criterion) {
		case MUTATION:
			return new MutationSuiteFitness();
		case LCSAJ:
			return new LCSAJCoverageSuiteFitness();
		case DEFUSE:
			return new DefUseCoverageSuiteFitness();
		case PATH:
			return new PrimePathSuiteFitness();
		case CONCURRENCY:
			return new ConcurrencySuitCoverage();
		case BRANCH:
			return new BranchCoverageSuiteFitness();
		case STATEMENT:
			return new StatementCoverageSuiteFitness();
		default:
			logger.warn("No TestSuiteFitnessFunction defined for " + Properties.CRITERION
			        + " using default one (BranchCoverageSuiteFitness)");
			return new BranchCoverageSuiteFitness();
		}
	}

	public static TestFitnessFactory getFitnessFactory() {
		switch (Properties.CRITERION) {
		case MUTATION:
			return new MutationFactory();
		case LCSAJ:
			return new LCSAJCoverageFactory();
		case DEFUSE:
			return new DefUseCoverageFactory();
		case PATH:
			return new PrimePathCoverageFactory();
		case BRANCH:
			return new BranchCoverageFactory();
		case CONCURRENCY:
			return new TestSuiteFitnessFunc_to_TestFitnessFactory_Adapter(
			        new ConcurrencySuitCoverage());
		case STATEMENT:
			return new StatementCoverageFactory();
		default:
			logger.warn("No TestFitnessFactory defined for " + Properties.CRITERION
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

		if (Properties.CRITERION == Criterion.DEFUSE) {
			System.out.println("* Disabled random bootstraping for DefUseCoverage-Criterion");
			Properties.RANDOM_TESTS = 0;
		}

		if (Properties.RANDOM_TESTS > 0) {
			System.out.println("* Bootstrapping initial random test suite");
		} else
			System.out.println("* Bootstrapping initial random test suite disabled!");

		FixedSizeTestSuiteChromosomeFactory factory = new FixedSizeTestSuiteChromosomeFactory(
		        Properties.RANDOM_TESTS);

		TestSuiteChromosome suite = factory.getChromosome();
		if (Properties.RANDOM_TESTS > 0) {
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer(goals);
			minimizer.minimize(suite);
			System.out.println("* Initial test suite contains " + suite.size() + " tests");
		}

		return suite;
	}

	/**
	 * Use the OneBranch approach: The budget for the search is split equally
	 * among all test goals, and then search is attempted for each goal. If a
	 * goal is covered, the remaining budget will be used in the next iteration.
	 * 
	 * @return
	 */
	public List<TestCase> generateIndividualTests(GeneticAlgorithm ga) {
		// Set up search algorithm
		System.out.println("* Setting up search algorithm for individual test generation");
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
			System.out.println("* Shuffling goals");
			Randomness.shuffle(goals);
		}
		if (Properties.PREORDER_GOALS_BY_DIFFICULTY) {
			orderGoalsByDifficulty(goals);
			System.out.println("* Time taken for difficulty computation: "
			        + DefUseCoverageTestFitness.difficulty_time + "ms");
		} else
			System.out.println("* Goal preordering by difficulty disabled!");
		if (!Properties.RECYCLE_CHROMOSOMES)
			System.out.println("* ChromosomeRecycler disabled!");

		System.out.println("* Total number of test goals: " + goals.size());

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
			System.out.println("* Random bootstrapping covered " + covered_goals
			        + " test goals");

		int total_goals = goals.size();
		if (covered_goals == total_goals)
			zero_fitness.setFinished();

		int current_budget = 0;

		long total_budget = Properties.GENERATIONS;
		System.out.println("* Budget: "
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

				if (Properties.PRINT_CURRENT_GOALS)
					System.out.println("* Searching for goal " + num + ": "
					        + fitness_function.toString());
				logger.info("Goal " + num + "/" + (total_goals - covered_goals) + ": "
				        + fitness_function);

				if (ShutdownTestWriter.isInterrupted()) {
					num++;
					continue;
				}
				if (global_time.isFinished()) {
					System.out.println("Skipping goal because time is up");
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
						System.out.println("* Covered!"); // : " +
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
						// System.out.println("Additionally covered: "+additional_covered_nums.size());
						for (Integer covered_num : additional_covered_nums) {
							covered_goals++;
							covered.add(covered_num);
						}
					}

				} else {
					logger.info("Found no solution at "
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

					ConsoleProgressBar.printProgressBar((int) percent, (int) coverage);
				}

				if (current_budget > total_budget)
					break;
				num++;

				// break;
			}
		}
		if (Properties.SHOW_PROGRESS)
			System.out.println();

		// for testing purposes
		if (global_time.isFinished())
			System.out.println("! Timeout reached");
		if (current_budget >= total_budget)
			System.out.println("! Budget exceeded");
		else
			System.out.println("* Remaining budget: " + (total_budget - current_budget));

		ga.printBudget();

		if (!analyzing) {
			int c = 0;
			int uncovered_goals = total_goals - covered_goals;
			if (uncovered_goals < 10)
				for (TestFitnessFunction goal : goals) {
					if (!covered.contains(c)) {
						System.out.println("! Unable to cover goal " + c + " "
						        + goal.toString());
					}
					c++;
				}
			else
				System.out.println("! #Goals that were not covered: " + uncovered_goals);
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
		System.out.println("* Search finished after " + (end_time - start_time) + "s, "
		        + current_budget + " statements, best individual has fitness "
		        + suite.getFitness());

		if (!analyzing) {
			System.out.println("* Covered " + covered_goals + "/" + goals.size()
			        + " goals");
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
			System.out.println("* Minimizing result");
			logger.info("Size before: " + suite.totalLengthOfTestCases());
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer(getFitnessFactory());
			minimizer.minimize(suite);
			logger.info("Size after: " + suite.totalLengthOfTestCases());
		}

		/*
		 * if(Properties.MINIMIZE) { System.out.println("* Minimizing result");
		 * TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
		 * minimizer.minimize(suite, suite_fitness); }
		 */
		// System.out.println("Resulting test suite has fitness "+suite.getFitness());
		System.out.println("* Resulting test suite: " + suite.size() + " tests, length "
		        + suite.totalLengthOfTestCases());

		// Log some stats

		statistics.iteration(suiteGA);
		statistics.minimized(suite);

		return suite.getTests();
	}

	private void orderGoalsByDifficulty(List<TestFitnessFunction> goals) {

		Collections.sort(goals);
		// for(TestFitnessFunction goal : goals)
		// System.out.println(goal.toString());
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
					System.out.println("* Additionally covered: " + goal.toString());
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

	protected static ChromosomeFactory<? extends Chromosome> getChromosomeFactory() {
		switch (Properties.STRATEGY) {
		case EVOSUITE:
			return new TestSuiteChromosomeFactory();
		default:
			return new RandomLengthTestFactory();
		}
	}

	private static SecondaryObjective getSecondaryObjective(String name) {
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

	private static void getSecondaryObjectives(GeneticAlgorithm algorithm) {
		if (Properties.STRATEGY == Strategy.ONEBRANCH) {
			SecondaryObjective objective = getSecondaryObjective("size");
			Chromosome.addSecondaryObjective(objective);
			algorithm.addSecondaryObjective(objective);
		} else {
			String objectives = Properties.SECONDARY_OBJECTIVE;

			// check if there are no secondary objectives to optimize
			if (objectives == null || objectives.trim().length() == 0
			        || objectives.trim().equalsIgnoreCase("none"))
				return;

			for (String name : objectives.split(":")) {
				SecondaryObjective objective = getSecondaryObjective(name.trim());
				Chromosome.addSecondaryObjective(objective);
				algorithm.addSecondaryObjective(objective);
			}
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
				if (Properties.STRATEGY == Strategy.EVOSUITE)
					ga.setReplacementFunction(new TestSuiteReplacementFunction());
				else
					ga.setReplacementFunction(new TestCaseReplacementFunction());
				return ga;
			}
		case MUPLUSLAMBDAGA:
			logger.info("Chosen search algorithm: MuPlusLambdaGA");
			{
				MuPlusLambdaGA ga = new MuPlusLambdaGA(factory);
				if (Properties.STRATEGY == Strategy.EVOSUITE)
					ga.setReplacementFunction(new TestSuiteReplacementFunction());
				else
					ga.setReplacementFunction(new TestCaseReplacementFunction());
				return ga;
			}

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

		ChromosomeFactory<? extends Chromosome> factory = getChromosomeFactory();
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
		if (!(stopping_condition instanceof MaxTimeStoppingCondition))
			ga.addStoppingCondition(global_time);
		if (Properties.CRITERION == Criterion.MUTATION)
			ga.addStoppingCondition(new MutationTimeoutStoppingCondition());

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
		// ga.addListener(MutationStatistics.getInstance());
		// ga.addListener(BestChromosomeTracker.getInstance());

		if (Properties.DYNAMIC_LIMIT) {
			// max_s = GAProperties.generations * getBranches().size();
			// TODO: might want to make this dependent on the selected coverage
			// criterion
			// TODO also, question: is branchMap.size() really intended here?
			// I think BranchPool.getBranchCount() was intended
			Properties.GENERATIONS = Properties.GENERATIONS
			        * (BranchPool.getBranchlessMethods().size() + BranchPool.getBranchCounter() * 2);
			stopping_condition.setLimit(Properties.GENERATIONS);
			logger.info("Setting dynamic length limit to " + Properties.GENERATIONS);
		}

		if (Properties.SHUTDOWN_HOOK) {
			ShutdownTestWriter writer = new ShutdownTestWriter();
			ga.addStoppingCondition(writer);
			Signal.handle(new Signal("INT"), writer);
		}

		return ga;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		TestSuiteGenerator generator = new TestSuiteGenerator();
		generator.generateTestSuite(null);
	}

}
