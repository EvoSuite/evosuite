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
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Gordon Fraser
 */
package org.evosuite.testcase;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationExecutionResult;
import org.evosuite.ga.Chromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
public abstract class ExecutableChromosome extends Chromosome {
	private static final long serialVersionUID = 1L;

	protected transient ExecutionResult lastExecutionResult = null;

	protected transient Map<Mutation, MutationExecutionResult> lastMutationResult = new HashMap<Mutation, MutationExecutionResult>();

	/**
	 * <p>Constructor for ExecutableChromosome.</p>
	 */
	public ExecutableChromosome() {
		super();
	}

	/**
	 * <p>Setter for the field <code>lastExecutionResult</code>.</p>
	 *
	 * @param lastExecutionResult a {@link org.evosuite.testcase.execution.ExecutionResult} object.
	 */
	public void setLastExecutionResult(ExecutionResult lastExecutionResult) {
		this.lastExecutionResult = lastExecutionResult;
	}

	/**
	 * <p>Getter for the field <code>lastExecutionResult</code>.</p>
	 *
	 * @return a {@link org.evosuite.testcase.execution.ExecutionResult} object.
	 */
	public ExecutionResult getLastExecutionResult() {
		return lastExecutionResult;
	}
	
	/**
	 * <p>Setter for the field <code>lastExecutionResult</code>.</p>
	 *
	 * @param lastExecutionResult a {@link org.evosuite.coverage.mutation.MutationExecutionResult} object.
	 * @param mutation a {@link org.evosuite.coverage.mutation.Mutation} object.
	 */
	public void setLastExecutionResult(MutationExecutionResult lastExecutionResult,
	        Mutation mutation) {
		this.lastMutationResult.put(mutation, lastExecutionResult);
	}

	/**
	 * <p>Getter for the field <code>lastExecutionResult</code>.</p>
	 *
	 * @param mutation a {@link org.evosuite.coverage.mutation.Mutation} object.
	 * @return a {@link org.evosuite.coverage.mutation.MutationExecutionResult} object.
	 */
	public MutationExecutionResult getLastExecutionResult(Mutation mutation) {
		return lastMutationResult.get(mutation);
	}
	

	/**
	 * <p>clearCachedResults</p>
	 */
	public void clearCachedResults() {
		this.lastExecutionResult = null;
		lastMutationResult.clear();
	}

	/**
	 * <p>clearCachedMutationResults</p>
	 */
	public void clearCachedMutationResults() {
		lastMutationResult.clear();
	}

	/**
	 * <p>copyCachedResults</p>
	 *
	 * @param other a {@link org.evosuite.testcase.ExecutableChromosome} object.
	 */
	protected abstract void copyCachedResults(ExecutableChromosome other);

	/**
	 * <p>executeForFitnessFunction</p>
	 *
	 * @param testSuiteFitnessFunction a {@link org.evosuite.testsuite.TestSuiteFitnessFunction} object.
	 * @return a {@link org.evosuite.testcase.execution.ExecutionResult} object.
	 */
	abstract public ExecutionResult executeForFitnessFunction(
	        TestSuiteFitnessFunction testSuiteFitnessFunction);
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
    IOException {
		ois.defaultReadObject();
		lastExecutionResult = null;
		lastMutationResult = new HashMap<Mutation, MutationExecutionResult>();
	}
}
