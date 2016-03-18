package org.evosuite.coverage.epa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.testcase.execution.ExecutionTrace;
import org.evosuite.testcase.execution.MethodCall;

@Deprecated
public abstract class EPATraceFactory {

	@Deprecated
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
