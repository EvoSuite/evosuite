package de.unisb.cs.st.evosuite.junit;

import de.unisb.cs.st.evosuite.testcase.ExecutionTrace;

public class TestRun {
	private final ExecutionTrace executionTrace;
	private final Throwable failure;

	public TestRun(ExecutionTrace executionTrace, Throwable failure) {
		this.executionTrace = executionTrace;
		this.failure = failure;
	}

	public ExecutionTrace getExecutionTrace() {
		return executionTrace;
	}

	public Throwable getFailure() {
		return failure;
	}
}