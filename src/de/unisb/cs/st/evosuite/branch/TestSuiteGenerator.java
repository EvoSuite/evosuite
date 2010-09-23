/**
 * 
 */
package de.unisb.cs.st.evosuite.branch;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.mutation.MutationStatistics;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;
import de.unisb.cs.st.evosuite.testcase.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.MaxTestsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.RandomLengthTestFactory;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testsuite.SearchStatistics;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteMinimizer;
import de.unisb.cs.st.ga.Chromosome;
import de.unisb.cs.st.ga.ChromosomeFactory;
import de.unisb.cs.st.ga.FitnessFunction;
import de.unisb.cs.st.ga.GAProperties;
import de.unisb.cs.st.ga.GeneticAlgorithm;
import de.unisb.cs.st.ga.MaxFitnessEvaluationsStoppingCondition;
import de.unisb.cs.st.ga.MaxGenerationStoppingCondition;
import de.unisb.cs.st.ga.MaxTimeStoppingCondition;
import de.unisb.cs.st.ga.MuPlusLambdaGA;
import de.unisb.cs.st.ga.OnePlusOneEA;
import de.unisb.cs.st.ga.Randomness;
import de.unisb.cs.st.ga.RankSelection;
import de.unisb.cs.st.ga.SelectionFunction;
import de.unisb.cs.st.ga.SinglePointCrossOver;
import de.unisb.cs.st.ga.SinglePointFixedCrossOver;
import de.unisb.cs.st.ga.SinglePointRelativeCrossOver;
import de.unisb.cs.st.ga.StandardGA;
import de.unisb.cs.st.ga.SteadyStateGA;
import de.unisb.cs.st.ga.StoppingCondition;
import de.unisb.cs.st.ga.ZeroFitnessStoppingCondition;



/**
 * @author Gordon Fraser
 *
 */
public class TestSuiteGenerator {

	private static Logger logger = Logger.getLogger(TestSuiteGenerator.class);
	
	boolean minimize = Properties.getPropertyOrDefault("minimize", true);

	private StoppingCondition max_gen = new MaxGenerationStoppingCondition();
	private StoppingCondition max_fitness = new MaxFitnessEvaluationsStoppingCondition();
	private StoppingCondition max_time = new MaxTimeStoppingCondition();
	private StoppingCondition max_tests = new MaxTestsStoppingCondition();
	private MaxStatementsStoppingCondition max_statements = new MaxStatementsStoppingCondition();
	private StoppingCondition zero_fitness = new ZeroFitnessStoppingCondition();
	private int num_experiments = 0;

	private final static boolean BEST_LENGTH = GAProperties.getPropertyOrDefault("check_best_length", true);  

	/**
	 * Do the magic
	 */
	public void generateTestSuite() {
		num_experiments = 1;
		experiment1();
	}
	
	
	
