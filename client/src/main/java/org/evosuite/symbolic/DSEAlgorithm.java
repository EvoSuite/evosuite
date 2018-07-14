package org.evosuite.symbolic;

import java.util.Collections;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;

public class DSEAlgorithm<T extends Chromosome> extends GeneticAlgorithm<T> {

	private final DSEDefaultChromosomeFactory<T> defaultChromosomeFactory;

	private final DSEExplorerFactory<T> explorerFactory;

	public DSEAlgorithm(ChromosomeFactory<T> factory, DSEExplorerFactory<T> explorerFactory) {
		super(factory);
		if (!(factory instanceof DSEDefaultChromosomeFactory)) {
			throw new IllegalArgumentException(
					"DSE algorithm only accepts factories extending " + DSEDefaultChromosomeFactory.class.getName());
		} else {
			this.defaultChromosomeFactory = (DSEDefaultChromosomeFactory<T>) factory;
			this.explorerFactory = explorerFactory;
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
		for (int i = 0; i < defaultChromosomeFactory.numberOfDefaultChromosomes(); i++) {
			T initialChromosome = defaultChromosomeFactory.getDefaultChromosome(i);
			population.add(initialChromosome);
		}
	}

	@Override
	public void generateSolution() {
		if (population.isEmpty()) {
			initializePopulation();
		}

		Collections.sort(population);

		for (int i = 0; i < population.size(); i++) {

			T individual = population.get(i);
			DSEExplorer<T> explorer = this.explorerFactory.buildExplorer(individual);

			while (explorer.hasNext()) {
				T newIndividual = explorer.next();
				population.add(newIndividual);
			}
		}

		this.notifySearchFinished();
	}

}
