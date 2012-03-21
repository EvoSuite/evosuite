package de.unisb.cs.st.evosuite.coverage.behavioral;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import de.unisb.cs.st.evosuite.testcase.DefaultTestCase;

/** Class that provides the methods to compute the coverage test-suite. */
public class CoverageGenerator {
	
	/**
	 * Computes the smallest set of transition sequences
	 * covering all edges of a concrete graph.</br>
	 * This means that for all other possible sets of transition sequences
	 * covering all edges of the graph the number of all transitions
	 * of the transitions sequences is greater or equal than the one of the set
	 * computed by this method.
	 * 
	 * <p><b>Note:</b> This method <b>does</b> modify the graph given
	 * as parameter. All edges of the given graph are set discovered <tt>true</tt>
	 * and for every edge a transition sequence leading to this edge is stored,
	 * whereby the <tt>lastInAlpha</tt>-flag is set accordingly.</p>
	 * 
	 * @param graph - the graph to cover.
	 * 
	 * @return the smallest set of transition sequences
	 *         covering all edges of the graph.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given graph is <tt>null</tt>.
	 * @throws Error
	 *             if an error occurs during the computation.
	 */
	public Set<TransitionSequence> generateTransitionSequences(BCGraph graph) {
		if (graph == null)
			throw new IllegalArgumentException("The given graph is null!");
		
		// initialize the edges of the given graph
		for (BCEdge edge : graph.getEdges()) {
			edge.setTransitionSequence(null, false);
			edge.setDiscovered(false);
		}
		
		// mapping edge to edge for updating transition sequences
		HashMap<BCEdge,BCEdge> map = new HashMap<BCEdge,BCEdge>();
		
		// nodes to examine
		LinkedList<BCNode> nodes = new LinkedList<BCNode>();
		nodes.add(graph.getStartNode());
		
		// breadth-first search
		while (!nodes.isEmpty()) {
			// extract the first node
			BCNode node = nodes.pollFirst();
			List<BCEdge> undiscoveredOutgoingEdges = node.getUndiscoveredOutgoingEdges();
			
			// if all outgoing edges are discovered then the node is discovered
			if (!undiscoveredOutgoingEdges.isEmpty()) {
				Set<BCEdge> incomingEdges = node.getIncomingEdges();
				Set<BCNode> newNodes = new HashSet<BCNode>();
				if (incomingEdges.isEmpty()) {
					assert (node instanceof BCEmptyNode);
					// set transition sequences for all outgoing edges
					for (BCEdge edge : undiscoveredOutgoingEdges) {
						TransitionSequence alpha = new TransitionSequence();
						alpha.add(edge.getTransition());
						edge.setTransitionSequence(alpha, true);
						edge.setDiscovered(true);
						newNodes.add(edge.getEndNode());
					}
				} else {
					// check for self loops
					LinkedList<BCEdge> selfLoops = new LinkedList<BCEdge>();
					for (BCEdge edge : undiscoveredOutgoingEdges) {
						if (edge.getStartNode().equals(edge.getEndNode())) {
							selfLoops.add(edge);
						}
					}
					if (!selfLoops.isEmpty()) {
						for (BCEdge edge : incomingEdges) {
							if (edge.isDiscovered() && edge.isLastInAlpha()) {
								edge.setLastInAlpha(false);
								BCEdge old = edge;
								// expand transition sequence by the loops
								for (int i = 0; i < selfLoops.size(); i++) {
									BCEdge loop = selfLoops.get(i); 
									TransitionSequence alpha = new TransitionSequence(old.getTransitionSequence());
									alpha.add(loop.getTransition());
									// set for last loop isLastInAlpha true
									loop.setTransitionSequence(alpha, (i == selfLoops.size()-1));
									loop.setDiscovered(true);
									map.put(old, loop);
									old = loop;
								}
								break;
							}
						}
						undiscoveredOutgoingEdges.removeAll(selfLoops);
					}
					
					// try to expand transition sequence of incoming edges
					for (BCEdge incomingEdge : incomingEdges) {
						if (!undiscoveredOutgoingEdges.isEmpty()
								&& incomingEdge.isDiscovered()
									&& incomingEdge.isLastInAlpha()) {
							BCEdge outgoingEdge = undiscoveredOutgoingEdges.remove(0);
							TransitionSequence alpha = new TransitionSequence(incomingEdge.getTransitionSequence());
							alpha.add(outgoingEdge.getTransition());
							incomingEdge.setLastInAlpha(false);
							outgoingEdge.setTransitionSequence(alpha, true);
							outgoingEdge.setDiscovered(true);
							map.put(incomingEdge, outgoingEdge);
							newNodes.add(outgoingEdge.getEndNode());
						}
					}
				}
				nodes.addAll(newNodes);
			}
		}
		
		// check for undiscovered chains
		LinkedList<LinkedList<BCEdge>> chainList = new LinkedList<LinkedList<BCEdge>>();
		LinkedList<BCEdge> targetEdges = new LinkedList<BCEdge>();
		LinkedList<BCEdge> startEdges = new LinkedList<BCEdge>();
		LinkedList<BCEdge> edgesToExamine = new LinkedList<BCEdge>(graph.getEdges());
		// examine all edges
		while (!edgesToExamine.isEmpty()) {
			BCEdge edge = edgesToExamine.pollFirst();
			if (edge.isLastInAlpha()) {
				startEdges.add(edge);
				continue;
			}
			if (!edge.isDiscovered()) {
				BCNode node = edge.getStartNode();
				// check whether the edge is start edge of a chain
				if (node.getUndiscoveredIncomingEdges().isEmpty()) {
					// compute chain
					LinkedList<BCEdge> chain = new LinkedList<BCEdge>();
					exploreChain(edge, chain);
					chainList.add(chain);
					
					// add target and start edge
					targetEdges.addFirst(edge); // same index of target and start means same chain
					startEdges.addFirst(chain.getLast());
				} else {
					// check if the number of undiscovered incoming and outgoing edges are equal
					if (node.getUndiscoveredIncomingEdges() != node.getUndiscoveredOutgoingEdges())
						edgesToExamine.addLast(edge); // need to reexamine the edge
				}
			}
		}
		
		// check for undiscovered loops
		LinkedList<LinkedList<BCEdge>> loopList = new LinkedList<LinkedList<BCEdge>>();
		for (BCNode node : graph.getNodes()) {
			List<BCEdge> undiscoveredOutgoingEdges = node.getUndiscoveredOutgoingEdges();
			List<BCEdge> undiscoveredIncomingEdges = node.getUndiscoveredIncomingEdges();
			
			// possible loop if there are undiscovered incoming and outgoing edges
			while (!undiscoveredOutgoingEdges.isEmpty() && !undiscoveredIncomingEdges.isEmpty()) {
				LinkedList<BCEdge> loop = new LinkedList<BCEdge>();
				boolean isLoop = exploreLoop(node, undiscoveredOutgoingEdges.remove(0), loop);
				if (isLoop) {
					loopList.add(loop);
					undiscoveredIncomingEdges.remove(loop.getLast());
				}
			}
		}
		
		// if chains exist solve optimization problem
		if (!targetEdges.isEmpty()) {
			// create edge from start node of the graph as possible alpha beginning
			BCEdge dummy = new BCEdge(graph.getStartNode(), new DefaultTestCase(), graph.getStartNode());
			dummy.getStartNode().removeOutgoingEdge(dummy);
			dummy.getEndNode().removeIncomingEdge(dummy);
			dummy.setLastInAlpha(true);
			startEdges.add(dummy);
			
			// compute the alpha matrix
			TransitionSequence[][] alpha_matrix = new TransitionSequence[targetEdges.size()][startEdges.size()];
			for (int i = 0; i < alpha_matrix.length; i++) {
				for (int j = 0; j < alpha_matrix[i].length; j++) {
					if (i == j) continue; // null for edges in same chain
					alpha_matrix[i][j] = getShortestTransitionSequence(graph, startEdges.get(j), targetEdges.get(i));
				}
			}
			
			// solve optimization problem
			AlphaOptimizer dm = new AlphaOptimizer(targetEdges, startEdges, alpha_matrix);
			Map<BCEdge,BCEdge> result = dm.branchAndBound(); // mapping target to start
			
			// insert the chains
			while (!chainList.isEmpty()) {
				LinkedList<BCEdge> chain = chainList.pollFirst();
				BCEdge old = result.get(chain.getFirst());
				TransitionSequence connection =
						alpha_matrix[targetEdges.indexOf(chain.getFirst())][startEdges.indexOf(old)];
				if (connection == null)
					throw new Error("The graph is not connected - could not insert the chain '" + chain + "'!");
				
				// try next chain if old not last in alpha
				if (!old.isLastInAlpha()) {
					chainList.add(chain);
					continue;
				}
				if (!old.equals(dummy)) // dummy edge always is last in alpha
					old.setLastInAlpha(false);
				
				// update the transition sequences
				for (int i = 0; i < chain.size(); i++) {
					BCEdge next = chain.get(i);
					TransitionSequence alpha = new TransitionSequence(old.getTransitionSequence());
					// add transition sequence from alpha matrix to first edge i == 0
					if (i == 0) alpha.addAll(connection);
					alpha.add(next.getTransition());
					// set for last chain isLastInAlpha true
					next.setTransitionSequence(alpha, (i == chain.size()-1));
					map.put(old, next);
					old = next;
				}
			}
		}
		
		// insert the loops
		for (LinkedList<BCEdge> loop : loopList) {
			// search edge to insert loop
			BCEdge old = null;
			LinkedList<BCEdge> rotatedLoop = new LinkedList<BCEdge>();
			for (BCEdge edge : loop) {
				for (BCEdge incoming : edge.getStartNode().getIncomingEdges()) {
					if (loop.contains(incoming))
						continue;
					if (old == null && incoming.isDiscovered())
						old = incoming;
					if (incoming.isLastInAlpha()) {
						old = incoming;
						break;
					}
				}
				if (old != null) {
					rotatedLoop.add(edge);
					break;
				}
			}
			// reorder the edges
			if (!rotatedLoop.isEmpty()) {
				BCEdge first = rotatedLoop.getFirst();
				int index = loop.indexOf(first);
				for (int i = index+1; i < loop.size(); i++) {
					rotatedLoop.add(loop.get(i));
				}
				for (int i = 0; i < index; i++) {
					rotatedLoop.add(loop.get(i));
				}
			}
			
			if (old == null)
				throw new Error("The graph is not connected - could not insert the loop '" + rotatedLoop + "'!");
			if (!old.isLastInAlpha()) {
				// add all edges up to last one in alpha
				BCEdge tmp = map.remove(old);
				rotatedLoop.add(tmp);
				while ((tmp = map.remove(tmp)) != null) {
					rotatedLoop.add(tmp);
				}
			}
			else old.setLastInAlpha(false);
			
			// update the transition sequences
			for (int i = 0; i < rotatedLoop.size(); i++) {
				BCEdge next = rotatedLoop.get(i);
				TransitionSequence alpha = new TransitionSequence(old.getTransitionSequence());
				alpha.add(next.getTransition());
				// set for last loop isLastInAlpha true
				next.setTransitionSequence(alpha, (i == rotatedLoop.size()-1));
				map.put(old, next);
				old = next;
			}
		}
		
		// compute the set of all transition sequences covering the graph
		HashSet<TransitionSequence> trans_seq = new HashSet<TransitionSequence>();
		boolean allEdgesDiscovered = true;
		for (BCEdge edge : graph.getEdges()) {
			if (!edge.isDiscovered()) // all edges actually should be discovered
				allEdgesDiscovered = false;
			if (edge.isLastInAlpha())
				trans_seq.add(edge.getTransitionSequence());
		}
		if (!allEdgesDiscovered)
			System.out.println("* Warning: Could not discover all edges of the graph");
		return trans_seq;
	}
	
