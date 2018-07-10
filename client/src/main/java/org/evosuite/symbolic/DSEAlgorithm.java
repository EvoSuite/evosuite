package org.evosuite.symbolic;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;

public class DSEAlgorithm<T extends Chromosome> extends GeneticAlgorithm<T> {

	private final DSEChromosomeFactory<T> chromosomeFactory;

	public DSEAlgorithm(ChromosomeFactory<T> factory) {
		super(factory);
		if (!(factory instanceof DSEChromosomeFactory)) {
			throw new IllegalArgumentException(
					"DSE algorithm only accepts factories extending " + DSEChromosomeFactory.class.getName());
		} else {
			this.chromosomeFactory = (DSEChromosomeFactory<T>) factory;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 964984026539409121L;

	@Override
	protected void evolve() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializePopulation() {
		this.notifySearchStarted();
		for (int i = 0; i < chromosomeFactory.numberOfChromosomes(); i++) {
			T chromosome = chromosomeFactory.getChromosome(i);
			population.add(chromosome);
		}

	}

	@Override
	public void generateSolution() {
		if (population.isEmpty()) {
			initializePopulation();
		}
		
	}

}
