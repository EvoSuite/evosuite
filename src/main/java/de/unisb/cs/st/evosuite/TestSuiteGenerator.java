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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.assertion.AssertionGenerator;
import de.unisb.cs.st.evosuite.coverage.FitnessLogger;
import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.ga.ChromosomeRecycler;
import de.unisb.cs.st.evosuite.ga.CrossOverFunction;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.FitnessProportionateSelection;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.MinimizeSizeSecondaryObjective;
import de.unisb.cs.st.evosuite.ga.MuPlusLambdaGA;
import de.unisb.cs.st.evosuite.ga.OnePlusOneEA;
import de.unisb.cs.st.evosuite.ga.Randomness;
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
import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxTimeStoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.StoppingCondition;
import de.unisb.cs.st.evosuite.ga.stoppingconditions.ZeroFitnessStoppingCondition;
import de.unisb.cs.st.evosuite.junit.TestSuite;
import de.unisb.cs.st.evosuite.mutation.MutationGoalFactory;
import de.unisb.cs.st.evosuite.mutation.MutationSuiteFitness;
import de.unisb.cs.st.evosuite.mutation.MutationTimeoutStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.MaxTestsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.RandomLengthTestFactory;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testcase.TestCaseMinimizer;
import de.unisb.cs.st.evosuite.testcase.TestCaseReplacementFunction;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.MinimizeMaxLengthSecondaryObjective;
import de.unisb.cs.st.evosuite.testsuite.MinimizeTotalLengthSecondaryObjective;
import de.unisb.cs.st.evosuite.testsuite.RelativeLengthBloatControl;
import de.unisb.cs.st.evosuite.testsuite.SearchStatistics;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosomeFactory;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteMinimizer;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteReplacementFunction;

/**
 * Main entry point
 * 
 * @author Gordon Fraser
 * 
 */
public class TestSuiteGenerator {

	private static Logger logger = Logger.getLogger(TestSuiteGenerator.class);

	private final SearchStatistics statistics = SearchStatistics.getInstance();

	private final ZeroFitnessStoppingCondition zero_fitness = new ZeroFitnessStoppingCondition();

	private StoppingCondition stopping_condition;
	
	/**
	 * Generate a test suite for the target class
	 */
	public void generateTestSuite() {
		List<TestCase> tests;

		System.out.println("* Generating tests for class " + Properties.TARGET_CLASS);

		if (Properties.STRATEGY.equals("EvoSuite"))
			tests = generateWholeSuite();
		else
			tests = generateIndividualTests();

		if (Properties.MUTATION) {
			AssertionGenerator asserter = new AssertionGenerator();
			Set<Long> killed = new HashSet<Long>();
			for (TestCase test : tests) {
				asserter.addAssertions(test, killed);
			}
			asserter.writeStatistics();
			System.out.println("Killed: " + killed.size() + "/" + asserter.numMutants());

		}

		if (Properties.getPropertyOrDefault("junit_tests", true)) {
			TestSuite suite = new TestSuite(tests);
			String name = Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.lastIndexOf(".") + 1);
			System.out.println("* Writing JUnit test cases to " + Properties.TEST_DIR);
			suite.writeTestSuite("Test" + name, Properties.TEST_DIR);
		}

