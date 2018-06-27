package org.evosuite.coverage.epa;

import java.util.List;

/**
 * A List of EPA transitions. A trace starts in the initial state of the EPA and
 * traverses the EPA.
 * 
 * @author galeotti
 *
 */
public class EPATrace {
	private final List<EPATransition> epaTransitions;

	public EPATrace(List<EPATransition> epaTransitions) {
		this.epaTransitions = epaTransitions;
	}

	public List<EPATransition> getEpaTransitions() {
		return epaTransitions;
	}

	public EPAState getFirstState() {
		if (this.epaTransitions.isEmpty()) {
			throw new IllegalStateException("Trace is empty!");
		}
		return this.epaTransitions.get(0).getOriginState();
	}

	public EPAState getLastState() {
		if (this.epaTransitions.isEmpty()) {
			throw new IllegalStateException("Trace is empty!");
		}
		return this.epaTransitions.get(this.epaTransitions.size() - 1).getDestinationState();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("EPATrace [");
		for (int i = 0; i < this.epaTransitions.size(); i++) {
			EPATransition epaTransition = epaTransitions.get(i);
			String actionName = epaTransition.getActionName();
			if (i > 0) {
				buff.append(",");
			}
			buff.append(actionName);
		}
		buff.append("]");
		return buff.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((epaTransitions == null) ? 0 : epaTransitions.hashCode());
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
		EPATrace other = (EPATrace) obj;
		if (epaTransitions == null) {
			if (other.epaTransitions != null)
				return false;
		} else if (!epaTransitions.equals(other.epaTransitions))
			return false;
		return true;
	}
}
