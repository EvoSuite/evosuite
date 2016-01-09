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
		EPATrace epaTrace = EPATraceFactory.buildEPATrace(result.getTrace(), epa);
		boolean isCovered = epaTrace.getEpaTransitions().contains(transition);
		if (isCovered) {
			return 0.0;
		} else {
			return 1.0;
		}
	}
	
	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}

}
