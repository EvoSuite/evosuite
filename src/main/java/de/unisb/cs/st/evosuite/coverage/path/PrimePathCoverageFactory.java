/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
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
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.path;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.AbstractFitnessFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class PrimePathCoverageFactory extends AbstractFitnessFactory {

	private static List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		if (!goals.isEmpty())
			return goals;

		String targetMethod = Properties.TARGET_METHOD;

		for (String className : PrimePathPool.primePathMap.keySet()) {
			for (String methodName : PrimePathPool.primePathMap.get(className).keySet()) {

				if (!targetMethod.equals("") && !methodName.equals(targetMethod)) {
					continue;
				}
				for (PrimePath path : PrimePathPool.primePathMap.get(className).get(methodName)) {
					goals.add(new PrimePathTestFitness(path, className, methodName));
				}
			}
		}

		return goals;
	}

}
