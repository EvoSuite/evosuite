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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.comparators.SortByFitness;

/**
 * This class implements different variants of Crowding Distance for many-objective problems
 * 
 * @author Annibale Panichella
 */
public class CrowdingDistance<T extends Chromosome> {
	
	/**
	 * Method used to assign the 'traditional' Crowding Distance
	 * @param front front of non-dominated solutions/tests
	 * @param set set of goals/targets (e.g., branches) to consider
	 */
	protected void crowdingDistanceAssignment(List<T> front, Set<FitnessFunction<T>> set) {
		int size = front.size();

		if (size == 0)
			return;
		if (size == 1) {
			front.get(0).setDistance(Double.POSITIVE_INFINITY);
			return;
		}
		if (size == 2) {
			front.get(0).setDistance(Double.POSITIVE_INFINITY);
			front.get(1).setDistance(Double.POSITIVE_INFINITY);
			return;
		}

		for (int i = 0; i < size; i++)
			front.get(i).setDistance(0.0);

		double objetiveMaxn;
		double objetiveMinn;
		double distance;

		for (final FitnessFunction<?> ff : set) {
			// Sort the population by Fit n
			Collections.sort(front, new SortByFitness(ff, false));

			objetiveMinn = front.get(0).getFitness(ff);
			objetiveMaxn = front.get(front.size() - 1).getFitness(ff);

			// set crowding distance
			front.get(0).setDistance(Double.POSITIVE_INFINITY);
			front.get(size - 1).setDistance(Double.POSITIVE_INFINITY);

			for (int j = 1; j < size - 1; j++) {
				distance = front.get(j + 1).getFitness(ff) - front.get(j - 1).getFitness(ff);
				distance = distance / (objetiveMaxn - objetiveMinn);
				distance += front.get(j).getDistance();
				front.get(j).setDistance(distance);
			}
		}
	}

	/**
	 * This method implements a variant of the crowding distance named "subvector-dominance-assignment"
	 * proposed by K\"{o}ppen and Yoshida in :
	 * [1] Mario K\"{o}ppen and Kaori Yoshida, "Substitute Distance Assignments in NSGA-II for handling Many-objective 
	 * Optimization Problems", Evolutionary Multi-Criterion Optimization, Volume 4403 of the series Lecture Notes 
	 * in Computer Science pp 727-741.
	 * @param front front of non-dominated solutions/tests
	 * @param set set of goals/targets (e.g., branches) to consider
	 */
	public void subvectorDominanceAssignment(List<T> front, Set<FitnessFunction<T>> set) {
		int size = front.size();
		if (front.size() == 1){
			front.get(0).setDistance(Double.POSITIVE_INFINITY);
			return;
		}

		for (int i = 0; i < size; i++)
			front.get(i).setDistance(Double.MAX_VALUE);

		int dominate1, dominate2;
		for (int i = 0; i<front.size()-1; i++){
			T p1 = front.get(i);
			for (int j = i+1; j<front.size(); j++){
				T p2 = front.get(j);
				dominate1 = 0;
				dominate2 = 0;
				for (final FitnessFunction<T> ff : set) {
					double value1 = p1.getFitness(ff);
					double value2 = p2.getFitness(ff);
					if (value1 < value2)
						dominate1++;
					else if (value1 > value2)
						dominate2++;
				}
				p1.setDistance(Math.min(dominate1, p1.getDistance()));
				p2.setDistance(Math.min(dominate2, p2.getDistance()));
			}
		}
	}

	/**
	 * This method implements a "fast" version of the variant of the crowding distance named "epsilon-dominance-assignment"
	 * proposed by K\"{o}ppen and Yoshida in :
	 * [1] Mario K\"{o}ppen and Kaori Yoshida, "Substitute Distance Assignments in NSGA-II for handling Many-objective 
	 * Optimization Problems", Evolutionary Multi-Criterion Optimization, Volume 4403 of the series Lecture Notes 
	 * in Computer Science pp 727-741.
	 * @param front front of non-dominated solutions/tests
	 * @param set set of goals/targets (e.g., branches) to consider
	 */
	public void fastEpsilonDominanceAssignment(List<T> front, Set<FitnessFunction<T>> set) {
		double value;
		for (T test : front){
			test.setDistance(0);
		}

		for (final FitnessFunction<T> ff : set) {
			double min = Double.POSITIVE_INFINITY;
			List<T> minSet = new ArrayList<T>(front.size());
			double max = 0;
			for (T test : front){
				value = test.getFitness(ff);
				if (value < min){
					min = value;
					minSet.clear();
					minSet.add(test);
				} else if (value == min)
					minSet.add(test);
				
				if (value > max){
					max = value;
				} 
			}

			if (max == min)
				continue;
			
			for (T test : minSet){
				double numer = (front.size() - minSet.size());
				double demon = front.size();
				test.setDistance(Math.max(test.getDistance(), numer/demon));
			}
		}
	}

}
