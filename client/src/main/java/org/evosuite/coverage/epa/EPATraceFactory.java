package org.evosuite.coverage.epa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.MethodCall;

public abstract class EPATraceFactory {

	public static List<EPATrace> buildEPATraces(ExecutionTrace executionTrace, EPA epa) {

		// Separate method call executions by `callingObjectID`
		Map<Integer, List<String>> methodCallExecutionsByCallingObjectID = new HashMap<>();
		for (MethodCall methodCallExecution : executionTrace.getMethodCalls()) {
			final int callingObjectID = methodCallExecution.callingObjectID;
			if (methodCallExecutionsByCallingObjectID.get(callingObjectID) == null) {
				methodCallExecutionsByCallingObjectID.put(callingObjectID, new ArrayList<>());
			}

			String methodName = methodCallExecution.methodName;
			methodCallExecutionsByCallingObjectID.get(callingObjectID).add(methodName);
		}
		
		// Transform each list of calls into a trace
		final List<EPATrace> collect = methodCallExecutionsByCallingObjectID.values().stream()
				.map(methodCallExecutions -> buildEPATrace(methodCallExecutions, epa))
				.collect(Collectors.toList());
		return collect;
	}

	private static EPATrace buildEPATrace(List<String> methodCallExecutions, EPA epa) {
		List<String> methodCallExecutionsLeft = new ArrayList<>(methodCallExecutions);
		final List<EPATransition> epaTransitions = new ArrayList<>();
		
		EPAState originState = epa.getInitialState();
		int firstIdx = 0;
		while (true) {
			// Break conditions: a) End of the method call executions list, or b) No `reportState()` calls remaining 
			if (firstIdx >= methodCallExecutionsLeft.size())
				break;
			
			methodCallExecutionsLeft = methodCallExecutionsLeft.subList(firstIdx, methodCallExecutionsLeft.size());

			final int reportStateIdx = methodCallExecutionsLeft.indexOf("reportState()V");
			if (reportStateIdx == -1)
				break;

			// Transition adding
			final String actionName = methodCallExecutionsLeft.get(reportStateIdx + 1);

			final String reportStateMethodName = methodCallExecutionsLeft.get(reportStateIdx - 1);
			final String destinationStateName = getStateNameFromReportStateMethodName(reportStateMethodName);
			final EPAState destinationState = epa.getStateByName(destinationStateName);
			epaTransitions.add(new EPATransition(originState, actionName, destinationState));

			originState = destinationState;
			firstIdx = reportStateIdx + 2;
		}
		return new EPATrace(epaTransitions);
	}

	private static String getStateNameFromReportStateMethodName(final String methodName) {
		return methodName.split("\\(")[0].replaceFirst("reportState", "");
	}
}
