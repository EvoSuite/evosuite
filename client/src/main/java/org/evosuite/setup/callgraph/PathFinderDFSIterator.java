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

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
/**
 * 
 * @author mattia
 *
 */
public class PathFinderDFSIterator<E> implements Iterator<E> {
	private Set<E> visited = new HashSet<E>();
	private Deque<Iterator<E>> stack = new LinkedList<Iterator<E>>();
	private Graph<E> graph;
	private E next;
	private Set<List<E>> paths = new HashSet<>();
	private List<E> currentPath = new ArrayList<>();
	private boolean reversed=false;

	public PathFinderDFSIterator(Graph<E> g, E startingVertex) {
		this(g, startingVertex, false);
	}
	
	public PathFinderDFSIterator(Graph<E> g, E startingVertex, boolean reversed) {
		if (!reversed) {
			this.stack.push(g.getNeighbors(startingVertex).iterator());
		} else {
			this.stack.push(g.getReverseNeighbors(startingVertex).iterator());
		}
		this.graph = g;
		this.next = startingVertex;
		paths.add(currentPath);
		this.reversed=reversed;
	}

	public Set<List<E>> getPaths() {
		return paths;
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNext() {
		return this.next != null;
	}

	@Override
	public E next() {
		if (this.next == null) {
			throw new NoSuchElementException();
		}
		try {
			this.visited.add(this.next);
			currentPath.add(next);
			return this.next;
		} finally {
			this.advance();
		}
	}

	private void advance() {
		Iterator<E> neighbors = this.stack.peek();
		boolean update = false;

		do {
			int levelback = 0;
			while (!neighbors.hasNext()) { // No more nodes -> back out a level
				this.stack.pop();
				if (this.stack.isEmpty()) { // All done!
					this.next = null;
					return;
				}
				neighbors = this.stack.peek();
				levelback++;
				update = true;
			}

			if (update) {
				List<E> newPath = new ArrayList<>(currentPath.subList(0,
						currentPath.size() - levelback));
				currentPath = newPath;
				paths.add(newPath);
				update = false;
			}

			this.next = neighbors.next();
			

		} while (this.visited.contains(this.next));
		if (!reversed) {
			this.stack.push(this.graph.getNeighbors(this.next).iterator());
		} else {
			this.stack.push(this.graph.getReverseNeighbors(this.next).iterator());
		}
	}
}
