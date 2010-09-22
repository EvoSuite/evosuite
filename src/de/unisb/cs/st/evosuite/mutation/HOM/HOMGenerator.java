package de.unisb.cs.st.evosuite.mutation.HOM;


import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.ga.SinglePointFixedCrossOver;
import de.unisb.cs.st.ga.GeneticAlgorithm;
import de.unisb.cs.st.ga.StandardGA;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;
import de.unisb.cs.st.javalanche.mutation.runtime.testDriver.MutationDriverShutdownHook;
import de.unisb.cs.st.javalanche.mutation.runtime.testDriver.MutationTestDriver;



public class HOMGenerator {
	
	private Thread shutDownThread;

	private MutationTestDriver test_driver;

	private static Logger logger = Logger.getLogger(HOMGenerator.class);
	
	public HOMGenerator(MutationTestDriver driver) {
		test_driver = driver;		
	}
	
	public void setup() {
		shutDownThread = new Thread(new MutationDriverShutdownHook(test_driver));
		//addMutationTestListener(new MutationObserver());
		//addListenersFromProperty();
		Runtime.getRuntime().addShutdownHook(shutDownThread);
	}
	
	public void pulldown() {
		//MutationObserver.reportAppliedMutations(); 	
		Runtime.getRuntime().removeShutdownHook(shutDownThread);
	}
	
	/*
	public void setSOMPopulation(int population_size) {
		int chromosome_length = hom_switcher.getNumMutants();
		
		for(int i=0; i<population_size; i++) {
			HOMChromosome individual = new HOMChromosome(chromosome_length);
			individual.flip(i);
			population.add(individual);
		}
	}
	*/
	

	// TODO: Only use mutants that are killed
	public void generateHOMs() {
		//getAllMutationsForClass
		//QueryManager.getNumberOfMutationsForClass();
		// TODO: Only mutants that are really executed?
				
		// The number of covered mutants is the chromosome length

		FragilityFitnessFunction fitness_function = new FragilityFitnessFunction(test_driver);
		List<Mutation> dead = fitness_function.analyzeFOMs();
		GeneticAlgorithm ga = new StandardGA(new HOMChromosomeFactory(dead));

		ga.setFitnessFunction(fitness_function);
		ga.setCrossOverFunction(new SinglePointFixedCrossOver());
		
		// Generate initial population
		//setRandomPopulation(population_size);
		//setSOMPopulation(population_size);
		
		ga.generateSolution();
		
		HOMChromosome best = (HOMChromosome) ga.getBestIndividual();
		logger.info("Best chromosome has "+best.getNumberOfMutations()+" mutations: ");
	}


}
