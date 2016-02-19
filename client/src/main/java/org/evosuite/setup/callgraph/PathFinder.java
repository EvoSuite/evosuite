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
