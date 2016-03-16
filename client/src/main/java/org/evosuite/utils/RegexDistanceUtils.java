/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

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
 *  Class used to define the distance between a string and a regex
 */
public class RegexDistanceUtils {

	/*
	 * Automatons for regex can be expensive to build. So we cache them,
	 * as we might need to access to them several times during the search
	 */
	private static Map<String, List<State>> regexStateCache = new HashMap<String, List<State>>();
	private static Map<String, Automaton> regexAutomatonCache = new HashMap<String, Automaton>();

	public static Automaton getRegexAutomaton(String regex) {
		if (!regexAutomatonCache.containsKey(regex)) {
			cacheRegex(regex);
		}
		return regexAutomatonCache.get(regex);
	}

	public static String getRegexInstance(String regex) {
		if (!regexAutomatonCache.containsKey(regex)) {
			cacheRegex(regex);
		}
		Automaton automaton = regexAutomatonCache.get(regex);
		return automaton.getShortestExample(true);
	}

	public static String getNonMatchingRegexInstance(String regex) {
		if (!regexAutomatonCache.containsKey(regex)) {
			cacheRegex(regex);
		}
		Automaton automaton = regexAutomatonCache.get(regex);
		return automaton.getShortestExample(false);
	}

	private static class GraphTransition {
				
		public enum TransitionType{INSERTION, DELETION, REPLACEMENT, 
			/**
			 * A phantom transition is an artificial transition from the sink/final states to a single artificial sink/state.
			 * This is used to simplify the recursion calculation of the subpath costs. 
			 */
			PHANTOM};
		
		public final double cost;
		public final int fromRow;
		public final State fromState;
		public final TransitionType type;
		
		public GraphTransition(double cost, int fromRow, State fromState, TransitionType type) {
			this.cost = cost;
			this.fromRow = fromRow;
			this.fromState = fromState;
			this.type = type;
		}
	}

	/**
	 * Normalize x in [0,1]
	 * 
	 * @param x
	 * @return
	 */
	private static double normalize(double x) {
		return x / (x + 1.0);
	}

	/**
	 * Java regular expressions contain predefined character classes which the
	 * regex parser cannot handle
	 * 
	 * @param regex
	 * @return
	 */
	public static String expandRegex(String regex) {
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

		if(newRegex.startsWith("^"))
			newRegex = newRegex.substring(1);
		
		if(newRegex.endsWith("$"))
			newRegex = newRegex.substring(0, newRegex.length() - 1);
		
		// TODO: Some of these should be handled, not just ignored!
		newRegex = removeFlagExpressions(newRegex);
		
		newRegex = removeReluctantOperators(newRegex);
		
		return newRegex;
	}
	
	protected static String removeFlagExpressions(String regex) {
		// Case insensitive
		regex = regex.replaceAll("\\(\\?i\\)", "");

		// Unix lines mode
		regex = regex.replaceAll("\\(\\?d\\)", "");

		// Permit comments and whitespace in pattern
		regex = regex.replaceAll("\\(\\?x\\)", "");

		// Multiline mode
		regex = regex.replaceAll("\\(\\?m\\)", "");

		// Dotall
		regex = regex.replaceAll("\\(\\?s\\)", "");

		// Unicode case
		regex = regex.replaceAll("\\(\\?u\\)", "");

		return regex;
	}
	
	protected static String removeReluctantOperators(String regex) {
		regex = regex.replaceAll("\\+\\?", "\\+");
		regex = regex.replaceAll("\\*\\?", "\\*");
		regex = regex.replaceAll("\\?\\?", "\\?");
		
		return regex;
	}

	/**
	 * Ensure that each row has the full data structures containing the target state
	 * 
	 * @param transitions 
	 * @param state
	 * @param numRows
	 */
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

	/**
	 * <p>
	 * Get the distance between the arg and the given regex.
	 * All operations (insertion/deletion/replacement) cost 1.
	 * There is no assumption on where and how the operations
	 * can be done (ie all sequences are valid). 
	 * </p>
	 */
	public static int getStandardDistance(String arg, String regex) {
		if(!isSupportedRegex(regex)) {
			return getDefaultDistance(arg, regex);
		}

		RegexGraph graph = new RegexGraph(arg, regex);		
		CostMatrix matrix = new CostMatrix();
		return matrix.calculateStandardCost(graph);		
	}
	
