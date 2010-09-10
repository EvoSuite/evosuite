package de.unisb.cs.st.evosuite.testcase;

import org.apache.log4j.Logger;

public class NullOutputObserver extends ExecutionObserver {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(PrimitiveOutputTraceObserver.class);
	
	private NullOutputTrace trace = new NullOutputTrace();
	
	@Override
	public void clear() {
		trace.trace.clear();
	}

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void statement(int position, Scope scope, VariableReference retval) {
		if(retval == null)
			return;

		Object object = scope.get(retval);
		trace.trace.put(position, object == null);
	}

	public NullOutputTrace getTrace() {
		return trace.clone();
	}
}
