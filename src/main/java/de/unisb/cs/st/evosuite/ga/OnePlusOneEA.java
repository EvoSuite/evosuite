/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of the GA library.
 * 
 * GA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with GA.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.unisb.cs.st.evosuite.ga;

/**
 * (1+1)EA
 * @author Gordon Fraser
 *
 */
public class OnePlusOneEA extends GeneticAlgorithm {

	private ReplacementFunction replacement_function;
	
	/**
	 * Constructor
	 * @param factory
	 */
	public OnePlusOneEA(ChromosomeFactory factory) {
		super(factory);
		setReplacementFunction(new FitnessReplacementFunction(selection_function));
	}
	
	public void setReplacementFunction(ReplacementFunction replacement_function) {
		this.replacement_function = replacement_function;
	}
	
	@Override
	protected void evolve() {

		Chromosome parent = population.get(0);
		Chromosome offspring = parent.clone();
		
		offspring.mutate();
		
		fitness_function.getFitness(offspring);
		notifyEvaluation(offspring);
		
		if(replacement_function.keepOffspring(parent, offspring)) {
			logger.debug("Replacing old population");
			population.set(0, offspring);
		} else {
			logger.debug("Keeping old population");
		}
		current_iteration++;
	}

	@Override
	public void generateSolution() {	
		notifySearchStarted();
		
		current_iteration = 0;
		
		// Only one parent 
		generateRandomPopulation(1);
		fitness_function.getFitness(population.get(0));
		logger.debug("Initial fitness: "+population.get(0).getFitness());

		while(!isFinished()) {
			logger.debug("Current population: "+getAge()+"/"+max_iterations);
			logger.debug("Best fitness: "+getBestIndividual().getFitness());
			this.notifyIteration();
			evolve();
		}
		notifySearchFinished();
	}
}
