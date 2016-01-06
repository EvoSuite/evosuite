package org.evosuite.coverage.epa;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 
 * @author galeotti
 *
 */
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
		this.stateCount = calculateStateCount(map, initialState);
		this.transitionCount = calculateTransitionCount(map);
	}

	public EPAState getInitialState() {
		return initialState;
	}

	public EPAState getStateByName(String stateName) {
		final Optional<EPAState> epaStateOptional = map.keySet().stream()
				.filter(state -> state.getName().equals(stateName)).findFirst();
		return epaStateOptional.orElse(null);
	}

	public String getName() {
		return name;
	}

	public EPAState temp_anyPossibleDestinationState(EPAState originState, String actionName) {
		return map.get(originState).stream().filter(epaTransition -> epaTransition.getActionName().equals(actionName))
				.map(EPATransition::getDestinationState).findFirst().orElse(null);
	}

	public boolean isActionInEPA(String actionName) {
		return map.values().stream().flatMap(Collection::stream)
				.filter(epaTransition -> epaTransition.getActionName().equals(actionName)).findAny().isPresent();
	}

	public int getNumberOfStates() {
		return stateCount;
	}

	public int getNumberOfTransitions() {
		return transitionCount;
	}

	private static int calculateStateCount(Map<EPAState, Set<EPATransition>> map, EPAState initialState) {
		Set<EPAState> states = new HashSet<EPAState>();
		states.add(initialState);
		states.addAll(map.keySet());
		Set<EPAState> destination_states = map.values().stream().flatMap(Set::stream).map(t -> t.getDestinationState())
				.collect(Collectors.toSet());
		states.addAll(destination_states);
		return states.size();
	}

	private static int calculateTransitionCount(Map<EPAState, Set<EPATransition>> map) {
		final Set<EPATransition> epaTransitions = map.values().stream().flatMap(Collection::stream)
				.collect(Collectors.toSet());
		final int epaTransitionsSize = epaTransitions.size();
		return epaTransitionsSize;
	}

}
