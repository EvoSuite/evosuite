package org.evosuite.javaagent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**
 * 
 */

/**
 * @author Gordon Fraser
 * 
 */
public class RegexDistance {

	public final static int INSERT_EDGE = 1;
	public final static int REPLACE_EDGE = 2;
	public final static int DELETE_EDGE = 3;
	public final static int ZERO_EDGE = 4;

	private static class GraphTransition {
		public int cost = 0;
		public int fromRow = 0;
		public State fromState;

		public GraphTransition(int cost, int fromRow, State fromState) {
			this.cost = cost;
			this.fromRow = fromRow;
			this.fromState = fromState;
		}
	}

	private static Map<String, List<State>> regexStateCache = new HashMap<String, List<State>>();

	private static Map<String, Automaton> regexAutomatonCache = new HashMap<String, Automaton>();

	/**
	 * Java regular expressions contain predefined character classes which the
	 * regex parser cannot handle
	 * 
	 * @param regex
	 * @return
	 */
	protected static String expandRegex(String regex) {
		// .	Any character (may or may not match line terminators)
		// \d	A digit: [0-9]
		String newRegex = regex.replaceAll("\\\\d", "[0-9]");

		// \D	A non-digit: [^0-9]
		newRegex = newRegex.replaceAll("\\\\D", "[^0-9]");

		// \s	A whitespace character: [ \t\n\x0B\f\r]
		newRegex = newRegex.replaceAll("\\\\s", "[ \\t\\n\\f\\r]");

		// \S	A non-whitespace character: [^\s]
		newRegex = newRegex.replaceAll("\\\\S", "[^ \\t\\n\\f\\r]");

		// \w	A word character: [a-zA-Z_0-9]
		newRegex = newRegex.replaceAll("\\\\w", "[a-zA-Z_0-9]");

		// \W	A non-word character: [^\w]
		newRegex = newRegex.replaceAll("\\\\W", "[^a-zA-Z_0-9]");

		return newRegex;
	}

	private static void ensureState(
	        Map<Integer, Map<State, Set<GraphTransition>>> transitions, State state,
	        int numRows) {
		for (int row = 0; row <= numRows; row++) {
			if (!transitions.containsKey(row))
				transitions.put(row, new HashMap<State, Set<GraphTransition>>());
			if (!transitions.get(row).containsKey(state))
				transitions.get(row).put(state, new HashSet<GraphTransition>());
		}
	}

	private static void cacheRegex(String regex) {
		String r = expandRegex(regex);
		Automaton automaton = new RegExp(r, RegExp.NONE).toAutomaton();
		automaton.expandSingleton();

		// We convert this to a graph without self-loops in order to determine the topological order
		DirectedGraph<State, DefaultEdge> regexGraph = new DefaultDirectedGraph<State, DefaultEdge>(
		        DefaultEdge.class);
		Set<State> visitedStates = new HashSet<State>();
		Queue<State> states = new LinkedList<State>();
		State initialState = automaton.getInitialState();
		states.add(initialState);

		while (!states.isEmpty()) {
			State currentState = states.poll();
			if (visitedStates.contains(currentState))
				continue;
			if (!regexGraph.containsVertex(currentState))
				regexGraph.addVertex(currentState);
			for (Transition t : currentState.getTransitions()) {
				// Need to get rid of back edges, otherwise there is no topological order!
				if (!t.getDest().equals(currentState)) {
					regexGraph.addVertex(t.getDest());
					regexGraph.addEdge(currentState, t.getDest());
					states.add(t.getDest());
					CycleDetector<State, DefaultEdge> det = new CycleDetector<State, DefaultEdge>(
					        regexGraph);
					if (det.detectCycles()) {
						regexGraph.removeEdge(currentState, t.getDest());
					}
				}
			}
			visitedStates.add(currentState);
		}

		TopologicalOrderIterator<State, DefaultEdge> iterator = new TopologicalOrderIterator<State, DefaultEdge>(
		        regexGraph);
		List<State> topologicalOrder = new ArrayList<State>();
		while (iterator.hasNext()) {
			topologicalOrder.add(iterator.next());
		}

		regexStateCache.put(regex, topologicalOrder);
		regexAutomatonCache.put(regex, automaton);
	}

