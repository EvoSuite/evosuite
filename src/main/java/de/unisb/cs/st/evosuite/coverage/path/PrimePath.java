/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.path;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

/**
 * @author Gordon Fraser
 * 
 */
public class PrimePath {

	List<CFGVertex> nodes = new ArrayList<CFGVertex>();

	class PathEntry {
		CFGVertex vertex;
		boolean value;
	}

	List<PathEntry> branches = new ArrayList<PathEntry>();

	String className;

	String methodName;

	public PrimePath(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
	}

	public CFGVertex getLast() {
		return nodes.get(nodes.size() - 1);
	}

	public void append(CFGVertex node) {
		nodes.add(node);
	}

	public PrimePath getAppended(CFGVertex node) {
		PrimePath copy = new PrimePath(className, methodName);
		copy.nodes.addAll(nodes);
		copy.append(node);
		return copy;
	}

	public boolean contains(CFGVertex vertex) {
		return nodes.contains(vertex);
	}

	public void condensate() {
		for (int position = 0; position < nodes.size(); position++) {
			CFGVertex node = nodes.get(position);
			if (node.isBranch() && position < (nodes.size() - 1)) {
				PathEntry entry = new PathEntry();
				entry.vertex = node;
				if (nodes.get(position + 1).getID() == (node.getID() + 1)) {
					entry.value = false;
				} else {
					entry.value = true;
				}
				branches.add(entry);
			}
		}

	}

	public int getSize() {
		return nodes.size();
	}

	public CFGVertex get(int position) {
		return nodes.get(position);
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < nodes.size(); i++) {
			builder.append(nodes.get(i).getID());
			builder.append(" ");
		}
		return builder.toString();
	}

}
