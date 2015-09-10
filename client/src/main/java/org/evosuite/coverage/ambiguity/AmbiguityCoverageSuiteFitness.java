/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.ambiguity;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * <p>
 * AmbiguityCoverageSuiteFitness class.
 * </p>
 * 
 * @author José Campos
 */
public class AmbiguityCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static final long serialVersionUID = 1893060100346404496L;

	/**
	 * 
	 */
	@Override
	public double getFitness(
			AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {

		List<ExecutionResult> results = runTestSuite(suite);
		List<? extends TestFitnessFunction> goals = (List<? extends TestFitnessFunction>) AmbiguityCoverageFactory.retrieveCoverageGoals();
		List<StringBuilder> transposed_matrix = new ArrayList<StringBuilder>(AmbiguityCoverageFactory.getTransposedMatrix());

		int g_i = 0;
		for (TestFitnessFunction goal : goals) {
			StringBuilder str = new StringBuilder();
			for (ExecutionResult result : results) {
				if (goal.isCovered(result))
					str.append("1");
				else
					str.append("0");
			}

			try {
				transposed_matrix.set(g_i, str.append(transposed_matrix.get(g_i)));
			} catch (IndexOutOfBoundsException exp) {
				transposed_matrix.add(g_i, str);
			}

			g_i++;
		}

		//double fitness = FitnessFunction.normalize(AmbiguityCoverageFactory.getAmbiguity(transposed_matrix));
		double fitness = AmbiguityCoverageFactory.getAmbiguity(transposed_matrix);

		updateIndividual(this, suite, fitness);
		return fitness;
	}

	/**
	 * 
	 */
	@Override
	public boolean isMaximizationFunction() {
		return false;
	}
}
