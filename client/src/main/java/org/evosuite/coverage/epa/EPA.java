package org.evosuite.coverage.epa;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class EPA {

	static EPA FIRST_EPA = null;

	static {
		try {
			FIRST_EPA = new EPA(new FileInputStream("ListItr-contractor-net.xml"));
		} catch (ParserConfigurationException | IOException | SAXException e) {
			e.printStackTrace();
		}
	}

	final private Map<EPAState, Set<EPATransition>> map;

	private String name;

	private EPAState initialState;
	public EPA(String name, Map<EPAState, Set<EPATransition>> map, EPAState initialState) {
		this.name = name;
		this.map = map;
		this.initialState = initialState;
	}

	public EPA(InputStream xml) throws ParserConfigurationException, IOException, SAXException {
		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		final Document document = documentBuilder.parse(xml);
		final Element abstraction = document.getDocumentElement();
		final String initialStateName = abstraction.getAttribute("initial_state");
		final String epaName = abstraction.getAttribute("name");
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
				if (Objects.equals(stateChild.getNodeName(), "transition")) {
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
		
		// Build map
		final Map<EPAState, Set<EPATransition>> map = new HashMap<>();
		epaStateMap.values().stream()
				.forEach(epaState -> {
					final Set<EPATransition> epaStateTransitions = epaTransitions.stream()
							.filter(epaTransition -> epaTransition.getOriginState() == epaState)
							.collect(Collectors.toSet());
					map.put(epaState, epaStateTransitions);
				});
		this.map = map;
		
		this.initialState = epaStateMap.get(initialStateName);
		this.name = epaName;
	}

	public double getCoverage(List<EPATrace> epaTraces) {
		final Set<EPATransition> epaTransitions = map.values().stream()
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
		final int epaTransitionsSize = epaTransitions.size();
		
		final Set<EPATransition> tracedEpaTransitions = epaTraces.stream()
				.map(EPATrace::getEpaTransitions)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());
		
		epaTransitions.removeAll(tracedEpaTransitions);
		return (double)epaTransitions.size() / epaTransitionsSize; 
	}


	public EPAState getInitialState() {
		return initialState;
	}

	public EPAState getStateByName(String stateName) {
		final Optional<EPAState> epaStateOptional = map.keySet().stream()
				.filter(state -> state.getName().equals(stateName))
				.findFirst();
		return epaStateOptional.orElse(null);
	}

	public String getName() {
		return name;
	}

	public EPAState temp_anyPossibleDestinationState(EPAState originState, String actionName) {
		return map.get(originState).stream()
				.filter(epaTransition -> epaTransition.getActionName().equals(actionName))
				.map(EPATransition::getDestinationState)
				.findFirst()
				.orElse(null);
	}

	public boolean isActionInEPA(String actionName) {
		return map.values().stream()
				.flatMap(Collection::stream)
				.filter(epaTransition -> epaTransition.getActionName().equals(actionName))
				.findAny()
				.isPresent();
	}
}
