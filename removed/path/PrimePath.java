/*
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

package org.evosuite.coverage.path;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>PrimePath class.</p>
 *
 * @author Gordon Fraser
 */
public class PrimePath {

	private static Logger logger = LoggerFactory.getLogger(PrimePath.class);

	List<BytecodeInstruction> nodes = new ArrayList<BytecodeInstruction>();

	class PathEntry {
		Branch branch;
		boolean value;
	}

	List<PathEntry> branches = new ArrayList<PathEntry>();

	String className;

	String methodName;

	/**
	 * <p>Constructor for PrimePath.</p>
	 *
	 * @param className a {@link java.lang.String} object.
	 * @param methodName a {@link java.lang.String} object.
	 */
	public PrimePath(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
	}

	/**
	 * <p>getLast</p>
	 *
	 * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public BytecodeInstruction getLast() {
		return nodes.get(nodes.size() - 1);
	}

	/**
	 * <p>append</p>
	 *
	 * @param node a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public void append(BytecodeInstruction node) {
		nodes.add(node);
	}

	/**
	 * <p>getAppended</p>
	 *
	 * @param node a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a {@link org.evosuite.coverage.path.PrimePath} object.
	 */
	public PrimePath getAppended(BytecodeInstruction node) {
		PrimePath copy = new PrimePath(className, methodName);
		copy.nodes.addAll(nodes);
		copy.append(node);
		return copy;
	}

	/**
	 * <p>contains</p>
	 *
	 * @param vertex a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 * @return a boolean.
	 */
	public boolean contains(BytecodeInstruction vertex) {
		return nodes.contains(vertex);
	}

	/**
	 * <p>condensate</p>
	 */
	public void condensate() {
		for (int position = 0; position < nodes.size(); position++) {
			BytecodeInstruction node = nodes.get(position);
			if (node.isBranch() && position < (nodes.size() - 1)) {
				PathEntry entry = new PathEntry();
				entry.branch = BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchForInstruction(node);
				if (nodes.get(position + 1).getInstructionId() == (node.getInstructionId() + 1)) {
					logger.info("FALSE: Next ID is "
					        + nodes.get(position + 1).getInstructionId() + " / "
					        + (node.getInstructionId() + 1));
					entry.value = false;
				} else {
					logger.info("TRUE: Next ID is "
					        + nodes.get(position + 1).getInstructionId() + " / "
					        + (node.getInstructionId() + 1));
					entry.value = true;
				}
				branches.add(entry);
			}
		}

	}

	/**
	 * <p>getSize</p>
	 *
	 * @return a int.
	 */
	public int getSize() {
		return nodes.size();
	}

	/**
	 * <p>get</p>
	 *
	 * @param position a int.
	 * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public BytecodeInstruction get(int position) {
		return nodes.get(position);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrimePath other = (PrimePath) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < nodes.size(); i++) {
			builder.append(nodes.get(i).getInstructionId());
			builder.append(" ");
		}
		return builder.toString();
	}

}
