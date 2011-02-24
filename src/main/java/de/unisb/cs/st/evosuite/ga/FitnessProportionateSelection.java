/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of the GA library.
 * 
 * GA is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * GA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * GA. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.ga;

import java.util.List;

/**
 * Roulette wheel selection
 * 
 * @author Gordon Fraser
 * 
 *         
 */
public class FitnessProportionateSelection extends SelectionFunction {

	/** Sum of fitness values, depending on minimization/maximization of the fitness function */
	private double sum_value = 0.0;


	@Override
	public int getIndex(List<Chromosome> population) 
	{
		//special case
		if(sum_value == 0d)
		{
			//here does not matter whether maximize or not.
			//we need to take at random, otherwise it d be always the first that d be chosen
			return randomness.nextInt(population.size());
		}
		
		double rnd = randomness.nextDouble() * sum_value;

		for (int i=0; i < population.size(); i++) 
		{
			double fit = population.get(i).getFitness();

			if(!maximize)
				fit = invert(fit);

			if(fit >= rnd)
				return i;
			else
				rnd = rnd - fit; 
		}

		//now this should never happens, but possible issues with rounding errors in for example "rnd = rnd - fit"
		//in such a case, we just return a random index and we log it

		logger.debug("ATTENTION: Possible issue in FitnessProportionateSelection");
		return randomness.nextInt(population.size());
	}

	/**
	 * Calculate total sum of fitnesses
	 * 
	 * @param population
	 */
	private void setSum(List<Chromosome> population) {
		sum_value = 0;
		for (Chromosome c : population) 
		{
			double v = c.getFitness(); 
			if(!maximize)
				v = invert(v);

			sum_value += v;
		}
	}

	/*
	 * used to handle the case of minimizing the fitness
	 */
	private double invert(double x)
	{
		return 1d / (x + 1d);
	}

	/**
	 * Return n parents
	 * 
	 * @param population
	 * @param number
	 *            n
	 * @return
	 */
	@Override
	public List<Chromosome> select(List<Chromosome> population, int number) {

		setSum(population);
		return super.select(population, number);
	}

}
