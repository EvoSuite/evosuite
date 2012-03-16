package de.unisb.cs.st.evosuite.coverage.behavioral;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** Class that computes the optimization problem of shortest edge connecting. */
public class AlphaOptimizer {
	
	/** The matrix holding the distances between edges. */
	private int[][] distance_matrix; // diagonal entries represent same chain
	
	/** The list of edges in the rows of the distance matrix. */
	private List<BCEdge> targetEdges;
	
	/** The list of edges in the columns of the distance matrix. */
	private List<BCEdge> startEdges;
	
	/**
	 * Creates a new <tt>AlphaOptimizer</tt> with given alpha matrix and corresponding edges.</p>
	 * 
	 * @param targetEdges - the target edges to find a corresponding start edge to.
	 * @param startEdges - the possible start edges.
	 * @param alpha_matrix - the matrix holding the transition sequences between the edges.
	 * 
	 * @throws IllegalArgumentException
	 *             if one of the parameters is <tt>null</tt>.
	 */
	public AlphaOptimizer(List<BCEdge> targetEdges, List<BCEdge> startEdges, TransitionSequence[][] alpha_matrix) {
		if (targetEdges == null)
			throw new IllegalArgumentException("The given list of target edges is null!");
		if (startEdges == null)
			throw new IllegalArgumentException("The given list of start edges is null!");
		if (alpha_matrix == null)
			throw new IllegalArgumentException("The given alpha matrix is null!");
		
		this.targetEdges = targetEdges;
		this.startEdges = startEdges;
		this.distance_matrix = new int[alpha_matrix.length][alpha_matrix[0].length];
		
		// initialize the distance matrix
		for (int i = 0; i < distance_matrix.length; i++) {
			for (int j = 0; j < distance_matrix[i].length; j++) {
				if (alpha_matrix[i][j] == null)
					distance_matrix[i][j] = Integer.MAX_VALUE;
				else 
					distance_matrix[i][j] = alpha_matrix[i][j].size();
			}
		}
	}
	
	/**
	 * Computes the optimization problem of connecting edges over
	 * already discovered edges. Due to the distance matrix holding
	 * the distances between the edges to connect this method is
	 * solving a linear objective function. The row i and columns j
	 * of the distance matrix represent the variables A <sub>i j</sub>.</br>
	 * Each variable is assigned a value k&#8712;{0,1}, where k = 1
	 * is assigned if the target edge represented by i shall be connected
	 * with the start edge represented by j or k = 0 if the 
	 * edges are not chosen to be connected.</p>
	 * 
	 * @return a mapping target edge to start edge so that the sum
	 *         of all transitions of the transition sequences between
	 *         the edges of this mapping is minimal.
	 */
	public Map<BCEdge,BCEdge> branchAndBound() {
		// compute the greatest lower bound for the start iteration
		int glb = 0;
		for (int i = 0; i < distance_matrix.length; i++) {
			glb = glb + getMin(distance_matrix[i], null);
		}
		return branchAndBound(glb);
	}
	
