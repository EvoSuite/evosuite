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