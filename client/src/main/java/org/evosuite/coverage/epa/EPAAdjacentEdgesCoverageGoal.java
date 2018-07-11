package org.evosuite.coverage.epa;

import java.io.IOException;
import java.io.Serializable;

import org.evosuite.testcase.execution.ExecutionResult;

/**
 * Represents a goal to be covered by the EEPAAdjacentEdgesCoverage criterion
 */
public class EPAAdjacentEdgesCoverageGoal implements Serializable, Comparable<EPAAdjacentEdgesCoverageGoal> {
	
	private static final long serialVersionUID = 1363867552616666031L;
	
	private final String className;
	private EPATransition firstTransition;
	private EPATransition secondTransition;

	public EPAAdjacentEdgesCoverageGoal(String className, EPATransition firstTransition, EPATransition secondTransition) {
		this.className = className;
		this.firstTransition = firstTransition;
		this.secondTransition = secondTransition;
	}
	
	public String getMethodName() {
		String actionName = this.firstTransition.getActionName();
		String methodName;
		if (actionName.contains("(")) {
			methodName = actionName.split("(")[0] + " -- ";
		} else {
			methodName = actionName + " -- ";
		}
		actionName = this.secondTransition.getActionName();
		if (actionName.contains("(")) {
			methodName = actionName.split("(")[0];
		} else {
			methodName = actionName;
		}
		return methodName;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public int compareTo(EPAAdjacentEdgesCoverageGoal o) {
		String myKey = this.getGoalName();
		String otherKey = o.getGoalName();
		return myKey.compareTo(otherKey);
	}
	
	public String getGoalName()
	{
		return String.format("{AdjacentEdge[%s],[%s]}", firstTransition.toString(), secondTransition.toString());
	}

	/**
	 * Returns 0.0 if the execution trace covers the adjacent edges, 1.0 otherwise If
	 * the execution trace has a INVALID_OBJECT_STATE, the rest of the trace is
	 * discarded.
	 * 
	 * @param result
	 * @return
	 */
	public double getDistance(ExecutionResult result) {
		for (EPATrace epa_trace : result.getTrace().getEPATraces())
			for (int i = 0; i < epa_trace.getEpaTransitions().size()-1; i++) {
				EPATransition firstEpaTransition = epa_trace.getEpaTransitions().get(i);
				EPATransition secondEpaTransition = epa_trace.getEpaTransitions().get(i + 1);
				if (secondEpaTransition.getDestinationState().equals(EPAState.INVALID_OBJECT_STATE)) {
					// discard the rest of the trace if an invalid object state is reached
					break;
				}
				
				if (isCoveredBy(firstEpaTransition, secondEpaTransition))
				{
					return 0.0;
				}
			}
		return 1.0;
	}
	
	public boolean isCoveredBy(EPATransition firstEpaTransition, EPATransition secondEpaTransition)
	{
		if(!this.secondTransition.getClass().equals(secondEpaTransition.getClass()))
				return false;
		return this.firstTransition.equals(firstEpaTransition)
				&& secondEpaTransition.getOriginState().equals(this.secondTransition.getOriginState())
					&& secondEpaTransition.getActionName().equals(this.secondTransition.getActionName())
					&& secondEpaTransition.getDestinationState().equals(this.secondTransition.getDestinationState());
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}

	public String toString() {
		return getGoalName().toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof EPAAdjacentEdgesCoverageGoal) {
			EPAAdjacentEdgesCoverageGoal other = (EPAAdjacentEdgesCoverageGoal) obj;
			return this.getGoalName().equals(other.getGoalName());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getGoalName().hashCode();
	}

}