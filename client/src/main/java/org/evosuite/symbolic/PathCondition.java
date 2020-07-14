/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic;

import java.util.LinkedList;
import java.util.List;

import org.evosuite.symbolic.expr.Constraint;

/**
 * Represents a sequence of path condition nodes.
 * 
 * @author galeotti
 *
 */
public class PathCondition {

	private final List<PathConditionNode> pathCondition;

	/**
	 * Creates a new path condition from a list of path condition nodes
	 * 
	 * @param pathConditionNodes
	 */
	public PathCondition(List<PathConditionNode> pathConditionNodes) {
		this.pathCondition = new LinkedList<PathConditionNode>(pathConditionNodes);
	}

	/**
	 * Returns the constraints for this path condition
	 * 
	 * @return
	 */
	public List<Constraint<?>> getConstraints() {
		List<Constraint<?>> constraints = new LinkedList<Constraint<?>>();
		for (PathConditionNode b : this.pathCondition) {
			constraints.addAll(b.getSupportingConstraints());
			constraints.add(b.getConstraint());
		}
		return constraints;
	}

	/**
	 * Returns the list of path condition nodes on this path condition
	 * 
	 * @return
	 */
	public List<PathConditionNode> getPathConditionNodes() {
		return this.pathCondition;
	}

	/**
	 * Returns true if the path condition is empty
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return this.pathCondition.isEmpty();
	}

	/**
	 * The length of the path condition in terms of path condition nodes
	 * 
	 * @return
	 */
	public int size() {
		return this.pathCondition.size();
	}

	/**
	 * Returns the path condition node at position <code>index</code>
	 * 
	 * @param index
	 * @return
	 */
	public PathConditionNode get(int index) {
		return this.pathCondition.get(index);
	}
	
	public String toString() {
	  return pathCondition.toString();
	}
}