	/**
	 * Experiment 1: 
	 * Generate test suite x times
	 */
	public void experiment1() {
		// Set up search algorithm
		System.out.println("Setting up genetic algorithm");
		SearchStatistics statistics = SearchStatistics.getInstance();
		ExecutionTrace.trace_calls = true;

		int max_s = GAProperties.generations;
		if(Properties.getProperty("dynamic_limit") != null) {
			logger.info("Setting dynamic length limit");
			max_s = GAProperties.generations * getBranches().size();
		}
		
		for(int current_experiment = 0; current_experiment < num_experiments; current_experiment++) {
			// Reset everything
			Randomness.getInstance().setSeed(Randomness.getInstance().getSeed() + current_experiment);
			
			GeneticAlgorithm ga = getGeneticAlgorithm();
			TestSuiteChromosome suite = new TestSuiteChromosome();
			FitnessFunction suite_fitness = new de.unisb.cs.st.evosuite.testsuite.BranchCoverageFitnessFunction();
			List<BranchCoverageGoal> goals = getBranches(); 
			Randomness.getInstance().shuffle(goals);
			int total_goals = goals.size(); 
			int covered_goals = 0;
			int current_statements = 0;
			
			logger.info("Experiment "+current_experiment+"/"+num_experiments);
			statistics.searchStarted(suite_fitness);

			Set<Integer> covered = new HashSet<Integer>();
			
			while(current_statements < max_s&& covered_goals < total_goals) {
				int budget = (max_s - current_statements) / (total_goals - covered_goals);
				logger.info("Budget: "+budget+"/"+(max_s - current_statements));
				logger.info("Statements: "+current_statements+"/"+max_s);
				logger.info("Goals covered: "+covered_goals+"/"+total_goals);
				max_statements.setMaxExecutedStatements(budget);
				int num = 0;
				//int num_statements = 0; //MaxStatementsStoppingCondition.getNumExecutedStatements();
				for(BranchCoverageGoal goal : goals) {
					
					if(covered.contains(num)){
						num++;
						continue;
					}
					
					ga.resetStoppingConditions();
					ga.clearPopulation();
					
					System.out.println("Searching for goal "+num);
					logger.info("Goal "+num+"/"+(total_goals - covered_goals)+": "+goal);
//					logger.info("Number of statements: "+max_statements.getNumExecutedStatements());
					//num_statements += MaxStatementsStoppingCondition.getNumExecutedStatements();

					zero_fitness.reset();
					//logger.info("Current number of statements executed: "+num_statements);
					if(goal.isCovered(suite.getTests())) {
						logger.info("Skipping goal because it is already covered");
						covered.add(num);
						covered_goals++;
						num++;
						continue;
					}
					/*
					if(num_statements >= budget) {
						logger.info("Stopping search as max number of statements has been reached");
						suite_fitness.getFitness(suite);
						statistics.iteration(suite);
						current_statements += MaxStatementsStoppingCondition.getNumExecutedStatements();
						max_statements.reset();
						break;
					}
					*/
					FitnessFunction fitness_function = new BranchCoverageFitnessFunction(goal);
					ga.setFitnessFunction(fitness_function);

					// Perform search
					logger.info("Starting evolution for goal "+goal);
					ga.generateSolution();

					if(ga.getBestIndividual().getFitness() == 0.0) {
						logger.info("Found solution, adding to test suite");
						suite.addTest((TestChromosome)ga.getBestIndividual());
						covered_goals++;
						covered.add(num);
					} else {
						logger.info("Found no solution");				
					}
					suite_fitness.getFitness(suite);
					List<Chromosome> population = new ArrayList<Chromosome>();
					population.add(suite);
					statistics.iteration(population);
					current_statements += max_statements.getNumExecutedStatements();
					if(current_statements > max_s)
						break;
					logger.info("Adding statements: "+max_statements.getNumExecutedStatements()+" -> "+current_statements+"/"+max_s);
					max_statements.reset();
					num++;

					//break;
				}
				max_statements.reset();

			}
			List<Chromosome> population = new ArrayList<Chromosome>();
			population.add(suite);

			statistics.searchFinished(population);
			logger.info("Resulting test suite: "+suite.size()+" tests, length "+suite.length());
			// Generate a test suite chromosome once all test cases are done?
			if(minimize) {
				logger.info("Starting minimization");
				TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
				minimizer.minimize(suite, suite_fitness);
				logger.info("Finished minimization");
			}
			System.out.println("Resulting test suite has fitness "+suite.getFitness());
			System.out.println("Resulting test suite: "+suite.size()+" tests, length "+suite.length());
			
			// Log some stats
			
			statistics.iteration(population);
			statistics.minimized(suite);			
			resetStoppingConditions();
		}
		statistics.writeReport();
		
	}
	
