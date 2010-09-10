package de.unisb.cs.st.evosuite.testcase;


import org.apache.log4j.Logger;

public class PrimitiveOutputTraceObserver extends ExecutionObserver {

	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(PrimitiveOutputTraceObserver.class);
	
	private PrimitiveOutputTrace trace = new PrimitiveOutputTrace();
	
	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub
		
	}
		
	@Override
	public void statement(int position, Scope scope, VariableReference retval) {
		if(retval == null)
			return;

		Object object = scope.get(retval);
		if(object == null || object.getClass().isPrimitive() || object.getClass().isEnum() || isWrapperType(object.getClass())) {
			trace.trace.put(position, object);
			/*
			if(object == null)
				logger.info("Adding null (Type: "+retval.type.getName()+")");
			else
				logger.info("Adding object of type "+object.getClass().getName());
				*/
		}
		//else
		//	logger.info("Not adding object of type "+object.getClass().getName());
	}

	@Override
	public void clear() {
		trace.trace.clear();
	}
	
	public PrimitiveOutputTrace getTrace() {
		return trace.clone();
	}

}
