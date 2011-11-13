package de.unisb.cs.st.evosuite.testcase;

import java.util.HashMap;
import java.util.Map;

import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

public abstract class ExecutableChromosome extends Chromosome {
	private static final long serialVersionUID = 1L;

	protected transient ExecutionResult lastExecutionResult = null;

	protected transient Map<Mutation, ExecutionResult> lastMutationResult = new HashMap<Mutation, ExecutionResult>();

	public ExecutableChromosome() {
		super();
	}

	public void setLastExecutionResult(ExecutionResult lastExecutionResult) {
		this.lastExecutionResult = lastExecutionResult;
	}

	public ExecutionResult getLastExecutionResult() {
		return lastExecutionResult;
	}

	public void setLastExecutionResult(ExecutionResult lastExecutionResult,
	        Mutation mutation) {
		if (mutation == null)
			this.lastExecutionResult = lastExecutionResult;
		else
			this.lastMutationResult.put(mutation, lastExecutionResult);
	}

	public ExecutionResult getLastExecutionResult(Mutation mutation) {
		if (mutation == null)
			return lastExecutionResult;
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