	/**
	 * Experiment 3: 
	 * Vary the population size
	 */
	public void experiment3() {
		// Set up search algorithm
		logger.info("Setting up search algorithm for experiment 1");
		SearchStatistics statistics = SearchStatistics.getInstance();
		ExecutionTrace.trace_calls = true;

		int max_s = GAProperties.getPropertyOrDefault("generations", 1000);
		int current_statements = 0;

		
		
		for(int current_experiment = 0; current_experiment < num_experiments; current_experiment++) {
			// Reset everything
			Randomness.getInstance().setSeed(Randomness.getInstance().getSeed() + current_experiment);
			
			GeneticAlgorithm ga = getGeneticAlgorithm();
			TestSuiteChromosome suite = new TestSuiteChromosome();
			FitnessFunction suite_fitness = new de.unisb.cs.st.evosuite.testsuite.BranchCoverageFitnessFunction();
			List<BranchCoverageGoal> goals = getBranches(); 
			Randomness.getInstance().shuffle(goals);
			int total_goals = goals.size(); 
			int covered_goals = 0;
			
			logger.info("Experiment "+current_experiment+"/"+num_experiments);
			statistics.searchStarted(suite_fitness);

			Set<Integer> covered = new HashSet<Integer>();
			
			while(current_statements < max_s&& covered_goals < total_goals) {
				int budget = (max_s - current_statements) / (total_goals - covered_goals);
				logger.info("Budget: "+budget+"/"+(max_s - current_statements));
				logger.info("Statements: "+current_statements+"/"+max_s);
				logger.info("Goals covered: "+covered_goals+"/"+total_goals);
				max_statements.setMaxExecutedStatements(budget);
				int num = 0;
				//int num_statements = 0; //MaxStatementsStoppingCondition.getNumExecutedStatements();
				for(BranchCoverageGoal goal : goals) {
					
					if(covered.contains(num)){
						num++;
						continue;
					}
					
					ga.resetStoppingConditions();
					ga.clearPopulation();
					
					logger.info("Goal "+num+"/"+goals.size()+": "+goal);
//					logger.info("Number of statements: "+max_statements.getNumExecutedStatements());
					//num_statements += MaxStatementsStoppingCondition.getNumExecutedStatements();

					zero_fitness.reset();
					//logger.info("Current number of statements executed: "+num_statements);
					if(goal.isCovered(suite.getTests())) {
						logger.info("Skipping goal because it is already covered");
						covered.add(num);
						covered_goals++;
						num++;
						continue;
					}
					/*
					if(num_statements >= budget) {
						logger.info("Stopping search as max number of statements has been reached");
						suite_fitness.getFitness(suite);
						statistics.iteration(suite);
						current_statements += MaxStatementsStoppingCondition.getNumExecutedStatements();
						max_statements.reset();
						break;
					}
					*/
					FitnessFunction fitness_function = new BranchCoverageFitnessFunction(goal);
					ga.setFitnessFunction(fitness_function);

					// Perform search
					logger.info("Starting evolution for goal "+goal);
					ga.generateSolution();

					if(ga.getBestIndividual().getFitness() == 0.0) {
						logger.info("Found solution, adding to test suite");
						suite.addTest((TestChromosome)ga.getBestIndividual());
						covered_goals++;
						covered.add(num);
					} else {
						logger.info("Found no solution");				
					}
					suite_fitness.getFitness(suite);
					List<Chromosome> population = new ArrayList<Chromosome>();
					population.add(suite);
					statistics.iteration(population);
					current_statements += max_statements.getNumExecutedStatements();
					logger.info("Adding statements: "+max_statements.getNumExecutedStatements());
					max_statements.reset();
					num++;

					//break;
				}
				max_statements.reset();

			}
			List<Chromosome> population = new ArrayList<Chromosome>();
			population.add(suite);

			statistics.searchFinished(population);
			logger.info("Resulting test suite: "+suite.size()+" tests, length "+suite.length());
			// Generate a test suite chromosome once all test cases are done?
			if(minimize) {
				logger.info("Starting minimization");
				TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
				minimizer.minimize(suite, suite_fitness);
				logger.info("Finished minimization");
			}
			logger.info("Minimized test suite: "+suite.size()+" tests, length "+suite.length());
			
			// Log some stats
			
			statistics.iteration(population);
			statistics.minimized(suite);			
			resetStoppingConditions();
		}
		statistics.writeReport();
		
	}
	
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
	