	private static int getDefaultDistance(String arg, String regex) {
        Pattern p = Pattern.compile(regex);
        if (p.matcher(arg).matches())
        	return 0;
        else
        	return 1;

	}
	
	/**
	 * Determine whether the regex requires features that are 
	 * not supported by the regex automaton library
	 *  
	 * @param regex
	 * @return
	 */
	private static boolean isSupportedRegex(String regex) {
		if(regex.contains("\\b"))
			return false;
		
		return true;
	}

	/**
	 * <p>Get the distance between the arg and the given regex.
	 * Insertion/deletion cost 1, whereas replacement is in [0,1] depending
	 * on the actual character values. </p>
	 * 
	 * <p> Note: the distance is tailored for the <b>StringAVM<b/> algorithm,
	 * in which characters are only inserted/appended at the end.</p>
	 * 
	 * @param arg
	 * @param regex
	 * @return
	 */
	public static double getDistanceTailoredForStringAVM(String arg, String regex) {
		RegexGraph graph = new RegexGraph(arg, regex);		
		CostMatrix matrix = new CostMatrix();
		return matrix.calculateCostForStringAVM(graph);		
	}
	
	protected static Automaton getAndCacheAutomaton(String regex){
		/*
		 * Cache it if first time we build it
		 */
		if (!regexAutomatonCache.containsKey(regex)) {
			/*
			 * Create an automaton representing the regex
			 */
			cacheRegex(regex);
		}
		
		Automaton automaton = regexAutomatonCache.get(regex);
		return automaton;
	}

	
	/**
	 * A graph created based on an "arg" that is matched against a "regex".
	 * There is going to be arg.length+1 copies of the regex automaton. 
	 * Each copy represents a "row".
	 * Each automaton state, in topological order, represents a "column".
	 * The graph can be considered as a "rows"x"columns" matrix.
	 * 
	 * @author arcuri
	 *
	 */
	private static class RegexGraph {

		private  Map<Integer, Map<State, Set<GraphTransition>>> transitions;
		private  Map<Integer, State> intToStateMap;
		private  Map<State, Integer> stateToIntMap;
		
		/**
		 * Build the graph
		 * @param arg
		 * @param regex
		 */
		public RegexGraph(String arg, String regex){
			transitions = createGraph(arg,regex);
		}
		
		public int getNumberOfRows(){
			return transitions.keySet().size();
		}
		
		public int getNumberOfColumns(){
			return stateToIntMap.size();
		}
		
		/**
		 * Get all the incoming transitions to the node located at coordinate "row" and "column"
		 * @param row
		 * @param column
		 * @return
		 */
		public Set<GraphTransition> getIncomingTransitions(int row, int column){
			State state = intToStateMap.get(column);
			return transitions.get(row).get(state);
		}
		
		public int getColumn(State state){
			return stateToIntMap.get(state);
		}
		
