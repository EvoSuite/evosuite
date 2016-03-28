package org.evosuite.coverage.epa;

import java.io.IOException;
import java.io.Serializable;

import org.evosuite.testcase.execution.ExecutionResult;

public class EPATransitionCoverageGoal implements Serializable, Comparable<EPATransitionCoverageGoal> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4572794069479938065L;

	private final EPATransition transition;
	private final EPA epa;
	private final String className;

	public EPATransitionCoverageGoal(String className, EPA epa, EPATransition t) {
		this.transition = t;
		this.epa = epa;
		this.className = className;
	}

	public String getMethodName() {
		String actionName = transition.getActionName();
		String methodName;
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
	public int compareTo(EPATransitionCoverageGoal o) {
		String myKey = this.getKey();
		String otherKey = o.getKey();
		return myKey.compareTo(otherKey);
	}

	private String getKey() {
		return String.format("[%s,%s,%s]", transition.getOriginState().getName(), transition.getActionName(),
				transition.getDestinationState().getName());
	}

	/**
	 * Returns 0.0 if the execution trace covers the transition, 1.0 otherwise
	 * 
	 * @param result
	 * @return
	 */
	public double getDistance(ExecutionResult result) {
		final boolean covered = result.getTrace().getEPATraces().stream()
				.filter(epaTrace -> epaTrace.getEpaTransitions().contains(transition)).findAny().isPresent();
		return covered ? 0.0 : 1.0;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((epa == null) ? 0 : epa.hashCode());
		result = prime * result + ((transition == null) ? 0 : transition.hashCode());
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
		EPATransitionCoverageGoal other = (EPATransitionCoverageGoal) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (epa == null) {
			if (other.epa != null)
				return false;
		} else if (!epa.equals(other.epa))
			return false;
		if (transition == null) {
			if (other.transition != null)
				return false;
		} else if (!transition.equals(other.transition))
			return false;
		return true;
	}

	public String toString() {
		return transition.toString();
	}

	public EPATransition getEPATransition() {
		return transition;
	}
}
