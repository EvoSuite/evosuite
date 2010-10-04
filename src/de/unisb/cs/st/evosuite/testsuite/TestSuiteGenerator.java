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

package de.unisb.cs.st.evosuite.testsuite;

import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.ga.CrossOverFunction;
import de.unisb.cs.st.evosuite.ga.FitnessFunction;
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
import de.unisb.cs.st.evosuite.ga.SinglePointRelativeCrossOver;
import de.unisb.cs.st.evosuite.ga.StandardGA;
import de.unisb.cs.st.evosuite.ga.SteadyStateGA;
import de.unisb.cs.st.evosuite.ga.StoppingCondition;
import de.unisb.cs.st.evosuite.ga.ZeroFitnessStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.MaxTestsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.TestCase;

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
	private StoppingCondition max_statements = new MaxStatementsStoppingCondition();
	private StoppingCondition zero_fitness = new ZeroFitnessStoppingCondition();
	private Randomness randomness = Randomness.getInstance();
	
	private void resetStoppingConditions() {
		max_gen.reset();
		max_fitness.reset();
		max_time.reset();
		max_tests.reset();
		max_statements.reset();
	}
	
	public void generateTestSuite() {
		
		// Set up search algorithm
		System.out.println("Setting up search algorithm");
		GeneticAlgorithm ga = getGeneticAlgorithm();
		
		// Perform search
		System.out.println("Starting evolution");
		ga.generateSolution();

		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("Search finished, best individual has fitness "+best.getFitness());
		
		if(minimize) {
			//System.out.println("Starting minimization");
			TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
			minimizer.minimize((TestSuiteChromosome) ga.getBestIndividual(), ga.getFitnessFunction());
		}
		
		SearchStatistics statistics = SearchStatistics.getInstance();
		statistics.iteration(ga.getPopulation());
		statistics.minimized(ga.getBestIndividual());
		statistics.writeReport();
		resetStoppingConditions();
		System.out.println("Generated "+best.size()+" tests with total length "+best.length());
		System.out.println("Done.");
		//printMethodTimes();
/*
		System.out.println();
		System.out.println("Bloat rejections: "+SteadyStateGA.rejected_bloat);
		System.out.println("Fitness rejections: "+SteadyStateGA.rejected_fitness);
		System.out.println("Fitness acceptions: "+SteadyStateGA.accepted_fitness);
		*/
		
		/*
		MutationForRun mm = MutationForRun.getFromDefaultLocation();
		Collection<String> classesToMutate = mm.getClassNames();
		try {
			Class<?> target = Class.forName(classesToMutate.iterator().next());
			for(TestCase t : ((TestSuiteChromosome) ga.getBestIndividual()).getTests()) {
				ParametrizedTestCase p = new ParametrizedTestCase(t, target);
			}
		} catch (ClassNotFoundException e) {
		}
		*/
		
		
	}
	
	/**
	 * Experiment 1: 
	 * Generate test suite x times
	 */
	public void experiment1() {
		// Set up search algorithm
		logger.info("Setting up search algorithm for experiment 1");
		SearchStatistics statistics = SearchStatistics.getInstance();

		int num_experiments = Properties.getPropertyOrDefault("num_experiments", 10);
		for(int current_experiment = 0; current_experiment < num_experiments; current_experiment++) {
			// Reset everything
			randomness.setSeed(randomness.getSeed() + current_experiment);
			GeneticAlgorithm ga = getGeneticAlgorithm();

			// Generate test suite
			logger.info("Starting evolution #"+current_experiment+"/"+num_experiments);
			ga.generateSolution();
			
			TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

			
			logger.info("Best individual has fitness: "+best.getFitness());
			if(minimize) {
				logger.info("Starting minimization ("+best.size()+"/"+best.length()+") "+best.getFitness());
				TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
				minimizer.minimize((TestSuiteChromosome) ga.getBestIndividual(), ga.getFitnessFunction());
				logger.info("Finished minimization ("+best.size()+"/"+best.length()+")");
			}

			statistics.iteration(ga.getPopulation());
			statistics.minimized(ga.getBestIndividual());

			resetStoppingConditions();
		}
		statistics.writeReport();
		
	}
	
	/**
	 * Experiment 2: 
	 * Vary the test suite length
	 */
	public void experiment2() {
		// Set up search algorithm
		logger.info("Setting up search algorithm for experiment 2");
		
		int num_experiments = Properties.getPropertyOrDefault("num_experiments", 10);
		int num_steps       = Properties.getPropertyOrDefault("num_steps", 10);
		int max_length      = Properties.CHROMOSOME_LENGTH;
		SearchStatistics statistics = SearchStatistics.getInstance();

		for(int current_step = 1; current_step <= num_steps; current_step++) {
			int current_length = current_step * max_length / num_steps;

			Properties.CHROMOSOME_LENGTH = current_length;
			logger.info("Setting length to "+current_length);

			for(int current_experiment = 0; current_experiment < num_experiments; current_experiment++) {
				// Reset everything
				GeneticAlgorithm ga = getGeneticAlgorithm();
				randomness.setSeed(randomness.getSeed() + current_experiment);

				// Generate test suite
				logger.info("Starting evolution #"+current_experiment+"/"+num_experiments+" at length "+current_length);
				ga.generateSolution();

				TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

				if(minimize) {
					logger.info("Starting minimization ("+best.size()+"/"+best.length()+") "+best.getFitness());
					TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
					minimizer.minimize((TestSuiteChromosome) ga.getBestIndividual(), ga.getFitnessFunction());
					logger.info("Finished minimization ("+best.size()+"/"+best.length()+")");
				}

				statistics.iteration(ga.getPopulation());
				statistics.minimized(ga.getBestIndividual());
				resetStoppingConditions();
			}
		}
		statistics.writeReport();
		
	}
	
	/**
	 * Experiment 3:
	 * Vary the population size
	 */
	public void experiment3() {
		// Set up search algorithm
		logger.info("Setting up search algorithm for experiment 3");
		SearchStatistics statistics = SearchStatistics.getInstance();
	
		int num_experiments = Properties.getPropertyOrDefault("num_experiments", 10);
		int num_steps       = Properties.getPropertyOrDefault("num_steps", 10);
		int max_population  = Properties.POPULATION_SIZE;

		for(int current_step = 1; current_step <= num_steps; current_step++) {
			int current_pop = current_step * max_population / num_steps;
			
			logger.info("Current size: "+current_pop);
			Properties.POPULATION_SIZE = current_pop;

			for(int current_experiment = 0; current_experiment < num_experiments; current_experiment++) {
		

				GeneticAlgorithm ga = getGeneticAlgorithm();
				randomness.setSeed(randomness.getSeed() + current_experiment);

				// Generate test suite
				logger.info("Starting evolution #"+current_experiment+"/"+num_experiments);


				ga.generateSolution();

				TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();

				if(minimize) {
					logger.info("Starting minimization ("+best.size()+"/"+best.length()+") "+best.getFitness());
					TestSuiteMinimizer minimizer = new TestSuiteMinimizer();
					minimizer.minimize((TestSuiteChromosome) ga.getBestIndividual(), ga.getFitnessFunction());
					logger.info("Finished minimization ("+best.size()+"/"+best.length()+")");
				}
				statistics.iteration(ga.getPopulation());
				statistics.minimized(ga.getBestIndividual());
				resetStoppingConditions();
			}
		}
		statistics.writeReport();
		
	}
	
	public List<TestCase> getTests() {
		return null;
	}

	public List<TestCase> getFailedTests() {
		return null;
	}

	/**
	 * Factory method for search algorithm
	 * @return
	 */
	protected GeneticAlgorithm getGeneticAlgorithm() {

		GeneticAlgorithm ga = null;
		ChromosomeFactory factory = new TestSuiteChromosomeFactory();

		SelectionFunction selection_function = new RankSelection();
		selection_function.setMaximize(false);

		String search_algorithm = Properties.getProperty("algorithm");
		if(search_algorithm.equals("(1+1)EA")) {
			logger.info("Chosen search algorithm: (1+1)EA");
			ga = new OnePlusOneEA(factory);

		} else if(search_algorithm.equals("SteadyStateGA")) {
			logger.info("Chosen search algorithm: SteadyStateGA");
			ga = new SteadyStateGA(factory);
			((SteadyStateGA)ga).setReplacementFunction(new TestSuiteReplacementFunction(selection_function));

		} else if(search_algorithm.equals("MuPlusLambdaGA")) {
			logger.info("Chosen search algorithm: MuPlusLambdaGA");
			ga = new MuPlusLambdaGA(factory);
			((MuPlusLambdaGA)ga).setReplacementFunction(new TestSuiteReplacementFunction(selection_function));
			
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
//		CrossOverFunction crossover_function = new SinglePointRelativeCrossOver();
		CrossOverFunction crossover_function = new SinglePointCrossOver();
		ga.setCrossOverFunction(crossover_function);

		FitnessFunction fitness_function = new BranchCoverageFitnessFunction();
		ga.setFitnessFunction(fitness_function);

		//MaxLengthBloatControl bloat_control = new MaxLengthBloatControl();
		//ga.setBloatControl(bloat_control);

		RelativeLengthBloatControl bloat_control = new RelativeLengthBloatControl();
		ga.addBloatControl(bloat_control);
		ga.addListener(bloat_control);
		//ga.addBloatControl(new MaxLengthBloatControl());
	
				
		ga.addListener(SearchStatistics.getInstance());
		//ga.addListener(MutationStatistics.getInstance());
		ga.addListener(BestChromosomeTracker.getInstance());
		
		// Possibly change stopping condition
		//ga.addStoppingCondition(MaxStatementsStoppingCondition.getInstance());
		ga.addListener(new MaxStatementsStoppingCondition());
		ga.addListener(new MaxTestsStoppingCondition());
		ga.addListener(new MaxFitnessEvaluationsStoppingCondition());
		return ga;
	}
	
	/**
	 * Entry point - generate task files
	 * 
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
				generator.experiment1();
				break;
			case 2:
				generator.experiment2();
				break;
			case 3:
				generator.experiment3();
				break;
			default:
				generator.generateTestSuite();
					
			}
		} else {
			generator.generateTestSuite();
		}
	}
}
