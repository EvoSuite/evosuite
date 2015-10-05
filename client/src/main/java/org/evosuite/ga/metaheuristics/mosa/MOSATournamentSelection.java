package org.evosuite.ga.metaheuristics.mosa;

import java.util.Comparator;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.operators.selection.SelectionFunction;
import org.evosuite.runtime.Random;
import org.evosuite.utils.Randomness;


/**
 * Select an individual from a population as winner of a number of tournaments according
 * to the "non-dominance" relationship and the crowding distance
 *
 * @author Annibale, Fitsum
 */
public class MOSATournamentSelection<T extends Chromosome> extends SelectionFunction<T> {

	private static final long serialVersionUID = -7465418404056357932L;
	
	private static final Comparator<Object> comparator = new RankAndCrowdingDistanceComparator();

	/**
	 * {@inheritDoc}
	 *
	 * Perform the tournament on the population, return one index
	 */
	@Override
	public int getIndex(List<T> population) {
		int new_num = Randomness.nextInt(population.size());
		int winner = new_num;

		int round = 0;

		while (round < Properties.TOURNAMENT_SIZE - 1) {
			new_num = Randomness.nextInt(population.size());
			Chromosome selected = population.get(new_num);
			int flag = comparator.compare(selected, population.get(winner));
			if (flag==-1) {
				winner = new_num;
			} else if (flag==0 && Random.nextDouble()<=0.5){
					winner = new_num;
			}
			round++;
		}

		return winner;
	}

}
