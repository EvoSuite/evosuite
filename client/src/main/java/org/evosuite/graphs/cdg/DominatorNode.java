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
package org.evosuite.graphs.cdg;

import java.util.HashSet;
import java.util.Set;

/**
 * This class serves as a convenience data structure within cfg.DominatorTree
 * 
 * For every node within a CFG for which the immediateDominators are to be
 * computed this class holds auxiliary information needed during the computation
 * inside the DominatorTree
 * 
 * After that computation instances of this class hold the connection between
 * CFG nodes and their immediateDominators
 * 
 * Look at cfg.DominatorTree for more detailed information
 * 
 * 
 * @author Andre Mis
 */
class DominatorNode<V> {

	final V node;
	int n = 0;

	// parent of node within spanning tree of DFS inside cfg.DominatorTree
	DominatorNode<V> parent;

	// computed dominators 
	DominatorNode<V> semiDominator;
	DominatorNode<V> immediateDominator;

	// auxiliary field needed for dominator computation
	Set<DominatorNode<V>> bucket = new HashSet<DominatorNode<V>>();

	// data structure needed to represented forest produced during cfg.DominatorTree computation
	DominatorNode<V> ancestor;
	DominatorNode<V> label;

	DominatorNode(V node) {
		this.node = node;

		this.label = this;
	}

	void link(DominatorNode<V> v) {
		ancestor = v;
	}

	DominatorNode<V> eval() {
		if (ancestor == null)
			return this;

		compress();

		return label;
	}

	void compress() {
		if (ancestor == null)
			throw new IllegalStateException("may only be called when ancestor is set");

		if (ancestor.ancestor != null) {
			ancestor.compress();
			if (ancestor.label.semiDominator.n < label.semiDominator.n)
				label = ancestor.label;

			ancestor = ancestor.ancestor;
		}
	}

	DominatorNode<V> getFromBucket() {

		for (DominatorNode<V> r : bucket)
			return r;

		return null;
	}

	/**
	 * <p>isRootNode</p>
	 *
	 * @return a boolean.
	 */
	public boolean isRootNode() {
		// TODO not that nice :/
		return n == 1;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "DTNode " + n + " - " + node;
	}
}
