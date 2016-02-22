package org.evosuite.coverage.epa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.MethodCall;

public abstract class EPATraceFactory {

	public static List<EPATrace> buildEPATraces(String className, ExecutionTrace executionTrace, EPA epa)
			throws MalformedEPATraceException {

		// Separate method call executions by `callingObjectID`
		Map<Integer, List<String>> methodCallExecutionsByCallingObjectID = new HashMap<>();
		for (MethodCall methodCallExecution : executionTrace.getMethodCalls()) {
			final int callingObjectID = methodCallExecution.callingObjectID;

			if (!methodCallExecution.className.equals(className)) {
				continue; // ignore method calls that do not match target class
			}

			if (methodCallExecutionsByCallingObjectID.get(callingObjectID) == null) {
				methodCallExecutionsByCallingObjectID.put(callingObjectID, new ArrayList<>());
			}

			String methodName = methodCallExecution.methodName;
			methodCallExecutionsByCallingObjectID.get(callingObjectID).add(methodName);
		}

		// Transform each list of calls into a trace
		List<EPATrace> collect = new ArrayList<EPATrace>();
		for (List<String> methodCallExecutions : methodCallExecutionsByCallingObjectID.values()) {
			EPATrace epaTrace = buildEPATrace(methodCallExecutions, epa);
			collect.add(epaTrace);
		}
		return collect;
	}

	public static EPATrace buildEPATrace(List<String> methodCallExecutions, EPA epa)
			throws MalformedEPATraceException {
		List<String> methodCallExecutionsLeft = new ArrayList<>(methodCallExecutions);
		final List<EPATransition> epaTransitions = new ArrayList<>();

		EPAState currentOriginState = epa.getInitialState();
		int firstIdx = 0;
		while (true) {
			// Break conditions: a) End of the method call executions list, or
			// b) No `reportState()` calls remaining
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

			if (destinationStateName.equals("TooManyResourcesException")) { 
				// Execution of reportState() method was interrupted by EvoSuite 
				// We cannot consider the last transition
				break;
			}
			
			final EPAState destinationState = epa.getStateByName(destinationStateName);

			if (destinationState == null) {
				throw new MalformedEPATraceException("State \"" + destinationStateName + "\" does not belong to EPA");
			}

			epaTransitions.add(new EPATransition(currentOriginState, actionName, destinationState));

			currentOriginState = destinationState;
			firstIdx = reportStateIdx + 2;
		}
		return new EPATrace(epaTransitions);
	}

	private static String getStateNameFromReportStateMethodName(final String methodName) {
		return methodName.split("\\(")[0].replaceFirst("reportState", "");
	}
}
