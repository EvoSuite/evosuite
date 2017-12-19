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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.mosa.comparators.MOSADominanceComparator;
import org.evosuite.ga.metaheuristics.mosa.comparators.PreferenceSortingComparator;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class ranks the test cases according to the 
 *  the "PreferenceCriterion" defined for the MOSA algorithm 
 * 
 * @author Annibale Panichella, Fitsum M. Kifetew
 */

public class RankBasedPreferenceSorting<T extends Chromosome> implements Ranking<T> {

	private static final Logger logger = LoggerFactory.getLogger(RankBasedPreferenceSorting.class);

	/**
	 * An array containing all the fronts found during the search
	 */
	private Map<Integer, List<T>> fronts = new LinkedHashMap<Integer, List<T>>();

	@Override
	public void computeRankingAssignment(List<T> solutions, Set<FitnessFunction<T>> uncovered_goals) {
		if (solutions.isEmpty()) {
			return;
		}

		fronts.clear();

		// first apply the "preference sorting" to the first front only
		// then compute the ranks according to the non-dominate sorting algorithm
		List<T> zero_front = getZeroFront(solutions, uncovered_goals);
		fronts.put(0, zero_front);

		if (zero_front.size() < Properties.POPULATION){
			int rankedSolutions = zero_front.size();
			MOSADominanceComparator<T> comparator =  new MOSADominanceComparator<T>(uncovered_goals);

			List<T> remaining = new ArrayList<T>(solutions.size());
			remaining.addAll(solutions);
			remaining.removeAll(zero_front);
			int front_index = 1;
			while(rankedSolutions < Properties.POPULATION && remaining.size()>0){
				List<T> new_front = getNonDominatedSolutions(remaining, comparator);
				fronts.put(front_index, new_front);
				remaining.removeAll(new_front);
				front_index++;
				rankedSolutions += new_front.size();
			}
		} else {
			List<T> remaining = new ArrayList<T>(solutions.size());
			remaining.addAll(solutions);
			remaining.removeAll(zero_front);
			fronts.put(1, remaining);
		}

		for (Integer index : fronts.keySet()){
			//logger.error("Front {} size {}", index, ranking_[index].size());
			for (T p : fronts.get(index))
				p.setRank(index);
		}	
	}

	private List<T> getZeroFront(List<T> solutionSet, Set<FitnessFunction<T>> uncovered_goals){
		Set<T> zero_front = new LinkedHashSet<T>(solutionSet.size());
		for (FitnessFunction<T> f : uncovered_goals){
			//for each goals:
			// peak up the best tests using the proper comparator
			PreferenceSortingComparator<T> comp = new PreferenceSortingComparator<T>(f);

			T best = null;
			for (T test : solutionSet){
				int flag = comp.compare(test, best);
				if (flag < 0 || (flag == 0  && Randomness.nextBoolean())){
					best = test;
				} 
			}
			assert best != null;
			zero_front.add(best);
		}
		List<T> list = new ArrayList<T>(zero_front.size());
		list.addAll(zero_front);
		return list;
	}

	private List<T> getNonDominatedSolutions(List<T> solutions, MOSADominanceComparator<T> comparator){
		List<T> front = new ArrayList<T>(solutions.size());
		boolean isDominated;
		for (T p : solutions){
			isDominated = false;
			List<T> dominatedSolutions = new ArrayList<T>(solutions.size());
			for (T best : front){
				int flag = comparator.compare(p, best);
				if (flag == -1) {
					dominatedSolutions.add(best);
				}
				if (flag == +1){
					isDominated = true;
				}	
			}
			if (isDominated)
				continue;

			front.add(p);
			front.removeAll(dominatedSolutions);
		}
		return front;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.ga.metaheuristics.mosa.Ranking#getSubfront(int)
	 */
	public List<T> getSubfront(int rank) {
		List<T> subFront = fronts.get(rank);
		if (subFront == null) {
			subFront = new ArrayList<T>();
		}
		return subFront;
	} // getSubFront

	/* (non-Javadoc)
	 * @see org.evosuite.ga.metaheuristics.mosa.Ranking#getNumberOfSubfronts()
	 */
	public int getNumberOfSubfronts() {
		return fronts.keySet().size();
	} // getNumberOfSubfronts

} // Ranking
