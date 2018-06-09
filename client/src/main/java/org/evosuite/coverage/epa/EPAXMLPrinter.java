package org.evosuite.coverage.epa;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class EPAXMLPrinter {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: <XML EPA file>");
			return;
		}
		String xmlFilename = args[0];
		EPAXMLPrinter printer = new EPAXMLPrinter();
		try {
			String xml = printer.toXML(xmlFilename);
			System.out.println(xml);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.out.println("An exception has occured!");
			e.printStackTrace();
		}
	}

	public String toXML(String xmlFilename) throws ParserConfigurationException, SAXException, IOException {
		EPA automata = EPAFactory.buildEPA(xmlFilename);
		EPAXMLPrinter printer = new EPAXMLPrinter();
		String dot = printer.toXML(automata);
		return dot;
	}

	public String toXML(EPA automata) {
		StringBuffer buff = new StringBuffer();
		buff.append("<?xml version=\"1.0\" ?>");
		buff.append("\n");
		String initialStateId = automata.getInitialState().getName();
		String automataName = automata.getName();
		buff.append(String.format("<abstraction initial_state=\"%s\" name=\"%s\">", initialStateId, automataName));
		buff.append("\n");

		SortedSet<String> sorted_action_ids = new TreeSet<String>(automata.getActions());
		for (String actionId : sorted_action_ids) {
			buff.append(String.format("  <label name=\"%s\" />", actionId));
			buff.append("\n");
		}
		SortedSet<String> sorted_state_ids = new TreeSet<String>();
		for (EPAState state : automata.getStates()) {
			String stateId = state.getName();
			sorted_state_ids.add(stateId);
		}
		for (String stateId : sorted_state_ids) {
			buff.append(String.format("  <state name=\"%s\">", stateId));
			buff.append("\n");

			SortedSet<String> sorted_transition_elements = new TreeSet<String>();
			SortedSet<String> sorted_enabled_action_elements = new TreeSet<String>();
			EPAState state = automata.getStateByName(stateId);
			for (EPATransition transition : automata.getTransitions(state)) {
				String destionationStateId = transition.getDestinationState().getName();
				String actionId = transition.getActionName();
				String enabled_action_element = String.format("<enabled_label name=\"%s\" />", actionId);
				sorted_enabled_action_elements.add(enabled_action_element);

				String transition_element = String.format("<transition destination=\"%s\" label=\"%s\" />",
						destionationStateId, actionId);
				sorted_transition_elements.add(transition_element);
			}

			for (String enabled_action_element : sorted_enabled_action_elements) {
				buff.append("    " + enabled_action_element);
				buff.append("\n");
			}

			for (String transition_element : sorted_transition_elements) {
				buff.append("    " + transition_element);
				buff.append("\n");

			}

			buff.append("  </state>");
			buff.append("\n");
		}
		buff.append("</abstraction>");
		buff.append("\n");
		return buff.toString();
	}

}
