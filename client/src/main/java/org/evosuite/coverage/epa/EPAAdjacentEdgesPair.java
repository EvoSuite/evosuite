package org.evosuite.coverage.epa;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.testcase.execution.ExecutionResult;

public class EPAAdjacentEdgesPair implements Serializable {
	private static final long serialVersionUID = 3447220243095960970L;
	private EPATransition firstTransition;
	private EPATransition secondTransition;

	public EPAAdjacentEdgesPair(EPATransition firstTransition, EPATransition secondTransition) {
		this.firstTransition = firstTransition;
		this.secondTransition = secondTransition;
	}

	public EPATransition getFirstEpaTransition() {
		return this.firstTransition;
	}

	public EPATransition getSecondEpaTransition() {
		return this.secondTransition;
	}

	@Override
	public java.lang.String toString() {
		String first = firstTransition.getClass().getSimpleName() + "{" + firstTransition.getOriginState() + "," +
				firstTransition.getActionName() + "," + firstTransition.getDestinationState() + "}";
		String second = secondTransition.getClass().getSimpleName() + "{" + secondTransition.getOriginState() + "," +
				secondTransition.getActionName() + "," + secondTransition.getDestinationState() + "}";
		return String.format("{AdjacentEdgesPair[%s],[%s]}", first, second);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstTransition == null) ? 0 : firstTransition.hashCode());
		result = prime * result + ((secondTransition == null) ? 0
				: secondTransition.getOriginState().hashCode() + secondTransition.getActionName().hashCode()
						+ secondTransition.getDestinationState().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EPAAdjacentEdgesPair other = (EPAAdjacentEdgesPair) obj;
		if (firstTransition == null) {
			if (other.firstTransition != null)
				return false;
		} else if (!firstTransition.equals(other.firstTransition))
			return false;
		else if (!firstTransition.getClass().equals(other.firstTransition.getClass()))
			return false;
		if (secondTransition == null) {
			if (other.secondTransition != null)
				return false;
		} else if (!secondTransition.getClass().equals(other.secondTransition.getClass()))
			return false;
		else if (!secondTransition.getOriginState().equals(other.secondTransition.getOriginState()))
			return false;
		else if (!secondTransition.getActionName().equals(other.secondTransition.getActionName()))
			return false;
		else if (!secondTransition.getDestinationState().equals(other.secondTransition.getDestinationState()))
			return false;
		return true;
	}

	public static Set<EPAAdjacentEdgesPair> getAdjacentEdgesPairsExecuted(List<ExecutionResult> executionResults) {
		Set<EPAAdjacentEdgesPair> pairs = new HashSet<>();
		for (ExecutionResult executionResult : executionResults) {
			Set<EPAAdjacentEdgesPair> pairsOfExecutionResult = getAdjacentEdgesPairsExecuted(executionResult);
			pairs.addAll(pairsOfExecutionResult);
		}

		return pairs;
	}

	/**
	 * Returns a set of adjacent pairs for the execution result
	 * 
	 * @param executionResult
	 * @return
	 */
	public static Set<EPAAdjacentEdgesPair> getAdjacentEdgesPairsExecuted(ExecutionResult executionResult) {
		Set<EPAAdjacentEdgesPair> pairsOfExecutionResult = new HashSet<>();
		for (EPATrace epaTrace : executionResult.getTrace().getEPATraces()) {
			for (int i = 0; i < epaTrace.getEpaTransitions().size() - 1; i++) {
				EPATransition firstEpaTransition = epaTrace.getEpaTransitions().get(i);
				EPATransition secondEpaTransition = epaTrace.getEpaTransitions().get(i + 1);
				if (firstEpaTransition.getDestinationState().equals(EPAState.INVALID_OBJECT_STATE)
						|| secondEpaTransition.getDestinationState().equals(EPAState.INVALID_OBJECT_STATE)) {
					// discard the rest of the trace if an invalid object state is reached
					break;
				}
				pairsOfExecutionResult.add(new EPAAdjacentEdgesPair(firstEpaTransition, secondEpaTransition));
			}
		}
		return pairsOfExecutionResult;
	}

}
