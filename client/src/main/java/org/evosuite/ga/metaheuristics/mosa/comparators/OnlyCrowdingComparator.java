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
package org.evosuite.ga.metaheuristics.mosa.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.evosuite.ga.Chromosome;

/**
 * Sort a Collection of Chromosomes by CrowdingDistance
 * 
 * @author Annibale Panichella, Fitsum M. Kifetew
 */
public class OnlyCrowdingComparator implements Comparator<Chromosome>, Serializable {
	
	private static final long serialVersionUID = -6576898111709166470L;

	@Override
	public int compare(Chromosome c1, Chromosome c2) {
		if (c1.getDistance() > c2.getDistance())
			return -1;
		else if (c1.getDistance() < c2.getDistance())
			return +1;
		else 
			return 0;
	}
}