	/**
	 * Factory method for search algorithm
	 * @return
	 */
	protected GeneticAlgorithm getGeneticAlgorithm() {

		GeneticAlgorithm ga = null;
		ChromosomeFactory factory = new RandomLengthTestFactory();
//		ChromosomeFactory factory = new OUMTestChromosomeFactory();

		SelectionFunction selection_function = new RankSelection();
		selection_function.setMaximize(false);

		String search_algorithm = GAProperties.getProperty("algorithm");
		if(search_algorithm.equals("(1+1)EA")) {
			logger.info("Chosen search algorithm: (1+1)EA");
			ga = new OnePlusOneEA(factory);

		} else if(search_algorithm.equals("SteadyStateGA")) {
			logger.info("Chosen search algorithm: SteadyStateGA");
			ga = new SteadyStateGA(factory);
			((SteadyStateGA)ga).setReplacementFunction(new TestCaseReplacementFunction(selection_function));


		} else if(search_algorithm.equals("MuPlusLambdaGA")) {
			logger.info("Chosen search algorithm: MuPlusLambdaGA");
			ga = new MuPlusLambdaGA(factory);
			
		} else {
			logger.info("Chosen search algorithm: StandardGA");
			ga = new StandardGA(factory);
		}
		
		ga.setSelectionFunction(selection_function);
		
		String stopping_condition = Properties.getPropertyOrDefault("stopping_condition", "MaxGenerations");
		logger.info("Setting stopping condition: "+stopping_condition);
		if(stopping_condition.equals("MaxGenerations"))
			ga.setStoppingCondition(max_gen);
		else if(stopping_condition.equals("MaxEvaluations"))
			ga.setStoppingCondition(max_fitness);
		else if(stopping_condition.equals("MaxTime"))
			ga.setStoppingCondition(max_time);
		else if(stopping_condition.equals("MaxTests"))
			ga.setStoppingCondition(max_tests);
		else if(stopping_condition.equals("MaxStatements"))
			ga.setStoppingCondition(max_statements);
		else {
			logger.warn("Unknown stopping condition: "+stopping_condition);
		}
		ga.addStoppingCondition(zero_fitness);

		
		// Relative position crossover to avoid that size increases
		String crossover_function = Properties.getPropertyOrDefault("crossover_function", "SinglePointRelative"); 
		if(crossover_function.equals("SinglePointRelative")) {
			logger.info("Setting relative single point cross over");
			ga.setCrossOverFunction(new SinglePointRelativeCrossOver());
		}
		else if(crossover_function.equals("SinglePointFixed")) {
			logger.info("Setting fixed single point cross over");
			ga.setCrossOverFunction(new SinglePointFixedCrossOver());
		}
		else if(crossover_function.equals("SinglePoint")) {
			logger.info("Setting normal single point cross over");
			ga.setCrossOverFunction(new SinglePointCrossOver());
		}
		else {
			logger.info("Setting relative single point cross over");
			ga.setCrossOverFunction(new SinglePointRelativeCrossOver());
		}
		
		//MaxLengthBloatControl bloat_control = new MaxLengthBloatControl();
		//ga.setBloatControl(bloat_control);

		
		RelativeLengthBloatControl bloat_control = new RelativeLengthBloatControl();
		  

		if(BEST_LENGTH) {
			logger.debug("Using best bloat control");
			ga.addBloatControl(bloat_control);
			ga.addListener(bloat_control);
		} else {
			logger.debug("Not using best bloat control");
		}
		//ga.addBloatControl(new MaxLengthBloatControl());
	    
				
		//ga.addListener(SearchStatistics.getInstance());
		ga.addListener(MutationStatistics.getInstance());
		//ga.addListener(BestChromosomeTracker.getInstance());
		
		// Possibly change stopping condition
		//ga.addStoppingCondition(MaxStatementsStoppingCondition.getInstance());
		//ga.addListener(MaxStatementsStoppingCondition.getInstance());
		//ga.addListener(new MaxStatementsStoppingCondition());
		ga.addListener(new MaxTestsStoppingCondition());
		ga.addListener(new MaxFitnessEvaluationsStoppingCondition());
		return ga;
	}
	
	private void resetStoppingConditions() {
		max_gen.reset();
		max_fitness.reset();
		max_time.reset();
		max_tests.reset();
		max_statements.reset();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Creating coverage test suite.");
		TestSuiteGenerator generator = new TestSuiteGenerator();
		String experiment = System.getProperty("experiment");
		if(experiment != null && !experiment.equals("none") && !experiment.equals("")) {
			int num = Integer.parseInt(experiment);
			switch(num) {
			case 1:
				generator.num_experiments = Properties.getPropertyOrDefault("num_experiments", 10);
				generator.experiment1();
				break;
			default:
				generator.generateTestSuite();
					
			}
		} else {
			generator.generateTestSuite();
		}
	}

}