	/**
	 * <b>Helper method.</b></br>
	 * Explores undiscovered chains in the graph. Given an undiscovered
	 * start edge the method iterates recursively over the undiscovered edges.
	 * 
	 * <p><b>Note:</b> As side effects this method adds all edges of
	 * an undiscovered chain to the parameter list and sets all edges
	 * discovered <tt>true</tt>.</p>
	 * 
	 * @param edge - the current edge in the recursion.
	 * @param discoveredChains - the list of the already discovered chain.
	 * 
	 * @return <tt>true</tt> if a chain was found; <tt>false</tt> otherwise.
	 */
	private boolean exploreChain(BCEdge edge, List<BCEdge> discoveredChains) {
		if (edge.isDiscovered())
			return false;
		
		discoveredChains.add(edge);
		edge.setDiscovered(true);
		
		BCNode node = edge.getEndNode();
		List<BCEdge> nDOE = node.getUndiscoveredOutgoingEdges();
		if (!nDOE.isEmpty()) {
			exploreChain(nDOE.get(0), discoveredChains);
		}
		return true;
	}
	
	/**
	 * <b>Helper method.</b></br>
	 * Explores undiscovered loops in the graph. Given the start node
	 * of a possible loop the method iterates recursively over the
	 * undiscovered edges.
	 * 
	 * <p><b>Note:</b> As side effects this method adds all edges of
	 * an undiscovered loop to the parameter list and sets all edges
	 * discovered <tt>true</tt>.</p>
	 * 
	 * @param startNode - the beginning node of the potential loop.
	 * @param edge - the current edge in the recursion.
	 * @param discoveredLoops - the list of the already discovered loop.
	 * 
	 * @return <tt>true</tt> if a loop was found; <tt>false</tt> otherwise.
	 */
	private boolean exploreLoop(BCNode startNode, BCEdge edge, LinkedList<BCEdge> discoveredLoops) {
		if (edge.isDiscovered())
			return false;
		
		BCNode node = edge.getEndNode();
		if (node.getOutgoingEdges().isEmpty())
			return false;
		
		discoveredLoops.add(edge);
		edge.setDiscovered(true);
		if (node.equals(startNode))
			return true;
		for (BCEdge outgoingEdge : node.getOutgoingEdges()) {
			if (exploreLoop(startNode, outgoingEdge, discoveredLoops))
				return true;
		}
		discoveredLoops.removeLast();
		edge.setDiscovered(false);
		return false;
	}
	
