package org.evosuite.coverage.epa;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is a Enabledness Preservation Automata (EPA).
 * 
 * @author galeotti
 *
 */
public class EPA implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7496754070047070624L;

	final private Map<EPAState, Set<EPATransition>> map;

	private final String name;

	private final EPAState initialState;
	
	public EPA(String name, Map<EPAState, Set<EPATransition>> map, EPAState initialState) {
		this.name = name;
		this.map = map;
		this.initialState = initialState;
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

	public boolean containsAction(String actionName) {
		return map.values().stream().flatMap(Collection::stream)
				.filter(epaTransition -> epaTransition.getActionName().equals(actionName)).findAny().isPresent();
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}

	public Set<String> getActions() {
		final Set<String> actions = map.values().stream().flatMap(Set::stream)
				.map(t -> t.getActionName()).collect(Collectors.toSet());
		return actions;
	}
	
	public Set<EPAState> getStates() {
		final Set<EPAState> states = new HashSet<EPAState>();
		states.add(initialState);
		states.addAll(map.keySet());
		final Set<EPAState> destination_states = map.values().stream().flatMap(Set::stream)
				.map(t -> t.getDestinationState()).collect(Collectors.toSet());
		states.addAll(destination_states);
		return states;
	}

	public Set<EPATransition> getTransitions() {
		final Set<EPATransition> epaTransitions = map.values().stream().flatMap(Collection::stream)
				.collect(Collectors.toSet());
		return epaTransitions;
	}

	public Set<EPATransition> getTransitions(EPAState originState) {
		return getTransitions().stream().filter(t -> t.getOriginState().equals(originState))
				.collect(Collectors.toSet());
	}

}
