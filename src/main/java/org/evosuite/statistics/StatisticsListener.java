package org.evosuite.statistics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.ga.SearchListener;
import org.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import org.evosuite.ga.stoppingconditions.MaxTestsStoppingCondition;
import org.evosuite.rmi.ClientServices;
import org.evosuite.sandbox.Sandbox;
import org.evosuite.testsuite.TestSuiteChromosome;

/**
 * Client-side listener that transmits data to master
 * 
 * @author gordon
 *
 */
public class StatisticsListener implements SearchListener {

	private volatile BlockingQueue<Chromosome> individuals = new LinkedBlockingQueue<Chromosome>();
	
	private volatile boolean done = false;
	
	private volatile double bestFitness = Double.MAX_VALUE;
	
	private volatile boolean minimizing = true;
	
	private int numFitnessEvaluations = 0;
	
	private volatile Thread notifier;
	
	/**
	 * When did we send an individual due to a new generation iteration?
	 */
	private volatile long timeFromLastGenerationUpdate = 0;
	
	public StatisticsListener() {
		notifier = new Thread() {
			@Override
			public void run() {
				// Wait for new element in queue
				// If there is a new element, then send it to master through RMI
				while(!done || !individuals.isEmpty()) {
					Chromosome individual;
					try {
						individual = individuals.take();
						StatisticsSender.sendIndividualToMaster(individual);
					} catch (InterruptedException e) {
						done = true;
					}
				}
			}
		};
		Sandbox.addPriviligedThread(notifier);
		notifier.start();
	}


	@Override
	public void iteration(GeneticAlgorithm<?> algorithm) {
		
		long elapsed = System.currentTimeMillis() - timeFromLastGenerationUpdate;
		if(elapsed > Properties.TIMELINE_INTERVAL){
			/*
			 * We do not send an individual to Master at each new generation, because
			 * for fast CUTs we could have far too many generations.
			 * As this info is only used for time interval values, there
			 * is no point in sending too many 
			 */
			timeFromLastGenerationUpdate = System.currentTimeMillis();
			// Enqueue current best individual
			individuals.offer(algorithm.getBestIndividual());
		}	
	}

	@Override
	public void searchFinished(GeneticAlgorithm<?> algorithm) {
		// If the search is finished, we may want to clear the queue and just send the final element?
		//individuals.clear(); // TODO: Maybe have a check on size
		individuals.offer(algorithm.getBestIndividual());
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Statements_Executed, MaxStatementsStoppingCondition.getNumExecutedStatements());
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Tests_Executed, MaxTestsStoppingCondition.getNumExecutedTests());
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Generations, algorithm.getAge());
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Fitness_Evaluations, numFitnessEvaluations);

		if(algorithm.getBestIndividual() instanceof TestSuiteChromosome) {
			reportTestSuiteResult((TestSuiteChromosome) algorithm.getBestIndividual());
		}
		done = true;
		try {
			notifier.join(3000);
		} catch (InterruptedException e) {
			notifier.interrupt();
			Thread.currentThread().interrupt();//interrupted flag was reset
		}
	}
	
	private void reportTestSuiteResult(TestSuiteChromosome testSuite) {
		
	}

	@Override
	public void searchStarted(GeneticAlgorithm<?> algorithm) {
		done = false;
		if(algorithm.getFitnessFunction().isMaximizationFunction()) {
			bestFitness = 0.0;
			minimizing = false;
		} else {
			bestFitness = Double.MAX_VALUE;
			minimizing = true;
		}
	}
	
	@Override
	public void fitnessEvaluation(Chromosome individual) {
		numFitnessEvaluations++;
		
		double fitness = individual.getFitness();
		if(minimizing) {
			if(fitness < bestFitness) {
				bestFitness = fitness;

				individuals.offer(individual);
			}
		} else {
			if(fitness > bestFitness) {
				bestFitness = fitness;

				individuals.offer(individual);				
			}
		}
	}

	@Override
	public void modification(Chromosome individual) {
		// Nothing to do
	}

}