	/** A mapping node to distance. */
	private Map<BCNode,Integer> d;
	
	/** A mapping node to predecessor node. */
	private Map<BCNode,BCNode> p;
	
	/**
	 * <b>Helper method.</b></br>
	 * Computes the shortest transition sequence between two edges
	 * in the graph.</p>
	 * 
	 * @param graph - the graph.
	 * @param start - the start edge of the transition sequence to compute.
	 * @param target - the target edge.
	 * 
	 * @return the shortest transition sequence connecting start
	 *         with target or <tt>null</tt> if they are not connected.
	 */
	private TransitionSequence getShortestTransitionSequence(BCGraph graph, BCEdge start, BCEdge target) {
		if (start.equals(target))
			return null;
		
		BCNode s = start.getEndNode();
		BCNode t = target.getStartNode();
		int capacity = graph.getNodes().size();
		d = new HashMap<BCNode,Integer>(capacity); // mapping node to distance
		p = new HashMap<BCNode,BCNode>(capacity); // mapping node to predecessor
		
		// initialization
		for (BCNode node : graph.getNodes()) {
			d.put(node, Integer.MAX_VALUE-100000);
		}
		d.put(s, 0);
		
		// comparator for priority queue
		Comparator<BCNode> comparator = new Comparator<BCNode>() {
			@Override
			public int compare(BCNode o1, BCNode o2) {
				return d.get(o1) - d.get(o2);
			}
		};
		
		// minimum priority queue using d as key
		PriorityQueue<BCNode> queue = new PriorityQueue<BCNode>(graph.getNodes().size(), comparator);
		queue.addAll(graph.getNodes());
		while (!queue.isEmpty()) {
			BCNode node = queue.poll();
			HashSet<BCNode> successors = new HashSet<BCNode>();
			for (BCEdge edge : node.getOutgoingEdges()) {
				successors.add(edge.getEndNode());
			}
			// update d and p for all successor
			for (BCNode successor : successors) {
				if (d.get(successor) > d.get(node)+1) {
					d.put(successor, d.get(node)+1);
					p.put(successor, node);
				}
			}
			// update the priority queue
			if (!queue.isEmpty()) {
				LinkedList<BCNode> list = new LinkedList<BCNode>(queue);
				queue = new PriorityQueue<BCNode>(queue.size(), comparator);
				queue.addAll(list);
			}
		}
		
		// compute transition sequence target edge to start edge
		TransitionSequence alpha = new TransitionSequence();
		BCNode old = t;
		BCNode next;
		while (!s.equals(old)) {
			next = p.get(old);
			if (next == null || old.getIncomingEdges().isEmpty())
				return null;
			
			// search connecting edge
			for (BCEdge edge : old.getIncomingEdges()) {
				if (next.equals(edge.getStartNode())) {
					alpha.addFirst(edge.getTransition());
					break;
				}
			}
			old = next;
		}
		return alpha;
	}
}
