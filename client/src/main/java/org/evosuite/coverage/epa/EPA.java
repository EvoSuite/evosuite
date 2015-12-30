package org.evosuite.coverage.epa;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EPA {

	final private Map<EPAState, Set<EPATransition>> map;

	final static EPA FIRST_EPA;

	public EPA(Map<EPAState, Set<EPATransition>> map) {
		this.map = map;
	}
	
	public EPA(InputStream xml) {
		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		final Document document = documentBuilder.parse(xml);
		final Element abstraction = document.getDocumentElement();
		final NodeList states = abstraction.getElementsByTagName("state");
		
		// Populate a map of names to states
		final Map<String, EPAState> epaStateMap = new HashMap<>();
		for (int i = 0; i < states.getLength(); i++) {
			final Element state = (Element)states.item(i);
			final String name = state.getAttribute("name");
			epaStateMap.put(name, new EPAState(name));
		}
		
		// Populate list of transitions
		final List<EPATransition> epaTransitions = new ArrayList<>();
		for (int i = 0; i < states.getLength(); i++) {
			final Element state = (Element)states.item(i);
			final String stateName = state.getAttribute("name");
			
			final NodeList stateChilds = states.item(i).getChildNodes();
			for (int j = 0; j < stateChilds.getLength(); j++) {
				final Node stateChild = stateChilds.item(j);
				if (stateChild.getNodeName() == "transition") {
					final Element transition = (Element)stateChild;
					final String transitionLabel = transition.getAttribute("label");
					final String transitionDestination = transition.getAttribute("destination");
					epaTransitions.add(
							new EPATransition(
									epaStateMap.get(stateName),
									transitionLabel,
									epaStateMap.get(transitionDestination)
							)
					);
				}
			}
		}
		
		final Map<EPAState, Set<EPATransition>> map = new HashMap<>();
		epaStateMap.values().stream()
				.forEach(epaState -> {
					final Set<EPATransition> epaStateTransitions = epaTransitions.stream()
							.filter(epaTransition -> epaTransition.getOriginState() == epaState)
							.collect(Collectors.toSet());
					map.put(epaState, epaStateTransitions);
				});
		this.map = map;
	}
	

}
