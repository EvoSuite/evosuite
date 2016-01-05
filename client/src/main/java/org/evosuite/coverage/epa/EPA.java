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

	final private Map<EPAState, Set<EPATransition>> map;

	private final String name;

	private final EPAState initialState;
	
	public EPA(String name, Map<EPAState, Set<EPATransition>> map, EPAState initialState) {
		this.name = name;
		this.map = map;
		this.initialState = initialState;
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
