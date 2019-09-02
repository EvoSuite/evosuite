/*
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
package org.evosuite.ga.operators.ranking;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.comparators.DominanceComparator;
import org.evosuite.ga.comparators.PreferenceSortingComparator;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class ranks the test cases according to the
 *  the "PreferenceCriterion" defined for the MOSA algorithm
 *
 * @author Annibale Panichella, Fitsum M. Kifetew
 */
public class RankBasedPreferenceSorting<T extends Chromosome> implements RankingFunction<T> {

	private static final long serialVersionUID = -6636175563989586394L;

	private static final Logger logger = LoggerFactory.getLogger(RankBasedPreferenceSorting.class);

	/**
	 * A list containing all the fronts found during the search.
	 */
	private List<List<T>> fronts = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void computeRankingAssignment(List<T> solutions, Set<? extends FitnessFunction<T>> uncoveredGoals) {
		if (solutions.isEmpty()) {
			logger.debug("solution is empty");
			return;
		}

		this.fronts = new ArrayList<>(solutions.size());

		// first apply the "preference sorting" to the first front only
		// then compute the ranks according to the non-dominate sorting algorithm
		List<T> zeroFront = this.getZeroFront(solutions, uncoveredGoals);
		this.fronts.add(zeroFront);
		int frontIndex = 1;

		if (zeroFront.size() < Properties.POPULATION) {
			int rankedSolutions = zeroFront.size();
			DominanceComparator<T> comparator = new DominanceComparator<>(uncoveredGoals);

			List<T> remaining = new ArrayList<>(solutions.size());
			remaining.addAll(solutions);
			remaining.removeAll(zeroFront);
			while(rankedSolutions < Properties.POPULATION && remaining.size() > 0) {
				List<T> newFront = this.getNonDominatedSolutions(remaining, comparator, frontIndex);
				this.fronts.add(newFront);
				remaining.removeAll(newFront);
				rankedSolutions += newFront.size();
				frontIndex++;
			}

		} else {
			List<T> remaining = new ArrayList<>(solutions.size());
			remaining.addAll(solutions);
			remaining.removeAll(zeroFront);

			for (T t : remaining) {
				t.setRank(frontIndex);
			}
			this.fronts.add(remaining);
		}
	}

	/**
	 * Returns the first (i.e. non-dominated) sub-front.
	 *
	 * @param solutionSet the solutions to rank
	 * @param uncoveredGoals the goals used for ranking
	 * @return the non-dominated solutions (first sub-front)
	 */
	private List<T> getZeroFront(List<T> solutionSet, Set<? extends FitnessFunction<T>> uncoveredGoals) {
		Set<T> zeroFront = new LinkedHashSet<>(solutionSet.size());
		for (FitnessFunction<T> f : uncoveredGoals) {
			// for each uncovered goal, peak up the best tests using the proper comparator
			PreferenceSortingComparator<T> comp = new PreferenceSortingComparator<>(f);

			T best = null;
			for (T test : solutionSet) {
				int flag = comp.compare(test, best);
				if (flag < 0 || (flag == 0  && Randomness.nextBoolean())) {
					best = test;
				}
			}
			assert best != null;

			best.setRank(0);
			zeroFront.add(best);
		}
		return new ArrayList<>(zeroFront);
	}

	private List<T> getNonDominatedSolutions(List<T> solutions, DominanceComparator<T> comparator, int frontIndex) {
		List<T> front = new ArrayList<>(solutions.size());
		for (T p : solutions) {
			boolean isDominated = false;
			List<T> dominatedSolutions = new ArrayList<>(solutions.size());
			for (T best : front) {
				int flag = comparator.compare(p, best);
				if (flag < 0) {
					dominatedSolutions.add(best);
				}
				if (flag > 0) {
					isDominated = true;
					break;
				}
			}
			if (isDominated) {
				continue;
			}

			p.setRank(frontIndex);
			front.add(p);
			front.removeAll(dominatedSolutions);
		}
		return front;
	}

	/**
	 * {@inheritDoc}
	 * @return
	 */
	public List<T> getSubfront(int rank) {
		if (this.fronts == null || rank >= this.fronts.size()) {
			return new ArrayList<>();
		}
		return this.fronts.get(rank);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getNumberOfSubfronts() {
		return this.fronts.size();
	}
}
