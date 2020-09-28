/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
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
package org.evosuite.coverage.lcsaj;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.testsuite.AbstractFitnessFactory;

/**
 * <p>
 * LCSAJCoverageFactory class.
 * </p>
 */
public class LCSAJCoverageFactory extends
        AbstractFitnessFactory<LCSAJCoverageTestFitness> {

	/* (non-Javadoc)
	 * @see org.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	/** {@inheritDoc} */
	@Override
	public List<LCSAJCoverageTestFitness> getCoverageGoals() {
		List<LCSAJCoverageTestFitness> goals = new ArrayList<LCSAJCoverageTestFitness>();

		// Branches
		for (String className : LCSAJPool.lcsaj_map.keySet()) {
			for (String methodName : LCSAJPool.lcsaj_map.get(className).keySet()) {
				for (LCSAJ lcsaj : LCSAJPool.getLCSAJs(className, methodName))
					goals.add(new LCSAJCoverageTestFitness(className, methodName, lcsaj));
			}
		}

		return goals;
	}

}
