/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.path;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;

/**
 * @author Gordon Fraser
 * 
 */
public class PrimePath {

	class PathEntry {
		BytecodeInstruction vertex;
		boolean value;
	}

	List<BytecodeInstruction> nodes = new ArrayList<BytecodeInstruction>();

	List<PathEntry> branches = new ArrayList<PathEntry>();

	String className;

	String methodName;

	public PrimePath(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
	}

	public void append(BytecodeInstruction node) {
		nodes.add(node);
	}

	public void condensate() {
		for (int position = 0; position < nodes.size(); position++) {
			BytecodeInstruction node = nodes.get(position);
			if (node.isBranch() && (position < (nodes.size() - 1))) {
				PathEntry entry = new PathEntry();
				entry.vertex = node;
				if (nodes.get(position + 1).getInstructionId() == (node.getInstructionId() + 1)) {
					entry.value = false;
				} else {
					entry.value = true;
				}
				branches.add(entry);
			}
		}

	}

	public boolean contains(BytecodeInstruction vertex) {
		return nodes.contains(vertex);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PrimePath other = (PrimePath) obj;
		if (className == null) {
			if (other.className != null) {
				return false;
			}
		} else if (!className.equals(other.className)) {
			return false;
		}
		if (methodName == null) {
			if (other.methodName != null) {
				return false;
			}
		} else if (!methodName.equals(other.methodName)) {
			return false;
		}
		if (nodes == null) {
			if (other.nodes != null) {
				return false;
			}
		} else if (!nodes.equals(other.nodes)) {
			return false;
		}
		return true;
	}

	public BytecodeInstruction get(int position) {
		return nodes.get(position);
	}

	public PrimePath getAppended(BytecodeInstruction node) {
		PrimePath copy = new PrimePath(className, methodName);
		copy.nodes.addAll(nodes);
		copy.append(node);
		return copy;
	}

	public BytecodeInstruction getLast() {
		return nodes.get(nodes.size() - 1);
	}

	public int getSize() {
		return nodes.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		return result;
	}

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