	public static int getDistance(String arg, String regex) {
		if (!regexAutomatonCache.containsKey(regex)) {
			cacheRegex(regex);
		}
		Automaton automaton = regexAutomatonCache.get(regex);
		int NUM_STATES = automaton.getNumberOfStates();
		int NUM_CHARS = arg.length();

		int[][] graph = new int[NUM_CHARS + 1][NUM_STATES + 2];
		Map<Integer, Map<State, Set<GraphTransition>>> transitions = new HashMap<Integer, Map<State, Set<GraphTransition>>>();

		Map<Integer, State> intToStateMap = new HashMap<Integer, State>();
		Map<State, Integer> stateToIntMap = new HashMap<State, Integer>();
		int numState = 0;

		List<State> topologicalOrder = regexStateCache.get(regex);
		for (State currentState : topologicalOrder) {
			stateToIntMap.put(currentState, numState);
			intToStateMap.put(numState, currentState);

			for (Transition t : currentState.getTransitions()) {
				for (int row = 0; row <= NUM_CHARS; row++) {
					// 1. add a deletion edge with cost 1 from currentState to t.getDest
					ensureState(transitions, t.getDest(), NUM_CHARS);
					transitions.get(row).get(t.getDest()).add(new GraphTransition(1, row,
					                                                  currentState));
				}

				for (int row = 0; row < NUM_CHARS; row++) {
					// 2. add a replacement edge from currentState in row to t.getDest in row+1
					//    if charAt row+1 == the parameter of this transition, this is a zero-cost edge
					ensureState(transitions, t.getDest(), NUM_CHARS);
					if (arg.charAt(row) <= t.getMax() && arg.charAt(row) >= t.getMin()) {
						transitions.get(row + 1).get(t.getDest()).add(new GraphTransition(
						                                                      0, row,
						                                                      currentState));
					} else {
						transitions.get(row + 1).get(t.getDest()).add(new GraphTransition(
						                                                      1, row,
						                                                      currentState));
					}
				}
			}

			ensureState(transitions, currentState, NUM_CHARS);
			for (int row = 0; row < NUM_CHARS; row++) {
				// 3. add an insertion edge from currentState in row to currentState in row+1
				transitions.get(row + 1).get(currentState).add(new GraphTransition(1,
				                                                       row, currentState));
			}
			numState++;
		}

		// Add phi transitions from accepting states to final state
		State finalState = new State();
		ensureState(transitions, finalState, NUM_CHARS);
		for (State s : automaton.getStates()) {
			if (s.isAccept()) {
				transitions.get(NUM_CHARS).get(finalState).add(new GraphTransition(0,
				                                                       NUM_CHARS, s));
			}
		}
		intToStateMap.put(numState, finalState);
		stateToIntMap.put(finalState, numState);

		// First column is costs of removing every single state from regex
		for (int row = 0; row <= NUM_CHARS; row++) {
			graph[row][0] = row;
		}

		// First row is cost of matching empty sequence on regex
		for (int col = 1; col <= NUM_STATES + 1; col++) {
			State state = intToStateMap.get(col - 1);

			int min = Integer.MAX_VALUE;
			System.out.println(transitions.get(0));
			for (GraphTransition t : transitions.get(0).get(state)) {
				int oldCol = stateToIntMap.get(t.fromState) + 1;
				int oldCost = graph[t.fromRow][oldCol];
				if (col == oldCol && t.fromRow == 0)
					continue;

				min = Math.min(min, oldCost + t.cost);
			}
			if (min == Integer.MAX_VALUE) {
				min = 0;
			}
			graph[0][col] = min;
		}

		for (int row = 1; row <= NUM_CHARS; row++) {
			for (int col = 1; col <= NUM_STATES + 1; col++) {
				State state = intToStateMap.get(col - 1);

				int min = Integer.MAX_VALUE;
				for (GraphTransition t : transitions.get(row).get(state)) {
					int oldCol = stateToIntMap.get(t.fromState) + 1;
					int oldCost = graph[t.fromRow][oldCol];
					if (col == oldCol && t.fromRow == row)
						continue;
					min = Math.min(min, oldCost + t.cost);
				}
				if (min == Integer.MAX_VALUE)
					min = 0;

				// minimum of:
				// incoming transitions in automaton
				// incoming transitions from row-1

				graph[row][col] = min;
			}
		}

		return graph[NUM_CHARS][NUM_STATES + 1];
	}

}