		private  Map<Integer, Map<State, Set<GraphTransition>>> createGraph(String arg, String regex){

			/*
			 * Create a graph to calculate the distance. The algorithm is based on what discussed in:
			 * 
			 * Mohammad Alshraideh and Leonardo Bottaci
			 * Search-based software test data generation for string data using program-specific search operators
			 * http://neo.lcc.uma.es/mase/attachments/085_TestDataGenerationForStringData.pdf
			 * 
			 * and 
			 * 
			 * EUGENE W. MYERS and WEBB MILLER
			 * APPROXIMATE MATCHING OF REGULAR EXPRESSIONS
			 * http://www.cs.mun.ca/~harold/Courses/Old/Ling6800.W06/Diary/reg.aprox.pdf
			 */

			Automaton automaton = getAndCacheAutomaton(regex);
			final int NUM_CHARS = arg.length();


			List<State> topologicalOrder = regexStateCache.get(regex);

			Map<Integer, Map<State, Set<GraphTransition>>> transitions = new HashMap<Integer, Map<State, Set<GraphTransition>>>();

			intToStateMap = new HashMap<Integer, State>();
			stateToIntMap = new HashMap<State, Integer>();
			int numState = 0;

			for (State currentState : topologicalOrder) {

				/*
				 * Init data structure to quickly map/access state/index
				 */
				stateToIntMap.put(currentState, numState);
				intToStateMap.put(numState, currentState);
				numState++;

				for (Transition t : currentState.getTransitions()) {

					State destination = t.getDest();
					ensureState(transitions, destination , NUM_CHARS);

					for (int row = 0; row <= NUM_CHARS; row++) {
						/*
						 *  add an insertion edge from currentState in row to target state in same row
						 */

						transitions.get(row).get(destination).add(new GraphTransition(1.0, row, currentState, GraphTransition.TransitionType.INSERTION));
					}

					for (int row = 0; row < NUM_CHARS; row++) {
						/*
						 *  Add a replacement edge from currentState in row to t.getDest in row+1
						 *  if charAt row+1 == the parameter of this transition, this is a zero-cost edge
						 */

						double cost = 0.0;

						if (arg.charAt(row) < t.getMin() || arg.charAt(row) > t.getMax()) {					
							int distMin = Math.abs(arg.charAt(row) - t.getMin());
							int distMax = Math.abs(arg.charAt(row) - t.getMax());
							cost = normalize(Math.min(distMin, distMax));
						}

						/*
						 * Important: even if the cost is 0 (eg match on the arg/regex in which we replace char X with X), we CANNOT
						 * use a PHANTOM transition. Even if we do not replace anything, we still need to consider it as a replacement 
						 * transition. Consider the case
						 * 
						 *  "ac".matches("abc")
						 *  
						 *  If we used a phantom transition to represent the alignment c/c, then it would be possible to insert 'b' in the 
						 *  middle of "abc". On the other hand, if we use a replacement c/c, then inserting 'b' would not be allowed, as an
						 *  insertion cannot be followed by a replacement.  
						 */

						transitions.get(row + 1).get(destination).add(new GraphTransition(cost, row, currentState, GraphTransition.TransitionType.REPLACEMENT));
					}
				}

				ensureState(transitions, currentState, NUM_CHARS);

				for (int row = 0; row < NUM_CHARS; row++) {

					/*
					 * add a deletion edge with cost 1 from currentState to currentState in next row
					 */

					transitions.get(row + 1).get(currentState).add(new GraphTransition(1.0, row, currentState,  GraphTransition.TransitionType.DELETION));
				}			
			}

			// Add zero-cost transitions from accepting states to final state
			State finalState = new State();
			ensureState(transitions, finalState, NUM_CHARS);
			for (State s : automaton.getStates()) {
				if (s.isAccept()) {
					transitions.get(NUM_CHARS).get(finalState).add(new GraphTransition(0, NUM_CHARS, s, GraphTransition.TransitionType.PHANTOM));
				}
			}
			intToStateMap.put(numState, finalState); 
			stateToIntMap.put(finalState, numState);	

			return transitions;
		}
	}

	/**
	 * Class used to calculate the cost, ie the actual distance, based on a RegexGraph.	
	 * 
	 * @author arcuri
	 */
	private static class CostMatrix{
		
		private final int DEL = 0;
		private final int REP = 1;
		private final int INS = 2;
		
		public CostMatrix() {
			super();			
		}

		public int calculateStandardCost(RegexGraph graph){
			final int ROWS = graph.getNumberOfRows();
			final int COLUMNS = graph.getNumberOfColumns();
			
			final double[][] matrix = new double[ROWS][COLUMNS]; 
			
			// First row is cost of matching empty sequence on regex
			final int FIRST_ROW = 0;
			
			/*
			 * init first starting state with 0 costs
			 */
			matrix[FIRST_ROW][0] = 0;
			
			//look at first row (which is special)
			for (int col = 1; col < graph.getNumberOfColumns(); col++) {

				double min = Double.MAX_VALUE;
				
				for (GraphTransition t :  graph.getIncomingTransitions(FIRST_ROW, col)) {

					int otherCol = graph.getColumn(t.fromState);

					//self transition
					if (col == otherCol){
						continue;
					}
					
					double otherCost = matrix[FIRST_ROW][otherCol];

					min = Math.min(min, getSubPathCost(otherCost, Math.ceil(t.cost)));
				}
				
				matrix[FIRST_ROW][col] = min;
			}
		
			//then look at the other rows
			for(int i=1; i<ROWS; i++){
				
				for (int col = 0; col < COLUMNS; col++) {
					
					matrix[i][col] = Double.MAX_VALUE;
					
					for (GraphTransition t : graph.getIncomingTransitions(i, col)) {
						
						int otherCol = graph.getColumn(t.fromState);
						int otherRow = t.fromRow;
						
						if(! t.type.equals(GraphTransition.TransitionType.PHANTOM)){														
							matrix[i][col] = Math.min(matrix[i][col], getSubPathCost(matrix[otherRow][otherCol],Math.ceil(t.cost)));							
						} else {
							/*
							 * artificial transition to final/sink state, so just take same values as previous state
							 */
							matrix[i][col] = Math.min(matrix[i][col], matrix[otherRow][otherCol]);
							
						}
					}
				}
			}
			
			double min = matrix[ROWS-1][COLUMNS-1];			
			return (int)Math.round(min);
		}
		
