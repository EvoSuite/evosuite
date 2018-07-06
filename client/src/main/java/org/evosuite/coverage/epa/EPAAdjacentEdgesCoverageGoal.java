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
	private final AdjacentEdgesPair adjacentEdgesPair; 
	
	public class AdjacentEdgesPair implements Serializable
	{
		private static final long serialVersionUID = 993646300419193667L;
		private final EPAState fromState;
		private final String actionId;
		private final EPAState toState;
		private final String actionId_2;
		private final EPAState toState_2;
		
		public AdjacentEdgesPair(EPAState fromState, String actionId, EPAState toState, String actionId_2, EPAState toState_2)
		{
			this.fromState = fromState;
			this.actionId = actionId;
			this.toState = toState;
			this.actionId_2 = actionId_2;
			this.toState_2 = toState_2;
		}
		
		public boolean isCoveredBy(EPATransition epa_transition_curr, EPATransition epa_trasition_next)
		{
			return epa_transition_curr.getOriginState().equals(this.fromState)
						&& epa_transition_curr.getActionName().equals(this.actionId)
						&& epa_transition_curr.getDestinationState().equals(this.toState)
						&& epa_trasition_next.getOriginState().equals(this.toState)
						&& epa_trasition_next.getActionName().equals(this.actionId_2)
						&& epa_trasition_next.getDestinationState().equals(this.toState_2);
		}
		
		public String getName()
		{
			return String.format("{AdjacentEdge[%s,%s,%s],[%s,%s,%s]}", fromState, actionId, toState, toState, actionId_2, toState_2);
		}
	}

	public EPAAdjacentEdgesCoverageGoal(String className, EPA epa, EPAState fromState, String actionId, EPAState toState, String actionId_2, EPAState toState_2) {
		this.className = className;
		this.adjacentEdgesPair = new AdjacentEdgesPair(fromState, actionId, toState, actionId_2, toState_2);
	}
	
	public String getMethodName() {
		String methodName;
		if (adjacentEdgesPair.actionId.contains("(")) {
			methodName = adjacentEdgesPair.actionId.split("(")[0] + "--";
		} else {
			methodName = adjacentEdgesPair.actionId + "--";
		}
		if (adjacentEdgesPair.actionId_2.contains("(")) {
			methodName = methodName + adjacentEdgesPair.actionId_2.split("(")[0];
		} else {
			methodName = methodName + adjacentEdgesPair.actionId_2;
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

	public String getGoalName() {
		return this.adjacentEdgesPair.getName();
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
				EPATransition epa_transition_curr = epa_trace.getEpaTransitions().get(i);
				EPATransition epa_transition_next = epa_trace.getEpaTransitions().get(i+1);
				if (this.adjacentEdgesPair.isCoveredBy(epa_transition_curr, epa_transition_next))
				{
					return 0.0;
				}
				if (epa_transition_curr.getDestinationState().equals(EPAState.INVALID_OBJECT_STATE)) {
					// discard the rest of the trace if an invalid object state is reached
					break;
				}
			}
		return 1.0;
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