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

import de.unisb.cs.st.evosuite.Properties;

/**
 * Implementation of steady state GA
 * 
 * @author Gordon Fraser
 *
 */
public class SteadyStateGA extends GeneticAlgorithm {


	protected ReplacementFunction replacement_function;
	
	/**
	 * Constructor
	 * @param factory
	 */
	public SteadyStateGA(ChromosomeFactory factory) {
		super(factory);

		setReplacementFunction(new FitnessReplacementFunction(selection_function));
	}
	

	
	@Override
	protected void evolve() {
		List<Chromosome> new_generation = new ArrayList<Chromosome>();

		
		// Elitism
		logger.debug("Elitism");		
		new_generation.addAll(elitism());
		
		// Add random elements
		// new_generation.addAll(randomism());
		
		while(new_generation.size() < Properties.POPULATION_SIZE && !isFinished()) {
			logger.debug("Generating offspring");		

			Chromosome parent1 = selection_function.select(population);
			Chromosome parent2 = selection_function.select(population);
			
			Chromosome offspring1 = parent1.clone();
			Chromosome offspring2 = parent2.clone();
			
			try {
				// Crossover
				if(randomness.nextDouble() <= crossover_rate) {
					crossover_function.crossOver(offspring1, offspring2);
				}
				
			} catch(ConstructionFailedException e) {
				logger.info("CrossOver failed");
				continue;
			}

			// Mutation
			offspring1.mutate();
			offspring2.mutate();

			// The two offspring replace the parents if and only if one of
			// the offspring is not worse than the best parent.
			
			
			// TODO: Change this to comparison < ?
			//fitness_function.getFitness(parent1);
			//fitness_function.getFitness(parent2);
			fitness_function.getFitness(offspring1);
			notifyEvaluation(offspring1);

			fitness_function.getFitness(offspring2);
			notifyEvaluation(offspring2);

			/*
			kinCompensation(parent1, new_generation);
			kinCompensation(parent2, new_generation);
			kinCompensation(offspring1, new_generation);
			kinCompensation(offspring2, new_generation);
			*/

			if(replacement_function.keepOffspring(parent1, parent2, offspring1, offspring2)) {
				logger.debug("Keeping offspring");	
				
				// Reject offspring straight away if it's too long
				int rejected = 0;
				if(isTooLong(offspring1) || offspring1.size() == 0) {
					rejected++;
				}
				else
					new_generation.add(offspring1);

				if(isTooLong(offspring2) || offspring2.size() == 0) {
					rejected++;
				}
				else
					new_generation.add(offspring2);

				if(rejected == 1)
					new_generation.add(randomness.choice(parent1, parent2));
				else if(rejected == 2) {
					new_generation.add(parent1);
					new_generation.add(parent2);
				}
			} else {
				logger.debug("Keeping parents");		
				new_generation.add(parent1);
				new_generation.add(parent2);
			}
		}
		
		population = new_generation;
		
		current_iteration++;
	}


	@Override
	public void generateSolution() {
		notifySearchStarted();
		
		current_iteration = 0;
		
		// Set up initial population
		generateRandomPopulation(Properties.POPULATION_SIZE);
		logger.debug("Calculating fitness of initial population");
		calculateFitness();

		logger.debug("Starting first iteration");
		while(!isFinished()) {
			logger.info("Current iteration: "+current_iteration);
			if(logger.isDebugEnabled()) {
				double avg = 0.0;
				int min = Integer.MAX_VALUE;
				int max = 0;
				for(Chromosome c : population) {
					avg += c.size();
					if(c.size() > max)
						max = c.size();
					if(c.size() < min)
						min = c.size();
				}
				avg = avg / population.size();
				System.out.println("CSV,"+current_iteration+","+min+","+avg+","+max);
			}
			//char esc = 27; 
			//String clear = esc + "[4A"; 
			//System.out.println(clear+"Current iteration: "+current_iteration+"                  ");
			this.notifyIteration();
			evolve();
			sortPopulation();
			//System.out.println("Best individual has fitness: "+population.get(0).getFitness()+"       ");
			//System.out.println("Worst individual has fitness: "+population.get(population.size()-1).getFitness()+"       ");
			logger.info("Best individual has fitness: "+population.get(0).getFitness());
			logger.info("Worst individual has fitness: "+population.get(population.size()-1).getFitness());
		}
		
		notifySearchFinished();
	}

	public void setReplacementFunction(ReplacementFunction replacement_function) {
		this.replacement_function = replacement_function;
	}

	public ReplacementFunction getReplacementFunction() {
		return replacement_function;
	}

}
