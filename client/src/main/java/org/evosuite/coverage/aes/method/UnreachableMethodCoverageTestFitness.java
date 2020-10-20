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
package org.evosuite.coverage.aes.method;

import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;

public class UnreachableMethodCoverageTestFitness extends MethodCoverageTestFitness {

	private static final long serialVersionUID = -1696168329265661620L;

	public UnreachableMethodCoverageTestFitness() throws IllegalArgumentException {
		super("", "");
	}

	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
        return 1.0;
	}
}