	/**
	 * <b>Helper method.</b></br>
	 * Computes for every variable the possible solutions with
	 * lowest distance between the edges.
	 * If all solutions with lowest value have cycles between
	 * the target edges this method is called again with increased
	 * greatest lower bound.</p>
	 * 
	 * @param glb - the greatest lower bound of acceptable solutions.
	 * 
	 * @return a mapping target edge to start edge so that the sum
	 *         of all transitions of the transition sequences between
	 *         the edges of this mapping is minimal.
	 */
	private Map<BCEdge,BCEdge> branchAndBound(int glb) {
		LinkedList<Solution> solutions = new LinkedList<Solution>();
		// iteration over all branches of the first variable
		int lowest_value = -1;
		for (int i = 1; i < distance_matrix[0].length; i++) {
			LinkedList<Integer> list = new LinkedList<Integer>();
			list.add(i); // add chosen index for first variable
			
			int value = distance_matrix[0][i];
			if (value == Integer.MAX_VALUE) continue; // no solution
			// choose best value for every remaining variable
			for (int j = 1; j < distance_matrix.length; j++) {
				value = value + getMin(distance_matrix[j], list);
			}
			
			if (value < glb) continue; // no solution - value is less than greatest lower bound
			
			// check for possible solution
			if (lowest_value == -1) { // initialize solution
				lowest_value = value;
				solutions.add(new Solution(value, list));
			} else {
				if (value == lowest_value) { // add solution of same value
					solutions.add(new Solution(value, list));
				} else {
					if (value < lowest_value) { // new list of solutions with lower value
						solutions = new LinkedList<Solution>();
						lowest_value = value;
						solutions.add(new Solution(value, list));
					}
				}
			}
		}
		
		// iteration over all remaining variables
		for (int i = 1; i < distance_matrix.length; i++) {
			LinkedList<Solution> oldSolutions = new LinkedList<Solution>();
			oldSolutions.addAll(solutions);
			solutions = new LinkedList<Solution>();
			// set lowest value to undefined
			lowest_value = -1;
			// choose value for variable i for every solution
			for (Solution solution : oldSolutions) {
				// iteration over all possibles values of the variable i
				for (int j = 0; j < distance_matrix[i].length; j++) {
					LinkedList<Integer> list = new LinkedList<Integer>(solution.getDistanceIndexes());
					if (list.contains(j)) continue; // no solution - j already chosen
					if (j == i) continue; // no solution - j and i are in the same chain
					
					int value = distance_matrix[i][j];
					if (value == Integer.MAX_VALUE) continue; // no solution
					// add chosen values
					for (int h = 0; h < list.size(); h++) {
						value = value + distance_matrix[h][list.get(h)];
					}
					// add chosen index for variable i
					list.add(j);
					// choose best value for every remaining variable
					for (int h = i+1; h < distance_matrix.length; h++) {
						value = value + getMin(distance_matrix[h], list);
					}
					
					if (value < glb) continue; // no solution - value is less than greatest lower bound
					
					// check for possible solution
					if (isSolution(list)) {
						if (lowest_value == -1) { // initialize solution
							lowest_value = value;
							solutions.add(new Solution(value, list));
						} else {
							if (value == lowest_value) { // add solution of same value
								// TODO reduce redundancy due to several dummy edges
								// solutions.add(new Solution(value, list));
							} else {
								if (value < lowest_value) { // new list of solutions with lower value
									solutions = new LinkedList<Solution>();
									lowest_value = value;
									solutions.add(new Solution(value, list));
								}
							}
						}
					}
				}
			}
		}
		
		// check whether a solution exists
		if (!solutions.isEmpty()) {
			return solutions.getFirst().getEdgeMap();
		} else { // increase greatest lower bound for next iteration if no solution exists
			return branchAndBound(glb+1);
		}
	}
	
	/**
	 * <b>Helper method.</b></br>
	 * Computes the lowest possible value a variable can choose.</p>
	 * 
	 * @param arr - the row array of a variable to get the lowest value from.
	 * @param restrictedIndexes - the index of the already chosen columns in the distance matrix.
	 * 
	 * @return the lowest possible value of the array.
	 */
	private int getMin(int[] arr, List<Integer> restrictedIndexes) {
		int min = -1;
		if (restrictedIndexes == null) { // no columns of the distance matrix are restricted
			min = arr[0];
			for (int i = 1; i < arr.length; i++) {
				if (arr[i] < min) min = arr[i];
			}
		} else {
			for (int i = 0; i < arr.length; i++) {
				if (!restrictedIndexes.contains(i)) {
					if (min == -1) {
						min = arr[i];
					} else {
						if (arr[i] < min) min = arr[i];
					}
				}
			}
		}
		return min;
	}
	
	/**
	 * <b>Helper method.</b></br>
	 * Checks whether a solution is a possible solution,
	 * i.e. when the already chosen corresponding target
	 * and start edge have no inner cycles.</p>
	 * 
	 * @param distance_indexes - the list of already chosen indexes by a solution to check.
	 * 
	 * @return <tt>true</tt> if the solution the distance_matrix belongs to
	 *         is a possible solution; <tt>false</tt> otherwise.
	 */
	private boolean isSolution(List<Integer> distance_indexes) {
		if (distance_indexes.size() < 2)
			return true;
		
		// check cycle occurrences
		LinkedList<Integer> checkedIndexes = new LinkedList<Integer>();
		for (Integer index : distance_indexes) {
			if (checkedIndexes.contains(index))
				continue;
			checkedIndexes.add(index);
			if (index < distance_indexes.size() // the size always is smaller than the column length of the distance matrix
					&& exploreCycle(distance_indexes, index, distance_indexes.get(index), checkedIndexes))
				return false;
		}
		return true;
	}
	
