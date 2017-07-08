/**
 *
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
package org.evosuite.ga.metaheuristics.mosa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.mosa.comparators.MOSADominanceComparator;

/**
 * This class ranks the test cases according to the 
 *  the "PreferenceCriterion" defined for the MOSA algorithm 
 * 
 * @author Annibale Panichella, Fitsum M. Kifetew
 */

public class FastNonDominatedSorting<T extends Chromosome> implements Ranking<T> {

	/**
	 * An array containing all the fronts found during the search
	 */
	private List<T>[] ranking_;

	/**
	 * Set used to store the goals that are covered from a population being sorted
	 */
	private Map<FitnessFunction<T>, T> newCoveredGoals = new LinkedHashMap<FitnessFunction<T>, T>();

	@SuppressWarnings("unchecked")
	@Override
	public void computeRankingAssignment(List<T> solutions, Set<FitnessFunction<T>> uncovered_goals) {
		List<T>[] fronts = getNextNonDominatedFronts(solutions, uncovered_goals);
		ranking_ = new ArrayList[fronts.length];
		for (int i = 0; i < fronts.length; i++)
			ranking_[i] = fronts[i];
	}


	/**
	 * This method ranks the remaining test cases using the traditional "Non-Dominated Sorting Algorithm"
	 * @param solutionSet set of test cases to rank with "Non-Dominated Sorting Algorithm"
	 * @param covered_goal set of goals
	 * @return the list of fronts according to the uncovered goals
	 */
	@SuppressWarnings("unchecked")
	private List<T>[] getNextNonDominatedFronts(List<T> solutionSet, Set<FitnessFunction<T>> uncovered_goals) {

		MOSADominanceComparator<T> criterion_ = new MOSADominanceComparator<T>(uncovered_goals);
		List<T> solutionSet_ = solutionSet;

		// dominateMe[i] contains the number of solutions dominating i
		int[] dominateMe = new int[solutionSet_.size()];

		// iDominate[k] contains the list of solutions dominated by k
		List<Integer>[] iDominate = new List[solutionSet_.size()];

		// front[i] contains the list of individuals belonging to the front i
		List<Integer>[] front = new List[solutionSet_.size() + 1];

		// flagDominate is an auxiliary encodings.variable
		int flagDominate;

		// Initialize the fronts
		for (int i = 0; i < front.length; i++)
			front[i] = new LinkedList<Integer>();

		// Initialize distance
		for (int p = 0; p < (solutionSet_.size()); p++) {
			solutionSet.get(p).setDistance(Double.MAX_VALUE);
		}

		// -> Fast non dominated sorting algorithm
		for (int p = 0; p < solutionSet_.size(); p++) {
			// Initialize the list of individuals that i dominate and the number
			// of individuals that dominate me
			iDominate[p] = new LinkedList<Integer>();
			dominateMe[p] = 0;
		}

		for (int p = 0; p < (solutionSet_.size() - 1); p++) {
			// For all q individuals , calculate if p dominates q or vice versa
			for (int q = p + 1; q < solutionSet_.size(); q++) {
				flagDominate = criterion_.compare(solutionSet.get(p), solutionSet.get(q));

				if (flagDominate == -1) {
					iDominate[p].add(q);
					dominateMe[q]++;
				} else if (flagDominate == 1) {
					iDominate[q].add(p);
					dominateMe[p]++;
				}
			}
			// If nobody dominates p, p belongs to the first front
		}
		for (int p = 0; p < solutionSet_.size(); p++) {
			if (dominateMe[p] == 0) {
				front[0].add(p);
				solutionSet.get(p).setRank(1);
			}
		}

		// Obtain the rest of fronts
		int i = 0;
		Iterator<Integer> it1, it2; // Iterators
		while (front[i].size() != 0) {
			i++;
			it1 = front[i - 1].iterator();
			while (it1.hasNext()) {
				it2 = iDominate[it1.next()].iterator();
				while (it2.hasNext()) {
					int index = it2.next();
					dominateMe[index]--;
					if (dominateMe[index] == 0) {
						front[i].add(index);
						solutionSet_.get(index).setRank(i+1);
					}
				}
			}
		}
		List<T>[] fronts = new ArrayList[i];
		// 0,1,2,....,i-1 are front, then i fronts
		for (int j = 0; j < i; j++) {
			fronts[j] = new ArrayList<T>();
			it1 = front[j].iterator();
			while (it1.hasNext()) {
				fronts[j].add(solutionSet.get(it1.next()));
			}
		}
		return fronts;
	} // Ranking

	/* (non-Javadoc)
	 * @see org.evosuite.ga.metaheuristics.mosa.Ranking#getSubfront(int)
	 */
	public List<T> getSubfront(int rank) {
		return ranking_[rank];
	} // getSubFront

	/* (non-Javadoc)
	 * @see org.evosuite.ga.metaheuristics.mosa.Ranking#getNumberOfSubfronts()
	 */
	public int getNumberOfSubfronts() {
		return ranking_.length;
	} // getNumberOfSubfronts

	public Map<FitnessFunction<T>, T> getNewCoveredGoals() {
		return newCoveredGoals;
	}

} // Ranking
