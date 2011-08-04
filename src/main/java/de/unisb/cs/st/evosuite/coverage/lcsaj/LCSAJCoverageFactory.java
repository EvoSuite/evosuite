/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.coverage.lcsaj;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.AbstractFitnessFactory;

/**
 * @author
 * 
 */
public class LCSAJCoverageFactory extends AbstractFitnessFactory {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();

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
