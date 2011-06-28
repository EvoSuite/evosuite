package de.unisb.cs.st.evosuite.testcase;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

public abstract class ExecutableChromosome extends Chromosome {
	private static final long serialVersionUID = 1L;

	protected ExecutionResult lastExecutionResult = null;

	public ExecutableChromosome() {
		super();
	}

	public void setLastExecutionResult(ExecutionResult lastExecutionResult) {
		this.lastExecutionResult = lastExecutionResult;
	}

	public ExecutionResult getLastExecutionResult() {
		return lastExecutionResult;
	}

	abstract public ExecutionResult executeForFitnessFunction(TestSuiteFitnessFunction testSuiteFitnessFunction);
}