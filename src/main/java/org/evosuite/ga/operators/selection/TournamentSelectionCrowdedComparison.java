package org.evosuite.ga.operators.selection;

import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;

/**
 * Select an individual from a population using the crowdedComparisonOperator
 * from NSGA-II as winner of a number of tournaments
 * 
 * @author José Campos
 */
public class TournamentSelectionCrowdedComparison<T extends Chromosome> extends SelectionFunction<T>
{
	private static final long serialVersionUID = -6887165634607218631L;

	@Override
	public int getIndex(List<T> population)
	{
		int size = population.size();
		int winner = 0;

		T p1 = population.get(winner);

		int round_limit = Properties.TOURNAMENT_SIZE > size ? size : Properties.TOURNAMENT_SIZE;
		for (int round = 1; round < round_limit; round++) {
			T p2 = population.get(round);

			if (maximize) {
				if (p1.getRank() < p2.getRank())
					winner = round;
				else if (p1.getRank() > p2.getRank()) { /* empty */}
				else if (p1.getRank() == p2.getRank())
					winner = (p1.getDistance() >= p2.getDistance()) ? winner : round;
			}
			else {
				if (p1.getRank() < p2.getRank()) { /* empty */}
				else if (p1.getRank() > p2.getRank())
					winner = round;
				else if (p1.getRank() == p2.getRank())
					winner = (p1.getDistance() >= p2.getDistance()) ? winner : round;
			}
		}

		return winner;
	}
}
