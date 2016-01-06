package org.evosuite.coverage.epa;

import java.util.List;

/**
 * A List of EPA transitions. 
 * A trace starts in the initial state of the EPA and traverses the EPA.
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

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("EPATrace [");
		for (int i=0; i<this.epaTransitions.size();i++) {
			EPATransition epaTransition = epaTransitions.get(i);
			String actionName = epaTransition.getActionName();
			if (i>0) {
				buff.append(",");
			}
			buff.append(actionName);
		}
		buff.append("]");
		return buff.toString();
	}
}
