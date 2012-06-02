/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
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
package de.unisb.cs.st.evosuite.testcase;

import java.util.HashMap;
import java.util.Map;

import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationExecutionResult;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

public abstract class ExecutableChromosome extends Chromosome {
	private static final long serialVersionUID = 1L;

	protected transient ExecutionResult lastExecutionResult = null;

	protected transient Map<Mutation, MutationExecutionResult> lastMutationResult = new HashMap<Mutation, MutationExecutionResult>();

	public ExecutableChromosome() {
		super();
	}

	public void setLastExecutionResult(ExecutionResult lastExecutionResult) {
		this.lastExecutionResult = lastExecutionResult;
	}

	public ExecutionResult getLastExecutionResult() {
		return lastExecutionResult;
	}

	public void setLastExecutionResult(MutationExecutionResult lastExecutionResult,
	        Mutation mutation) {
		this.lastMutationResult.put(mutation, lastExecutionResult);
	}

	public MutationExecutionResult getLastExecutionResult(Mutation mutation) {
		return lastMutationResult.get(mutation);
	}

	public void clearCachedResults() {
		this.lastExecutionResult = null;
		lastMutationResult.clear();
	}

	public void clearCachedMutationResults() {
		lastMutationResult.clear();
	}

	protected abstract void copyCachedResults(ExecutableChromosome other);

	abstract public ExecutionResult executeForFitnessFunction(
	        TestSuiteFitnessFunction testSuiteFitnessFunction);
}