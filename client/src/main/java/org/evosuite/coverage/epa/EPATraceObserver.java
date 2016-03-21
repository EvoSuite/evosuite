package org.evosuite.coverage.epa;

import java.util.Set;

import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.Statement;

public class EPATraceObserver extends ExecutionObserver {

	public EPATraceObserver() {
	}

	@Override
	public void output(int position, String output) {
		// skip
	}

	@Override
	public void beforeStatement(Statement statement, Scope scope) {
		// EPAMonitor takes care of tracing
	}

	/**
	 * Appends a new EPA transition if necessary.
	 * 
	 * @param statement
	 * @param scope
	 * @param exception
	 */
	@Override
	public void afterStatement(Statement statement, Scope scope, Throwable exception) {
		// EPAMonitor takes care of tracing
	}

	/**
	 * Copies the observed EPA Transitions as EPA traces into the execution
	 * result
	 * 
	 * @param r
	 * @param s
	 */
	@Override
	public void testExecutionFinished(ExecutionResult r, Scope s) {
		final Set<EPATrace> traces = EPAMonitor.getInstance().getTraces();
		r.addEPATraces(traces);
		EPAMonitor.reset();
	}

	/**
	 * Clears all observed EPA Transitions
	 */
	@Override
	public void clear() {
		EPAMonitor.reset();
	}

}
