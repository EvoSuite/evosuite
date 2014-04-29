package org.evosuite.junit;

import java.util.LinkedHashSet;
import java.util.Set;

import org.evosuite.testcase.ExecutionTrace;
import org.evosuite.testcase.ExecutionTracer;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

public class ExecutionTraceRunListener extends RunListener {

	private Set<ExecutionTrace> traces = new LinkedHashSet<ExecutionTrace>();
	
	@Override
	public void testFinished(Description description) throws Exception {
		ExecutionTrace trace = ExecutionTracer.getExecutionTracer().getTrace();
		traces.add(trace);
	}
	

	public Set<ExecutionTrace> getExecutionTraces() {
		return traces;
	}
}
