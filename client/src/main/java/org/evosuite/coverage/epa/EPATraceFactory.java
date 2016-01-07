package org.evosuite.coverage.epa;

import java.util.ArrayList;

import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.MethodCall;

public abstract class EPATraceFactory {

	public static EPATrace buildEPATrace(ExecutionTrace executionTrace, EPA epa) {
		ArrayList<EPATransition> epaTransitions = new ArrayList<>();
		EPAState originState = epa.getInitialState();

		for (MethodCall methodCallExecution : executionTrace.getMethodCalls()) {
			String methodName = methodCallExecution.methodName;
			String actionName = getCleanMethodName(methodName);
			actionName = actionName.equals("<init>") ? epa.getName() : actionName;
			if (!epa.containsAction(actionName)) {
				continue;
			}
			
			//final EPAState destinationState = epa.getStateByName(methodCallexecution.endState);
			// TODO: Remove this once MethodCall.endState is implemented
			final EPAState destinationState = epa.temp_anyPossibleDestinationState(originState, actionName);
			if (destinationState == null)
				throw new RuntimeException("Cannot create the EPA Trace");

			epaTransitions.add(new EPATransition(originState, actionName, destinationState));
			originState = destinationState;
		}
		
		EPATrace trace = new EPATrace(epaTransitions);
		return trace;
	}
	
	private static String getCleanMethodName(final String methodName) {
		String[] splitted = methodName.split("\\(");
		return splitted[0];
	}
}
