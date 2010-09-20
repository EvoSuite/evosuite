package de.unisb.cs.st.evosuite.testcase;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class InspectorTraceObserver extends ExecutionObserver {


	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(InspectorTraceObserver.class);
	
	private InspectorManager manager = InspectorManager.getInstance();
	
	private InspectorTrace trace = new InspectorTrace();
	
	@Override
	public void clear() {
		trace.clear();
	}

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void statement(int position, Scope scope, VariableReference retval) {
		if(retval == null)
			return;
		
		List<Inspector> inspectors = manager.getInspectors(retval.getVariableClass());
		if(inspectors.isEmpty()) {
			return;
		}
		if(scope.get(retval) == null)
			return;
		
		List<Object> result = new ArrayList<Object>();
		for(Inspector i : inspectors) {
			result.add(i.getValue(scope.get(retval)));
			//logger.info("New inspector result for variable of type "+retval.getClassName()+"/" + retval.getVariableClass().getName()+": "+i.getClassName()+"."+i.getMethodCall()+" -> "+i.getValue(scope.get(retval)));
		}
		
		trace.inspector_results.put(position, result);
		trace.return_values.put(position, retval);
	}

	public InspectorTrace getTrace() {
		return trace.clone();
	}
}
