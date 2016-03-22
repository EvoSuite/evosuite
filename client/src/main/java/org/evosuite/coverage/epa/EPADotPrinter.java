package org.evosuite.coverage.epa;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class EPADotPrinter {

	public String toDot(EPA automata) {
		StringBuffer buff = new StringBuffer();
		buff.append("digraph epa {");
		buff.append("\n");
		List<String> entries = new LinkedList<String>();
		for (EPATransition transition : automata.getTransitions()) {
			String fromState = transition.getOriginState().getName();
			String toState = transition.getDestinationState().getName();
			String actionName = transition.getActionName();
			String edge_str = String.format("  %s -> %s [label=\"%s\"];", fromState, toState, actionName);
			entries.add(edge_str);
		}
		Collections.sort(entries);
		for (String entry : entries) {
			buff.append(entry);
			buff.append("\n");
		}
		
		buff.append("}");
		buff.append("\n");
		return buff.toString();
	}
}