	/**
	 * <b>Helper method.</b></br>
	 * Checks recursively whether the chosen indexes of the distance matrix
	 * by a solution contain an inner cycle.</p>
	 * 
	 * @param distance_indexes - the list of already chosen indexes by a solution to check.
	 * @param startIndex - the index to check whether it is the start of a cycle.
	 * @param index - the current index in the recursion.
	 * @param checkedIndexes - the list of already checked indexes.
	 * 
	 * @return <tt>true</tt> if there is an inner cycle in the list
	 *         of chosen indexes; <tt>false</tt> otherwise.
	 */
	private boolean exploreCycle(List<Integer> distance_indexes, int startIndex, int index, List<Integer> checkedIndexes) {
		if (startIndex == index)
			return true;
		
		if (!checkedIndexes.contains(index)) {
			checkedIndexes.add(index);
			if (index < distance_indexes.size()) // the size always is smaller than the column length of the distance matrix
				return exploreCycle(distance_indexes, startIndex, distance_indexes.get(index), checkedIndexes);
		}
		return false;
	}
	
	
	
	/** Inner class that represents a solution of the optimization problem. */
	public class Solution {
		
		/** The value of the solution. */
		private int value;
		
		/** The indexes chosen in the distance matrix by the solution. */
		private List<Integer> distance_indexes;
		
		/**
		 * Creates a new solution with given value and distance indexes.</p>
		 * 
		 * @param value - the value of this solution.
		 * @param distance_indexes - the list of chosen indexes by this solution.
		 * 
		 * @throws IllegalArgumentException
		 *             if the given list of chosen indexes is <tt>null</tt>.
		 */
		public Solution(int value, LinkedList<Integer> distance_indexes) {
			if (distance_indexes == null)
				throw new IllegalArgumentException("The given list of distance indexes of the solution is null!");
			
			this.value = value;
			this.distance_indexes = distance_indexes;
		}
		
		/**
		 * Creates the edge mapping for this solution.</p>
		 * 
		 * @return the mapping target edge to start edge given by
		 *         the list of chosen indexes of this solution.
		 */
		public Map<BCEdge,BCEdge> getEdgeMap() {
			assert (distance_indexes.size() == distance_matrix.length)
				: "The number of distance indexes and the column length of the distance matrix are not equal!";
			HashMap<BCEdge,BCEdge> map = new HashMap<BCEdge,BCEdge>(distance_indexes.size());
			for (int i = 0; i < distance_indexes.size(); i++) {
				map.put(targetEdges.get(i), startEdges.get(distance_indexes.get(i)));
			}
			return map;
		}
		
		/**
		 * Two solutions are equal if they have the same
		 * chosen indexes.</p>
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Solution) {
				Solution solution = (Solution) obj;
				return distance_indexes.equals(solution.getDistanceIndexes());
			}
			return false;
		}
		
		/**
		 * Returns a hash code for this solution,
		 * i.e. the value of this solution.</p>
		 * 
		 * @return a hash code for this solution.
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return value;
		}
		
		/**
		 * Returns a string representation of this solution,
		 * i.e. a pair (solution, value).</p>
		 * 
		 * @return a string representation of this solution.
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuffer result = new StringBuffer();
			result.append("(");
			result.append(distance_indexes);
			result.append(", ");
			result.append(value);
			result.append(")");
			return result.toString();
		}
		
		/**
		 * Returns the value of this solution.</p>
		 * 
		 * @return the value of this solution.
		 */
		public int getValue() {
			return value;
		}
		
		/**
		 * Returns the list of distance indexes of this solution.</p>
		 * 
		 * @return the list of distance indexes of this solution.
		 */
		public List<Integer> getDistanceIndexes() {
			return distance_indexes;
		}
	}
}