		/**
		 * Note: this is different from normal matching algorithms, as we enforce an order
		 * among the operators: delete, replace and then insert. 
		 * @param graph
		 * @return
		 */
		public double calculateCostForStringAVM(RegexGraph graph){
			
			final int ROWS = graph.getNumberOfRows();
			final int COLUMNS = graph.getNumberOfColumns();
			
			/*
			 * we create a matrix based on each row and each column in the graph.
			 * Each cell has 3 values, each representing the cost of thre different types of path:
			 * 
			 * 0) only deletion
			 * 1) deletions followed by replacement
			 * 2) as above, and then followed by insertions
			 */
			final double[][][] matrix = new double[ROWS][COLUMNS][3]; 
			
			calculateInsertionCostOnFirstRow(graph, matrix);
			
			for(int i=1; i<ROWS; i++){
				
				for (int col = 0; col < COLUMNS; col++) {
					
					/*
					 * unless a path is explicitly updated, it will have maximum distance by default
					 */
					matrix[i][col][DEL] = Double.MAX_VALUE;
					matrix[i][col][REP] = Double.MAX_VALUE;
					matrix[i][col][INS] = Double.MAX_VALUE;
					
					for (GraphTransition t : graph.getIncomingTransitions(i, col)) {
						
						int otherCol = graph.getColumn(t.fromState);
						int otherRow = t.fromRow;
						
						if(t.type.equals(GraphTransition.TransitionType.INSERTION)){							
							assert otherRow == i;			
							/*
							 * if we have an insertion, only the insertion path can be continued.
							 * that's the reason why on the left side we only update for [INS].
							 * An insertion can continue any type of path (and so all types are present on the right side). 
							 */
							matrix[i][col][INS] = Math.min(matrix[i][col][INS], getSubPathCost(matrix[otherRow][otherCol][DEL],t.cost));
							matrix[i][col][INS] = Math.min(matrix[i][col][INS], getSubPathCost(matrix[otherRow][otherCol][REP],t.cost));
							matrix[i][col][INS] = Math.min(matrix[i][col][INS], getSubPathCost(matrix[otherRow][otherCol][INS],t.cost));
						} else if(t.type.equals(GraphTransition.TransitionType.REPLACEMENT)){
							/*
							 * if we have a replacement, then we cannot continue a delete path.
							 * So, no [DEL] on the left side.
							 * A replacement can continue a delete or replace path, but not an insertion one (and so [DEL] and
							 * [REP] on right side)
							 */
							matrix[i][col][REP] = Math.min(matrix[i][col][REP], getSubPathCost(matrix[otherRow][otherCol][DEL],t.cost));
							matrix[i][col][REP] = Math.min(matrix[i][col][REP], getSubPathCost(matrix[otherRow][otherCol][REP],t.cost));
							/*
							 * from this state on, an insertion path can be followed, with same cost (ie right side) as replacement path
							 */
							matrix[i][col][INS] = Math.min(matrix[i][col][INS], getSubPathCost(matrix[otherRow][otherCol][DEL],t.cost));
							matrix[i][col][INS] = Math.min(matrix[i][col][INS], getSubPathCost(matrix[otherRow][otherCol][REP],t.cost));
						} else if(t.type.equals(GraphTransition.TransitionType.DELETION)){
							/*
							 * deletion can only follow a deletion path (so only [DEL] or right side). 
							 * but, from this state on, any new path can be followed (so all on left side)
							 */
							matrix[i][col][DEL] = Math.min(matrix[i][col][DEL], getSubPathCost(matrix[otherRow][otherCol][DEL],t.cost));
							matrix[i][col][REP] = Math.min(matrix[i][col][REP], getSubPathCost(matrix[otherRow][otherCol][DEL],t.cost));
							matrix[i][col][INS] = Math.min(matrix[i][col][INS], getSubPathCost(matrix[otherRow][otherCol][DEL],t.cost));
						} else if(t.type.equals(GraphTransition.TransitionType.PHANTOM)){
							assert t.cost == 0;							
							/*
							 * artificial transition to final/sink state, so just take same values as previous state
							 */
							matrix[i][col][DEL] = Math.min(matrix[i][col][DEL], matrix[otherRow][otherCol][DEL]);
							matrix[i][col][REP] = Math.min(matrix[i][col][REP], matrix[otherRow][otherCol][REP]);
							matrix[i][col][INS] = Math.min(matrix[i][col][INS], matrix[otherRow][otherCol][INS]);
						}
					}
				}
				
				/*
				 * TODO: The algorithm of Myers's paper, at page 12, makes a distinction between D and E transitions.
				 * Insertions of type E are done last. Not fully clear if it has an effect here: ie, recall that
				 * here we do minimization (calculate distance) and not maximization (similarity)  
				 */
			}
			
			/*
			 * get the minimum among the 3 different paths in the sink state
			 */
			double min = Double.MAX_VALUE;
			for(double value : matrix[ROWS-1][COLUMNS-1]){
				if(value < min){
					min = value;
				}
			}
			
			return min;
		}

