package org.evosuite.statistics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.ga.SearchListener;
import org.evosuite.rmi.ClientServices;
import org.evosuite.sandbox.Sandbox;

public class StatisticsListener implements SearchListener {

	private BlockingQueue<Chromosome> individuals = new LinkedBlockingQueue<Chromosome>();
	
	private boolean done = false;
	
	private double bestFitness = Double.MAX_VALUE;
	
	private boolean minimizing = true;
	
	private Thread notifier;
	
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
						ClientServices.getInstance().getClientNode().updateStatistics(individual);
					} catch (InterruptedException e) {
						done = true;
					}
				}
			}
		};
		notifier.start();
		Sandbox.addPriviligedThread(notifier);
	}


	@Override
	public void iteration(GeneticAlgorithm algorithm) {
		// Enqueue current best individual
		// individuals.offer(algorithm.getBestIndividual());
	}

	@Override
	public void searchFinished(GeneticAlgorithm algorithm) {
		// If the search is finished, we may want to clear the queue and just send the final element?
		//individuals.clear(); // TODO: Maybe have a check on size
		done = true;
	}

	@Override
	public void searchStarted(GeneticAlgorithm algorithm) {
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
