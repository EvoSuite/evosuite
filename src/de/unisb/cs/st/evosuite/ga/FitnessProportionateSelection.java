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

import java.util.ArrayList;
import java.util.List;


/**
 * Roulette wheel selection
 * @author Gordon Fraser
 *
 * TODO: Can only maximize fitness!
 */
public class FitnessProportionateSelection extends SelectionFunction {

	/** Sum of fitness values */
	private double fitness_sum = 0.0;
	
	/** Maximum fitness */
	private double max_fitness = 0.0;
	
	@Override
	public int getIndex(List<Chromosome> population) {
		boolean valid = false;
		int index = 1;

		if(fitness_sum == 0.0) {
			index = randomness.nextInt(population.size()); 
			valid = true;
		}
		while(!valid) {
		
			double rnd = randomness.nextDouble() * fitness_sum;

			for(index=0; index<population.size() && rnd>0.0F; index++) {
				if(maximize) {
					rnd -= population.get(index).getFitness();
				} else {
					// TODO: This hasn't been tested
					rnd -= max_fitness - population.get(index).getFitness();					
				}
			}
			if(index >= 1)
				valid = true;
		}
        return (index - 1);
	}
	
	/**
	 * Calculate total sum of fitnesses
	 * @param population
	 */
	private void setFitnessSum(List<Chromosome> population) {
		double sum = 0.0;
		max_fitness = 0.0;
		for(Chromosome c : population) {
			sum += c.getFitness();
			if(c.getFitness() > max_fitness)
				max_fitness = c.getFitness();
		}
		fitness_sum = sum;
	}
	
	/**
	 * Return n parents
	 * @param population
	 * @param number n
	 * @return
	 */
	public List<Chromosome> select(List<Chromosome> population, int number) {
		
		setFitnessSum(population);
		
		List<Chromosome> offspring = new ArrayList<Chromosome>();
		for(int i = 0; i < number; i++) {
			offspring.add(population.get(getIndex(population)));
		}
		return offspring;
	}

}
