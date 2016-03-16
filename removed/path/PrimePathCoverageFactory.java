/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
/**
 * 
 */
package org.evosuite.coverage.path;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.coverage.MethodNameMatcher;
import org.evosuite.testsuite.AbstractFitnessFactory;

/**
 * <p>
 * PrimePathCoverageFactory class.
 * </p>
 * 
 * @author Gordon Fraser
 */
public class PrimePathCoverageFactory extends
        AbstractFitnessFactory<PrimePathTestFitness> {

	private static List<PrimePathTestFitness> goals = new ArrayList<PrimePathTestFitness>();

	/* (non-Javadoc)
	 * @see org.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	/** {@inheritDoc} */
	@Override
	public List<PrimePathTestFitness> getCoverageGoals() {
		if (!goals.isEmpty())
			return goals;

		final MethodNameMatcher matcher = new MethodNameMatcher();
		
		for (String className : PrimePathPool.primePathMap.keySet()) {
			for (String methodName : PrimePathPool.primePathMap.get(className).keySet()) {

				if (!matcher.methodMatches(methodName)) {
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
