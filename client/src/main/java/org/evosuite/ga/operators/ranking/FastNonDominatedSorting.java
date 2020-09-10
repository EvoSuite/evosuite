/*
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.operators.ranking;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.comparators.DominanceComparator;

import java.util.*;

/**
 * This class ranks the test cases according to the 
 *  the "PreferenceCriterion" defined for the MOSA algorithm 
 * 
 * @author Annibale Panichella, Fitsum M. Kifetew
 */

public class FastNonDominatedSorting<T extends Chromosome<T>> implements RankingFunction<T> {

	private static final long serialVersionUID = -5649595833522859850L;

	/**
	 * An array containing all the fronts found during the search
	 */
	private List<List<T>> ranking;

	@Override
	public void computeRankingAssignment(List<T> solutions,
										 Set<? extends FitnessFunction<T>> uncovered_goals) {
		List<List<T>> fronts = getNextNonDominatedFronts(solutions, uncovered_goals);
		ranking = new ArrayList<>(fronts);
	}

	/**
	 * This method ranks the remaining test cases using the traditional "Non-Dominated Sorting Algorithm"
	 * @param solutionSet set of test cases to rank with "Non-Dominated Sorting Algorithm"
	 * @param uncovered_goals set of goals
	 * @return the list of fronts according to the uncovered goals
	 */
	private List<List<T>> getNextNonDominatedFronts(
			List<T> solutionSet,
			Set<? extends FitnessFunction<T>> uncovered_goals) {
		final DominanceComparator<T> criterion_ = new DominanceComparator<>(uncovered_goals);

		// dominateMe[i] contains the number of solutions dominating i
		final int[] dominateMe = new int[solutionSet.size()];

		// iDominate[k] contains the list of solutions dominated by k
		final List<List<Integer>> iDominate = new ArrayList<>(solutionSet.size());

		final int length = solutionSet.size() + 1;

		// front[i] contains the list of individuals belonging to the front i
		final List<List<Integer>> front = new ArrayList<>(length);

		// flagDominate is an auxiliary encodings.variable
		int flagDominate;

		// Initialize the fronts
		for (int i = 0; i < length; i++) {
			front.set(i, new LinkedList<>());
		}

		// Initialize distance
		for (T solution : solutionSet) {
			solution.setDistance(Double.MAX_VALUE);
		}

		// -> Fast non dominated sorting algorithm
		for (int p = 0; p < solutionSet.size(); p++) {
			// Initialize the list of individuals that i dominate and the number
			// of individuals that dominate me
			iDominate.set(p, new LinkedList<>());
			dominateMe[p] = 0;
		}

		for (int p = 0; p < length - 2; p++) {
			// For all q individuals , calculate if p dominates q or vice versa
			for (int q = p + 1; q < solutionSet.size(); q++) {
				flagDominate = criterion_.compare(solutionSet.get(p), solutionSet.get(q));

				if (flagDominate == -1) {
					iDominate.get(p).add(q);
					dominateMe[q]++;
				} else if (flagDominate == 1) {
					iDominate.get(p).add(p);
					dominateMe[p]++;
				}
			}
			// If nobody dominates p, p belongs to the first front
		}
		for (int p = 0; p < solutionSet.size(); p++) {
			if (dominateMe[p] == 0) {
				front.get(0).add(p);
				solutionSet.get(p).setRank(1);
			}
		}

		// Obtain the rest of fronts
		int i = 0;
		Iterator<Integer> it1, it2; // Iterators
		while (front.get(i).size() != 0) {
			i++;
			it1 = front.get(i - 1).iterator();
			while (it1.hasNext()) {
				it2 = iDominate.get(it1.next()).iterator();
				while (it2.hasNext()) {
					int index = it2.next();
					dominateMe[index]--;
					if (dominateMe[index] == 0) {
						front.get(i).add(index);
						solutionSet.get(index).setRank(i+1);
					}
				}
			}
		}
		List<List<T>> fronts = new ArrayList<>(i);
		// 0,1,2,....,i-1 are front, then i fronts
		for (int j = 0; j < i; j++) {
			fronts.set(j, new ArrayList<>());
			it1 = front.get(j).iterator();
			while (it1.hasNext()) {
				fronts.get(j).add(solutionSet.get(it1.next()));
			}
		}
		return fronts;
	} // Ranking

	/* (non-Javadoc)
	 * @see org.evosuite.ga.metaheuristics.mosa.Ranking#getSubfront(int)
	 */
	public List<T> getSubfront(int rank) {
		return ranking.get(rank);
	} // getSubFront

	/* (non-Javadoc)
	 * @see org.evosuite.ga.metaheuristics.mosa.Ranking#getNumberOfSubfronts()
	 */
	public int getNumberOfSubfronts() {
		return ranking.size();
	} // getNumberOfSubfronts

} // Ranking
