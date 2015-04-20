package org.evosuite.setup.callgraph;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * 
 * @author mattia
 *
 */
public class PathFinder {

	private PathFinder() {}

	/**
	 * if startingVertex is not included in the graph, returns an empty collection.
	 * XXX should it throws an exception?
	 * @param g
	 * @param startingVertex
	 * @return
	 */
	public static <E> Set<List<E>> getPahts(Graph<E> g, E startingVertex) {
		if(!g.containsVertex(startingVertex)){
			return new HashSet<>();
		}
		PathFinderDFSIterator<E> dfs = new PathFinderDFSIterator<E>(g, startingVertex);
		while (dfs.hasNext()) {
			dfs.next();
		}
		return dfs.getPaths();
	}
	
	public static <E> Set<List<E>> getReversePahts(Graph<E> g, E startingVertex) {
		if(!g.containsVertex(startingVertex)){
			return new HashSet<>();
		}
		PathFinderDFSIterator<E> dfs = new PathFinderDFSIterator<E>(g, startingVertex,true);
		while (dfs.hasNext()) {
			dfs.next();
		}
		return dfs.getPaths();
	}

}