		/**
		 * We cannot just do previousStateCost + transitionCost, as there might be computational overflows
		 * 
		 * @param previousStateCost
		 * @param transitionCost
		 * @return
		 * @throws IllegalArgumentException
		 */
		private double getSubPathCost(double previousStateCost, double transitionCost) throws IllegalArgumentException{
			if(previousStateCost<0){
				throw new IllegalArgumentException("previousStateCost cannot be negative: "+previousStateCost);
			}
			if(transitionCost<0){
				throw new IllegalArgumentException("transitionCost cannot be negative: "+transitionCost);
			}
			
			if(previousStateCost == Double.MAX_VALUE || transitionCost == Double.MAX_VALUE){
				return Double.MAX_VALUE;
			}
			
			double sum =  previousStateCost + transitionCost;
			
			if(sum<previousStateCost || sum<transitionCost){
				/*
				 * likely overflow
				 */
				return Double.MAX_VALUE;
			}
			
			return sum;
		}
		
		/**
		 * First row is special, ie very different from the others
		 * 
		 * @param graph
		 * @param matrix
		 */
		private void calculateInsertionCostOnFirstRow(RegexGraph graph, final double[][][] matrix) {
			
			// First row is cost of matching empty sequence on regex
			final int FIRST_ROW = 0;
			
			/*
			 * init first starting state with 0 costs
			 */
			matrix[FIRST_ROW][0][0] = 0;
			matrix[FIRST_ROW][0][1] = 0;
			matrix[FIRST_ROW][0][2] = 0;
			
			for (int col = 1; col < graph.getNumberOfColumns(); col++) {

				double min = Double.MAX_VALUE;
				
				for (GraphTransition t :  graph.getIncomingTransitions(FIRST_ROW, col)) {
					
					/*
					 * on first row, there can be only insertions coming from the same row,
					 * apart from last node that can have a phantom transition to sink state
					 */
					assert t.type.equals(GraphTransition.TransitionType.INSERTION) ||
						t.type.equals(GraphTransition.TransitionType.PHANTOM);
					assert t.fromRow == 0;
					
					int otherCol = graph.getColumn(t.fromState);

					//self transition
					if (col == otherCol){
						continue;
					}
					
					double otherCost = matrix[FIRST_ROW][otherCol][2];

					min = Math.min(min, getSubPathCost(otherCost, t.cost));
				}
				
				/*
				 * as there can be only insertions, the delete and replace paths cannot be followed, and 
				 * so maximum distance
				 */
				matrix[FIRST_ROW][col][0] = Double.MAX_VALUE;
				matrix[FIRST_ROW][col][1] = Double.MAX_VALUE;				
				matrix[FIRST_ROW][col][2] = min;
			}
		}		
	}
}
