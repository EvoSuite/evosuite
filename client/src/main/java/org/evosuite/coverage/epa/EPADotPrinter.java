package org.evosuite.coverage.epa;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class EPADotPrinter {

	private static class KeyPair implements Comparable<Object> {
		private final String fromState;
		private final String toState;

		public KeyPair(String fromState, String toState) {
			this.fromState = fromState;
			this.toState = toState;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fromState == null) ? 0 : fromState.hashCode());
			result = prime * result + ((toState == null) ? 0 : toState.hashCode());
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
			KeyPair other = (KeyPair) obj;
			if (fromState == null) {
				if (other.fromState != null)
					return false;
			} else if (!fromState.equals(other.fromState))
				return false;
			if (toState == null) {
				if (other.toState != null)
					return false;
			} else if (!toState.equals(other.toState))
				return false;
			return true;
		}

		@Override
		public int compareTo(Object arg0) {
			if (arg0 == null) {
				throw new NullPointerException();
			}
			if (!(arg0 instanceof KeyPair)) {
				throw new ClassCastException("cannot compare KeyPair to " + arg0.getClass().getName());
			}
			KeyPair other = (KeyPair) arg0;
			if (this.fromState.equals(other.fromState)) {
				return this.toString().compareTo(other.toState);
			} else {
				return this.fromState.compareTo(other.fromState);
			}

		}
	}

	private List<String> allowedActions = Arrays.<String>asList("moveToInsertRow", "moveToCurrentRow", "absolute",
			"first", "last", "next", "previous", "relative", "afterLast", "beforeFirst", "insertRow", "close",
			"deleteRow", "cancelRowUpdates", "refreshRow", "updateRow", "updateInt", "getInt", "wasNull");

	public String toDot(EPA automata) {

		Map<KeyPair, Set<String>> edges = new HashMap<KeyPair, Set<String>>();
		Map<String, Set<String>> enabledActions = new HashMap<String, Set<String>>();
		for (EPATransition transition : automata.getTransitions()) {
			String fromState = transition.getOriginState().getName();
			String toState = transition.getDestinationState().getName();
			String actionId = transition.getActionName();

			if (!allowedActions.contains(actionId)) {
				continue;
			}

			KeyPair key = new KeyPair(fromState, toState);
			if (!edges.containsKey(key)) {
				edges.put(key, new HashSet<String>());
			}
			if (!enabledActions.containsKey(fromState)) {
				enabledActions.put(fromState, new HashSet<String>());
			}
			edges.get(key).add(actionId);
			enabledActions.get(fromState).add(actionId);
		}

		List<KeyPair> listOfKeys = new LinkedList<KeyPair>(edges.keySet());
		Collections.sort(listOfKeys);

		StringBuffer buff = new StringBuffer();
		String automataName = automata.getName();
		buff.append(String.format("digraph epa {", automataName));
		buff.append("fontsize=22;");
		buff.append("\n");
		buff.append("labelloc=top;");
		buff.append("\n");
		buff.append("labeljust=center;");
		buff.append("\n");

		for (String stateId : enabledActions.keySet()) {
			List<String> enabledActionsList = new LinkedList<String>(enabledActions.get(stateId));
			Collections.sort(enabledActionsList);
			String enabledActionsStr = String.join("\\n", enabledActionsList);
			buff.append(String.format("%s[label=\"%s\"]", stateId, stateId + ":\\n" + enabledActionsStr));
			buff.append("\n");
		}

		for (KeyPair keyPair : listOfKeys) {
			String fromStateId = keyPair.fromState;
			String toStateId = keyPair.toState;
			List<String> actionsInEdge = new LinkedList<String>(edges.get(keyPair));
			Collections.sort(actionsInEdge);
			String actions = String.join("\\n", actionsInEdge);

			buff.append(String.format(" %s -> %s [label=\"%s\"];", fromStateId, toStateId, actions));
			buff.append("\n");
		}

		buff.append("}");
		buff.append("\n");
		return buff.toString();
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: <XML EPA file>");
			return;
		}
		String xmlFilename = args[0];
		EPADotPrinter printer = new EPADotPrinter();
		try {
			String dot = printer.toDot(xmlFilename);
			System.out.println(dot);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.out.println("An exception has occured!");
			e.printStackTrace();
		}
	}

	public String toDot(String xmlFilename) throws ParserConfigurationException, SAXException, IOException {
		EPA automata = EPAFactory.buildEPA(xmlFilename);
		EPADotPrinter printer = new EPADotPrinter();
		String dot = printer.toDot(automata);
		return dot;
	}

}
