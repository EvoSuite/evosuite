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
package org.evosuite.coverage.aes.branch;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

public class UnreachableBranchCoverageTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 1L;

	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		return 1;
	}

	@Override
	public int compareTo(TestFitnessFunction other) {
		return 0;
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof UnreachableBranchCoverageTestFitness;
	}

	@Override
	public String getTargetClass() {
		return null;
	}

	@Override
	public String getTargetMethod() {
		return null;
	}

}
