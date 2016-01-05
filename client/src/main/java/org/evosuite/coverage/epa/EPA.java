package org.evosuite.coverage.epa;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class EPA {

	final private Map<EPAState, Set<EPATransition>> map;

	private final String name;

	private final EPAState initialState;
	
	private final int stateCount;

	private final int transitionCount;

	public EPA(String name, Map<EPAState, Set<EPATransition>> map, EPAState initialState) {
		this.name = name;
		this.map = map;
		this.initialState = initialState;
		this.stateCount = calculateStateCount();
		this.transitionCount = calculateTransitionCount();
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

	public int getNumberOfStates() {
		return stateCount;
	}

	public int getNumberOfTransitions() {
		return transitionCount;
	}
	
	private int calculateStateCount() {
		Set<EPAState> states = new HashSet<EPAState>();
		states.add(this.initialState);
		for (EPAState transitionSource : this.map.keySet()) {
			states.add(transitionSource);
			Set<EPATransition> destinations = this.map.get(transitionSource);
			for (EPATransition epaTransition : destinations) {
				EPAState transitionDestination = epaTransition.getDestinationState();
				states.add(transitionDestination);
			}
		}
		return states.size();
	}

	private int calculateTransitionCount() {
		int transitions =  0;
		for (EPAState transitionSource : this.map.keySet()) {
			Set<EPATransition> destinations = this.map.get(transitionSource);
			for (EPATransition epaTransition : destinations) {
				transitions++;
			}
		}
		return transitions;
	}

	
}
