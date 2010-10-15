/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.assertion.AssertionGenerator;
import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.coverage.TestFitnessFactory;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.lcsaj.LCSAJCoverageSuiteFitness;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.ga.CrossOverFunction;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
import de.unisb.cs.st.evosuite.ga.FitnessProportionateSelection;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.ga.MaxFitnessEvaluationsStoppingCondition;
import de.unisb.cs.st.evosuite.ga.MaxGenerationStoppingCondition;
import de.unisb.cs.st.evosuite.ga.MaxTimeStoppingCondition;
import de.unisb.cs.st.evosuite.ga.MuPlusLambdaGA;
import de.unisb.cs.st.evosuite.ga.OnePlusOneEA;
import de.unisb.cs.st.evosuite.ga.Randomness;
import de.unisb.cs.st.evosuite.ga.RankSelection;
import de.unisb.cs.st.evosuite.ga.SelectionFunction;
import de.unisb.cs.st.evosuite.ga.SinglePointCrossOver;
import de.unisb.cs.st.evosuite.ga.SinglePointFixedCrossOver;
import de.unisb.cs.st.evosuite.ga.SinglePointRelativeCrossOver;
import de.unisb.cs.st.evosuite.ga.StandardGA;
import de.unisb.cs.st.evosuite.ga.SteadyStateGA;
import de.unisb.cs.st.evosuite.ga.StoppingCondition;
import de.unisb.cs.st.evosuite.ga.TournamentSelection;
import de.unisb.cs.st.evosuite.ga.ZeroFitnessStoppingCondition;
import de.unisb.cs.st.evosuite.junit.TestSuite;
import de.unisb.cs.st.evosuite.mutation.MutationGoalFactory;
import de.unisb.cs.st.evosuite.mutation.MutationSuiteFitness;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.MaxTestsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.RandomLengthTestFactory;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseMinimizer;
import de.unisb.cs.st.evosuite.testcase.TestCaseReplacementFunction;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
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
	
	private SearchStatistics statistics = SearchStatistics.getInstance();
	
	private ZeroFitnessStoppingCondition zero_fitness = new ZeroFitnessStoppingCondition();
	
	private StoppingCondition stopping_condition;
	
	/**
	 * Generate a test suite for the target class
	 */
	public void generateTestSuite() {
		List<TestCase> tests;

		System.out.println("* Generating tests for class "+Properties.TARGET_CLASS);

		if(Properties.STRATEGY.equals("EvoSuite"))
			tests = generateWholeSuite();
		else
			tests = generateIndividualTests();
		
		if(Properties.MUTATION) {
			Set<Long> killed = new HashSet<Long>();
			AssertionGenerator asserter = new AssertionGenerator();
			for(TestCase test : tests) {
				asserter.addAssertions(test, killed);
			}
		}
		
		TestSuite suite = new TestSuite(tests);
		String name = Properties.TARGET_CLASS.substring(Properties.TARGET_CLASS.lastIndexOf(".")+1);
		System.out.println("* Writing JUnit test cases to "+Properties.TEST_DIR);
		suite.writeTestSuite("Test"+name, Properties.TEST_DIR);
		
		statistics.writeReport();
		System.out.println("* Done!");
	}
	
	/**
	 * Use the EvoSuite approach (Whole test suite generation)
	 * @return
	 */
	public List<TestCase> generateWholeSuite() {
		// Set up search algorithm
		System.out.println("* Setting up search algorithm for whole suite generation");
		GeneticAlgorithm ga = setup();
		long start_time = System.currentTimeMillis() / 1000;
		
		// What's the search target
		// TODO: Support multiple coverage criteria
		FitnessFunction fitness_function = getFitnessFunction();
		ga.setFitnessFunction(fitness_function);

		// Perform search
		System.out.println("* Starting evolution");
		ga.generateSolution();

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		long end_time = System.currentTimeMillis() / 1000;
		System.out.println("* Search finished after "+(end_time-start_time)+"s, best individual has fitness "+best.getFitness());
		
		if(Properties.MINIMIZE) {
			System.out.println("* Minimizing result");
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
			minimizer.minimize((TestSuiteChromosome) ga.getBestIndividual(), ga.getFitnessFunction());
		}
		statistics.iteration(ga.getPopulation());
		statistics.minimized(ga.getBestIndividual());
		System.out.println("* Generated "+best.size()+" tests with total length "+best.length());

		return best.getTests();
	}
	
	private TestSuiteFitnessFunction getFitnessFunction() {
		if(Properties.CRITERION.equalsIgnoreCase("mutation")) {
			System.out.println("* Test criterion: Mutation testing");
			return new MutationSuiteFitness();
		}
		else if(Properties.CRITERION.equalsIgnoreCase("lcsaj")) {
			System.out.println("* Test criterion: LCSAJ");
			return new LCSAJCoverageSuiteFitness();
		}
		else if(Properties.CRITERION.equalsIgnoreCase("defuse")) {
			System.out.println("* Test criterion: All DU Pairs");
			return new DefUseCoverageSuiteFitness();
		}
		else {
			System.out.println("* Test criterion: Branch coverage");
			return new BranchCoverageSuiteFitness();
		}	
	}
	
	private TestFitnessFactory getFitnessFactory() {
		if(Properties.CRITERION.equalsIgnoreCase("mutation")) {
			System.out.println("* Test criterion: Mutation testing");
			return new MutationGoalFactory();
		}
		else if(Properties.CRITERION.equalsIgnoreCase("lcsaj")) {
			System.out.println("* Test criterion: LCSAJ");
			return new LCSAJCoverageFactory();
		}
		else if(Properties.CRITERION.equalsIgnoreCase("defuse")) {
			System.out.println("* Test criterion: All DU Pairs");
			return new DefUseCoverageFactory();
		}
		else {
			System.out.println("* Test criterion: Branch coverage");
			return new BranchCoverageFactory();
		}
	}
	
	/**
	 * Use the OneBranch approach (One branch at a time)
	 * @return
	 */
	public List<TestCase> generateIndividualTests() {
		// Set up search algorithm
		System.out.println("* Setting up search algorithm for individual test generation");
		ExecutionTrace.enableTraceCalls();
		GeneticAlgorithm ga = setup();
		long start_time = System.currentTimeMillis() / 1000;
		

		// Each generated test case is put into a test suite
		TestSuiteChromosome suite = new TestSuiteChromosome();
		// TODO: Minimize individual tests instead?
		FitnessFunction suite_fitness = new de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageSuiteFitness();
		
		// Get list of goals
		// TODO: This needs to be replacable by other coverage criteria
		TestFitnessFactory goal_factory = getFitnessFactory();
		List<TestFitnessFunction> goals = goal_factory.getCoverageGoals(); 
		System.out.println("* Total number of test goals: "+goals.size());

		Randomness.getInstance().shuffle(goals);
		int total_goals = goals.size(); 
		int covered_goals = 0;
		int current_budget = 0;

		int total_budget = Properties.GENERATIONS;
		
		if(Properties.getProperty("dynamic_limit") != null) {
			//max_s = GAProperties.generations * getBranches().size();
			total_budget = Properties.GENERATIONS * (CFGMethodAdapter.branchless_methods.size() + CFGMethodAdapter.branch_map.size() * 2);
			logger.info("Setting dynamic length limit to "+total_budget);
		}

		statistics.searchStarted(suite_fitness);

		Set<Integer> covered = new HashSet<Integer>();
		
		while(current_budget < total_budget&& covered_goals < total_goals) {
			int budget = (total_budget - current_budget) / (total_goals - covered_goals);
			logger.info("Budget: "+budget+"/"+(total_budget - current_budget));
			logger.info("Statements: "+current_budget+"/"+total_budget);
			logger.info("Goals covered: "+covered_goals+"/"+total_goals);
			stopping_condition.setLimit(budget);

			int num = 0;
			//int num_statements = 0; //MaxStatementsStoppingCondition.getNumExecutedStatements();
			for(TestFitnessFunction fitness_function : goals) {
				
				if(covered.contains(num)){
					num++;
					continue;
				}
				
				ga.resetStoppingConditions();
				ga.clearPopulation();
				
				System.out.println("* Searching for goal "+num+": "+fitness_function.toString());
				logger.info("Goal "+num+"/"+(total_goals - covered_goals)+": "+fitness_function);

				if(fitness_function.isCovered(suite.getTests())) {
					logger.info("Skipping goal because it is already covered");
					covered.add(num);
					covered_goals++;
					num++;
					continue;
				}

				//FitnessFunction fitness_function = new de.unisb.cs.st.evosuite.coverage.BranchCoverageTestFitness(goal);
				ga.setFitnessFunction(fitness_function);

				// Perform search
				logger.info("Starting evolution for goal "+fitness_function);
				ga.generateSolution();

				if(ga.getBestIndividual().getFitness() == 0.0) {
					logger.info("Found solution, adding to test suite");
					TestChromosome best = (TestChromosome)ga.getBestIndividual();
					if(Properties.MINIMIZE) {
						TestCaseMinimizer minimizer = new TestCaseMinimizer((TestFitnessFunction) fitness_function);
						minimizer.minimize(best);
					}
					suite.addTest(best);
					
					//suite.addTest((TestChromosome)ga.getBestIndividual());
					covered_goals++;
					covered.add(num);
				} else {
					logger.info("Found no solution");				
				}

				suite_fitness.getFitness(suite);
				List<Chromosome> population = new ArrayList<Chromosome>();
				population.add(suite);
				statistics.iteration(population);
				current_budget += stopping_condition.getCurrentValue();
				if(current_budget > total_budget)
					break;
				num++;

				//break;
			}
		}
		List<Chromosome> population = new ArrayList<Chromosome>();
		population.add(suite);

		statistics.searchFinished(population);
		long end_time = System.currentTimeMillis() / 1000;
		System.out.println("* Search finished after "+(end_time-start_time)+"s, best individual has fitness "+suite.getFitness());
		System.out.println("* Covered "+covered_goals+"/"+goals.size()+" goals");
		logger.info("Resulting test suite: "+suite.size()+" tests, length "+suite.length());
		// Generate a test suite chromosome once all test cases are done?
		/*
		if(Properties.MINIMIZE) {
			System.out.println("* Minimizing result");
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
			minimizer.minimize(suite, suite_fitness);
		}
		*/
		//System.out.println("Resulting test suite has fitness "+suite.getFitness());
		System.out.println("* Resulting test suite: "+suite.size()+" tests, length "+suite.length());
		
		// Log some stats
		
		statistics.iteration(population);
		statistics.minimized(suite);
		
		return suite.getTests();
	}
	
	/*
	protected List<BranchCoverageGoal> getBranches() {
		List<BranchCoverageGoal> goals = new ArrayList<BranchCoverageGoal>();

		// Branchless methods
		String class_name = Properties.TARGET_CLASS;
		logger.info("Getting branches for "+class_name);
		for(String method : CFGMethodAdapter.branchless_methods) {
			goals.add(new BranchCoverageGoal(class_name, method));
			logger.info("Adding new method goal for method "+method);
		}
		
		// Branches
		for(String className : CFGMethodAdapter.branch_map.keySet()) {
			for(String methodName : CFGMethodAdapter.branch_map.get(className).keySet()) {
				// Get CFG of method
				ControlFlowGraph cfg = ExecutionTracer.getExecutionTracer().getCFG(className, methodName);
				
				for(Entry<Integer,Integer> entry : CFGMethodAdapter.branch_map.get(className).get(methodName).entrySet()) {
					// Identify vertex in CFG
					goals.add(new BranchCoverageGoal(entry.getValue(), entry.getKey(), true, cfg, className, methodName));
					goals.add(new BranchCoverageGoal(entry.getValue(), entry.getKey(), false, cfg, className, methodName));
					logger.info("Adding new branch goals for method "+methodName);
				}
						
			// Approach level is measured in terms of line coverage? Or possible in terms of branches...
			}
		}
		
		return goals;
	}
	*/
	
	private StoppingCondition getStoppingCondition() {
		String stopping_condition = Properties.getPropertyOrDefault("stopping_condition", "MaxGenerations");
		logger.info("Setting stopping condition: "+stopping_condition);
		if(stopping_condition.equals("MaxGenerations")) {
			return new MaxGenerationStoppingCondition();
		}
		else if(stopping_condition.equals("MaxEvaluations")) {
			return new MaxFitnessEvaluationsStoppingCondition();
		}
		else if(stopping_condition.equals("MaxTime")) {
			return new MaxTimeStoppingCondition();
		}
		else if(stopping_condition.equals("MaxTests")) {
			return new MaxTestsStoppingCondition();
		}
		else if(stopping_condition.equals("MaxStatements")) {
			return new MaxStatementsStoppingCondition();
		}
		else {
			logger.warn("Unknown stopping condition: "+stopping_condition);
			return new MaxGenerationStoppingCondition();
		}
	}
	
	private CrossOverFunction getCrossoverFunction() {
		String crossover_function = Properties.getPropertyOrDefault("crossover_function", "SinglePoint");
		if(crossover_function.equals("SinglePointFixed"))
			return new SinglePointFixedCrossOver();
		else if(crossover_function.equals("SinglePointRelative"))
			return new SinglePointRelativeCrossOver();
		else 
			return new SinglePointCrossOver();
	}
	
	private SelectionFunction getSelectionFunction() {
		String selection_function = Properties.getPropertyOrDefault("selection_function", "Rank");
		if(selection_function.equals("Roulette"))
			return new FitnessProportionateSelection();
		else if(selection_function.equals("Tournament"))
			return new TournamentSelection();
		else
			return new RankSelection();
	}
	
	private ChromosomeFactory getChromosomeFactory() {
		if(Properties.STRATEGY.equals("EvoSuite"))
			return new TestSuiteChromosomeFactory();
		else
			return new RandomLengthTestFactory();
	}
	
	private GeneticAlgorithm getGeneticAlgorithm(ChromosomeFactory factory) {
		String search_algorithm = Properties.getProperty("algorithm");
		if(search_algorithm.equals("(1+1)EA")) {
			logger.info("Chosen search algorithm: (1+1)EA");
			return new OnePlusOneEA(factory);

		} else if(search_algorithm.equals("SteadyStateGA")) {
			logger.info("Chosen search algorithm: SteadyStateGA");
			SteadyStateGA ga = new SteadyStateGA(factory);
			if(Properties.STRATEGY.equals("EvoSuite"))
				ga.setReplacementFunction(new TestSuiteReplacementFunction());
			else
				ga.setReplacementFunction(new TestCaseReplacementFunction());
			return ga;

		} else if(search_algorithm.equals("MuPlusLambdaGA")) {
			logger.info("Chosen search algorithm: MuPlusLambdaGA");
			MuPlusLambdaGA ga = new MuPlusLambdaGA(factory);
			if(Properties.STRATEGY.equals("EvoSuite"))
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
		ga.addListener(stopping_condition);		
		ga.addStoppingCondition(zero_fitness);

		// How to cross over
		CrossOverFunction crossover_function = getCrossoverFunction();
		ga.setCrossOverFunction(crossover_function);

		// What to do about bloat
		//MaxLengthBloatControl bloat_control = new MaxLengthBloatControl();
		//ga.setBloatControl(bloat_control);

		if(Properties.STRATEGY.equals("EvoSuite")) {		
			RelativeLengthBloatControl bloat_control = new RelativeLengthBloatControl();
			ga.addBloatControl(bloat_control);
			ga.addListener(bloat_control);
		} else {
			de.unisb.cs.st.evosuite.testcase.RelativeLengthBloatControl bloat_control = new de.unisb.cs.st.evosuite.testcase.RelativeLengthBloatControl();
			ga.addBloatControl(bloat_control);
			ga.addListener(bloat_control);
		}
		//ga.addBloatControl(new MaxLengthBloatControl());
	
		// Some statistics
		if(Properties.STRATEGY.equals("EvoSuite"))
			ga.addListener(SearchStatistics.getInstance());
		//ga.addListener(MutationStatistics.getInstance());
		//ga.addListener(BestChromosomeTracker.getInstance());
		
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