		TestCaseExecutor.pullDown();
		statistics.writeReport();
		System.out.println("* Done!");
	}

	/**
	 * Use the EvoSuite approach (Whole test suite generation)
	 * 
	 * @return
	 */
	public List<TestCase> generateWholeSuite() {
		// Set up search algorithm
		System.out.println("* Setting up search algorithm for whole suite generation");
		GeneticAlgorithm ga = setup();
		long start_time = System.currentTimeMillis() / 1000;

		// What's the search target
		FitnessFunction fitness_function = getFitnessFunction();
		ga.setFitnessFunction(fitness_function);

		if(Properties.CRITERION.equals("defuse"))
			ExecutionTrace.enableTraceCalls();
		
		// Perform search
		System.out.println("* Starting evolution");
		ga.generateSolution();

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		long end_time = System.currentTimeMillis() / 1000;
		System.out.println("* Search finished after " + (end_time - start_time)
		        + "s and "+ga.getAge()+" generations, best individual has fitness " + best.getFitness());

		if (Properties.MINIMIZE) {
			System.out.println("* Minimizing result");
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer(getFitnessFactory());
			minimizer.minimize((TestSuiteChromosome) ga.getBestIndividual());
		}
		statistics.iteration(ga.getPopulation());
		statistics.minimized(ga.getBestIndividual());
		System.out.println("* Generated " + best.size() + " tests with total length "
		        + best.length());
		
		System.out.println("* Resulting TestSuite's coverage: "+best.getCoverage());
		
		if(Properties.CRITERION.equals("defuse")) {
			// TODO this is horribly inefficient! 
			// compute all results once and then ask each goal individually
			// ... and put all that in TestSuiteFitnessFuncion
			List<TestFitnessFunction> singleGoals = getFitnessFactory().getCoverageGoals();
			int covered = 0;
			for(TestFitnessFunction singleGoal : singleGoals) {
				if(singleGoal.isCovered(best.getTests()))
					covered++;
			}
			System.out.println("* Covered "+covered+"/"+singleGoals.size()+" goals");
			ga.printBudget();;
		}
		
		return best.getTests();
	}

	private TestSuiteFitnessFunction getFitnessFunction() {
		if (Properties.CRITERION.equalsIgnoreCase("mutation")) {
			System.out.println("* Test criterion: Mutation testing");
			return new MutationSuiteFitness();
		} else if (Properties.CRITERION.equalsIgnoreCase("lcsaj")) {
			System.out.println("* Test criterion: LCSAJ");
			return new LCSAJCoverageSuiteFitness();
		} else if (Properties.CRITERION.equalsIgnoreCase("defuse")) {
			System.out.println("* Test criterion: All DU Pairs");
			return new DefUseCoverageSuiteFitness();
		} else {
			System.out.println("* Test criterion: Branch coverage");
			return new BranchCoverageSuiteFitness();
		}
	}

	private TestFitnessFactory getFitnessFactory() {
		if (Properties.CRITERION.equalsIgnoreCase("mutation")) {
			System.out.println("* Test criterion: Mutation testing");
			return new MutationGoalFactory();
		} else if (Properties.CRITERION.equalsIgnoreCase("lcsaj")) {
			System.out.println("* Test criterion: LCSAJ");
			return new LCSAJCoverageFactory();
		} else if (Properties.CRITERION.equalsIgnoreCase("defuse")) {
			System.out.println("* Test criterion: All DU Pairs");
			return new DefUseCoverageFactory();
		} else {
			System.out.println("* Test criterion: Branch coverage");
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

		System.out.println("* Bootstrapping initial random test suite");

		TestSuiteChromosomeFactory factory = new TestSuiteChromosomeFactory();
		int random_tests = Properties.getPropertyOrDefault("random_tests", 100);
		if(Properties.CRITERION.equals("defuse")) {
			System.out.println("* Tuned down random bootstraping for DefUseCoverage-Criterion");
			factory.setNumberOfTests(random_tests/10);
		} else
			factory.setNumberOfTests(random_tests);
		TestSuiteChromosome chromosome = (TestSuiteChromosome) factory.getChromosome();
		TestSuiteMinimizer minimizer = new TestSuiteMinimizer(goals);
		minimizer.minimize(chromosome);
		System.out.println("* Initial test suite contains " + chromosome.size()
		        + " tests");

		return chromosome;
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
		System.out.println("* Setting up search algorithm for individual test generation");
		ExecutionTrace.enableTraceCalls();
		GeneticAlgorithm ga = setup();
		long start_time = System.currentTimeMillis() / 1000;
		boolean skip_covered = Properties.getPropertyOrDefault("skip_covered", true);
		boolean reuse_budget = Properties.getPropertyOrDefault("reuse_budget", true);
		boolean log_goals = Properties.getPropertyOrDefault("log_goals", false);
		FitnessLogger fitness_logger = new FitnessLogger();
		if (log_goals) {
			ga.addListener(fitness_logger);
		}

		// Get list of goals
		TestFitnessFactory goal_factory = getFitnessFactory();
		List<TestFitnessFunction> goals = goal_factory.getCoverageGoals();
		// Need to shuffle goals because the order may make a difference
		Randomness.getInstance().shuffle(goals);
		if(Properties.getPropertyOrDefault("preorder_goals_by_difficulty", true))
			orderGoalsByDifficulty(goals);
		System.out.println("* Total number of test goals: " + goals.size());

		// Bootstrap with random testing to cover easy goals
		FitnessFunction suite_fitness = getFitnessFunction();
		statistics.searchStarted(suite_fitness);

		TestSuiteChromosome suite = bootstrapRandomSuite(suite_fitness, goal_factory);
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
		if (covered_goals > 0) {
			System.out.println("* Random bootstrapping covered " + covered_goals
			        + " test goals");
		}

		GlobalTimeStoppingCondition global_time = new GlobalTimeStoppingCondition();
		int total_goals = goals.size();
		int current_budget = 0;

		int total_budget = Properties.GENERATIONS;

		while (current_budget < total_budget && covered_goals < total_goals
		        && !global_time.isFinished()) {
			int budget = (total_budget - current_budget) / (total_goals - covered_goals);
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

				if(Properties.getPropertyOrDefault("print_current_goals", false))
					System.out.println("* Searching for goal " + num + ": "
					        + fitness_function.toString());
				logger.info("Goal " + num + "/" + (total_goals - covered_goals) + ": "
				        + fitness_function);

				if (skip_covered && fitness_function.isCovered(suite.getTests())) {
					logger.info("Skipping goal because it is already covered");
					covered.add(num);
					covered_goals++;
					num++;
					continue;
				}

				if (global_time.isFinished()) {
					logger.info("Skipping goal because time is up");
					continue;
				}

				// FitnessFunction fitness_function = new
				// de.unisb.cs.st.evosuite.coverage.BranchCoverageTestFitness(goal);
				ga.setFitnessFunction(fitness_function);

				// Perform search
				logger.info("Starting evolution for goal " + fitness_function);
				ga.generateSolution();

				if (ga.getBestIndividual().getFitness() == 0.0) {
					if(Properties.getPropertyOrDefault("print_covered_goals", false))
						System.out.println("* Covered: "+fitness_function.toString());
					logger.info("Found solution, adding to test suite");
					TestChromosome best = (TestChromosome) ga.getBestIndividual();
					if (Properties.MINIMIZE) {
						TestCaseMinimizer minimizer = new TestCaseMinimizer(
						        fitness_function);
						minimizer.minimize(best);
					}
					best.test.addCoveredGoal(fitness_function);
					suite.addTest(best);
					
					// suite.addTest((TestChromosome)ga.getBestIndividual());
					covered_goals++;
					covered.add(num);
					
					// experiment:
					Set<Integer> additional_covered_nums = getAdditionallyCoveredGoals(goals,covered,best);
					for(Integer covered_num : additional_covered_nums) {
						covered_goals++;
						covered.add(covered_num);
					}
						
					
				} else {
					logger.info("Found no solution");
				}

				suite_fitness.getFitness(suite); // ???
				List<Chromosome> population = new ArrayList<Chromosome>();
				population.add(suite);
				statistics.iteration(population);
				if (reuse_budget)
					current_budget += stopping_condition.getCurrentValue();
				else
					current_budget += budget + 1;
				if (current_budget > total_budget)
					break;
				num++;

				// break;
			}
		}

		// for testing purposes
		ga.printBudget();
		if (global_time.isFinished())
			System.out.println("! Timeout reached");
		if (current_budget >= total_budget)
			System.out.println("! Budget exceeded");
		else
			System.out.println("* Remaining budget: "
					+ (total_budget - current_budget));
		int c = 0; 
		int uncovered_goals = total_goals-covered_goals;
		if(uncovered_goals<10)
			for (TestFitnessFunction goal : goals) {
				if (!covered.contains(c))
					System.out.println("! Unable to cover goal " + c + " " + goal.toString());
				c++;
			}
		else
			System.out.println("! #Goals that were not covered: "+uncovered_goals);

		List<Chromosome> population = new ArrayList<Chromosome>();
		population.add(suite);

		statistics.searchFinished(population);
		long end_time = System.currentTimeMillis() / 1000;
		System.out.println("* Search finished after " + (end_time - start_time)
		        + "s, best individual has fitness " + suite.getFitness());
		System.out.println("* Covered " + covered_goals + "/" + goals.size() + " goals");
		logger.info("Resulting test suite: " + suite.size() + " tests, length "
		        + suite.length());
		// Generate a test suite chromosome once all test cases are done?
		/*
		 * if(Properties.MINIMIZE) { System.out.println("* Minimizing result");
		 * TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
		 * minimizer.minimize(suite, suite_fitness); }
		 */
		// System.out.println("Resulting test suite has fitness "+suite.getFitness());
		System.out.println("* Resulting test suite: " + suite.size() + " tests, length "
		        + suite.length());

		// Log some stats

		statistics.iteration(population);
		statistics.minimized(suite);

		return suite.getTests();
	}

	private void orderGoalsByDifficulty(List<TestFitnessFunction> goals) {
		
		Collections.sort(goals);
//		for(TestFitnessFunction goal : goals) {
//			System.out.println(goal.toString());
//			System.out.println("dificulty: "+goal.getDifficulty());
//		}
	}

	/**
	 * Returns a list containing all positions of goals in the given goalList
	 * that are covered by the given test but not already in the given coveredSet
	 * 
	 * Used to avoid unnecessary solutionGenerations in generateIndividualTests() 
	 */
	private Set<Integer> getAdditionallyCoveredGoals(
			List<TestFitnessFunction> goals, Set<Integer> covered,
			TestChromosome best) {

		Set<Integer> r = new HashSet<Integer>();
		ExecutionResult result = TestCaseExecutor.getInstance().execute(best.test);
		int num = -1;
		for(TestFitnessFunction goal : goals) {
			num++;
			if(covered.contains(num))
				continue;
			if(goal.isCovered(best,result)) {
				r.add(num);
				if(Properties.getPropertyOrDefault("print_covered_goals", false))
					System.out.println("* Additionally covered: "+goal.toString());
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

	private StoppingCondition getStoppingCondition() {
		String stopping_condition = Properties.getPropertyOrDefault("stopping_condition",
		                                                            "MaxGenerations");
		logger.info("Setting stopping condition: " + stopping_condition);
		if (stopping_condition.equals("MaxGenerations")) {
			return new MaxGenerationStoppingCondition();
		} else if (stopping_condition.equals("MaxEvaluations")) {
			return new MaxFitnessEvaluationsStoppingCondition();
		} else if (stopping_condition.equals("MaxTime")) {
			return new MaxTimeStoppingCondition();
		} else if (stopping_condition.equals("MaxTests")) {
			return new MaxTestsStoppingCondition();
		} else if (stopping_condition.equals("MaxStatements")) {
			return new MaxStatementsStoppingCondition();
		} else {
			logger.warn("Unknown stopping condition: " + stopping_condition);
			return new MaxGenerationStoppingCondition();
		}
	}

	private CrossOverFunction getCrossoverFunction() {
		String crossover_function = Properties.getPropertyOrDefault("crossover_function",
		                                                            "SinglePoint");
		if (crossover_function.equals("SinglePointFixed"))
			return new SinglePointFixedCrossOver();
		else if (crossover_function.equals("SinglePointRelative"))
			return new SinglePointRelativeCrossOver();
		else
			return new SinglePointCrossOver();
	}

	private SelectionFunction getSelectionFunction() {
		String selection_function = Properties.getPropertyOrDefault("selection_function",
		                                                            "Rank");
		if (selection_function.equals("Roulette"))
			return new FitnessProportionateSelection();
		else if (selection_function.equals("Tournament"))
			return new TournamentSelection();
		else
			return new RankSelection();
	}

	private ChromosomeFactory getChromosomeFactory() {
		if (Properties.STRATEGY.equals("EvoSuite"))
			return new TestSuiteChromosomeFactory();
		else
			return new RandomLengthTestFactory();
	}

	private SecondaryObjective getSecondaryObjective(String name) {
		if (name.equalsIgnoreCase("size"))
			return new MinimizeSizeSecondaryObjective();
		else if (name.equalsIgnoreCase("maxlength"))
			return new MinimizeMaxLengthSecondaryObjective();
		else if (name.equalsIgnoreCase("averagelength"))
			return new MinimizeMaxLengthSecondaryObjective();
		else
			// default: totallength
			return new MinimizeTotalLengthSecondaryObjective();
	}

	private void getSecondaryObjectives(GeneticAlgorithm algorithm) {
		if (Properties.STRATEGY.equals("OneBranch")) {
			SecondaryObjective objective = getSecondaryObjective("size");
			Chromosome.addSecondaryObjective(objective);
			algorithm.addSecondaryObjective(objective);
		} else {
			String objectives = Properties.getPropertyOrDefault("secondary_objectives",
			                                                    "totallength");
			for (String name : objectives.split(":")) {
				SecondaryObjective objective = getSecondaryObjective(name);
				Chromosome.addSecondaryObjective(objective);
				algorithm.addSecondaryObjective(objective);
			}
		}
	}

	private GeneticAlgorithm getGeneticAlgorithm(ChromosomeFactory factory) {
		String search_algorithm = Properties.getProperty("algorithm");
		if (search_algorithm.equals("(1+1)EA")) {
			logger.info("Chosen search algorithm: (1+1)EA");
			return new OnePlusOneEA(factory);

		} else if (search_algorithm.equals("SteadyStateGA")) {
			logger.info("Chosen search algorithm: SteadyStateGA");
			SteadyStateGA ga = new SteadyStateGA(factory);
			if (Properties.STRATEGY.equals("EvoSuite"))
				ga.setReplacementFunction(new TestSuiteReplacementFunction());
			else
				ga.setReplacementFunction(new TestCaseReplacementFunction());
			return ga;

		} else if (search_algorithm.equals("MuPlusLambdaGA")) {
			logger.info("Chosen search algorithm: MuPlusLambdaGA");
			MuPlusLambdaGA ga = new MuPlusLambdaGA(factory);
			if (Properties.STRATEGY.equals("EvoSuite"))
				ga.setReplacementFunction(new TestSuiteReplacementFunction());
			else
				ga.setReplacementFunction(new TestCaseReplacementFunction());
			return ga;

		} else {
			logger.info("Chosen search algorithm: StandardGA");
			return new StandardGA(factory);
		}

	}

	/**
	 * Factory method for search algorithm
	 * 
	 * @return
	 */
	private GeneticAlgorithm setup() {

		ChromosomeFactory factory = getChromosomeFactory();
		GeneticAlgorithm ga = getGeneticAlgorithm(factory);

		// How to select candidates for reproduction
		SelectionFunction selection_function = getSelectionFunction();
		selection_function.setMaximize(false);
		ga.setSelectionFunction(selection_function);

		// When to stop the search
		stopping_condition = getStoppingCondition();
		ga.setStoppingCondition(stopping_condition);
		// ga.addListener(stopping_condition);
		if (Properties.getPropertyOrDefault("stop_zero", true))
			ga.addStoppingCondition(zero_fitness);
		ga.addStoppingCondition(new GlobalTimeStoppingCondition());
		if (Properties.MUTATION)
			ga.addStoppingCondition(new MutationTimeoutStoppingCondition());

		// How to cross over
		CrossOverFunction crossover_function = getCrossoverFunction();
		ga.setCrossOverFunction(crossover_function);

		// What to do about bloat
		// MaxLengthBloatControl bloat_control = new MaxLengthBloatControl();
		// ga.setBloatControl(bloat_control);

		if (Properties.STRATEGY.equals("EvoSuite")) {
			RelativeLengthBloatControl bloat_control = new RelativeLengthBloatControl();
			ga.addBloatControl(bloat_control);
			ga.addListener(bloat_control);
		} else {
			de.unisb.cs.st.evosuite.testcase.RelativeLengthBloatControl bloat_control = new de.unisb.cs.st.evosuite.testcase.RelativeLengthBloatControl();
			ga.addBloatControl(bloat_control);
			ga.addListener(bloat_control);
		}
		// ga.addBloatControl(new MaxLengthBloatControl());

		getSecondaryObjectives(ga);

		// Some statistics
		if (Properties.STRATEGY.equals("EvoSuite"))
			ga.addListener(SearchStatistics.getInstance());
		//ga.addListener(MutationStatistics.getInstance());
		//ga.addListener(BestChromosomeTracker.getInstance());

		if (Properties.getPropertyOrDefault("dynamic_limit", false)) {
			//max_s = GAProperties.generations * getBranches().size();
			// TODO: might want to make this dependent on the selected coverage criterion
			// TODO also, question: is branchMap.size() really intended here? 
			// I think BranchPool.getBranchCount() was intended
			Properties.GENERATIONS = Properties.GENERATIONS
			        * (BranchPool.getBranchlessMethods().size() + BranchPool.branchMap.size() * 2);
			stopping_condition.setLimit(Properties.GENERATIONS);
			logger.info("Setting dynamic length limit to " + Properties.GENERATIONS);
		}

		return ga;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestSuiteGenerator generator = new TestSuiteGenerator();
		generator.generateTestSuite();
	}

}
