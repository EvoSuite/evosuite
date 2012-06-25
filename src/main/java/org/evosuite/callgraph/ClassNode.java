/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.callgraph;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassNode implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;

	private List<ClassNode> supers;

	private final Set<ClassNode> subclasses = new HashSet<ClassNode>();

	public ClassNode(String name) {
		this.name = name;
	}

	public Set<String> getAllSupers() {
		Set<String> result = new HashSet<String>();
		if (supers != null) {
			for (ClassNode cn : supers) {
				result.add(cn.getName());
				result.addAll(cn.getAllSupers());
			}
		}
		return result;
	}

	public Set<ClassNode> getAllSuperNodes() {
		Set<ClassNode> result = new HashSet<ClassNode>();
		if (supers != null) {
			for (ClassNode cn : supers) {
				result.add(cn);
				result.addAll(cn.getAllSuperNodes());
			}
		}
		return result;
	}

	public String getName() {
		return name;
	}

	public List<ClassNode> getSupers() {
		return supers;
	}

	public void setSupers(List<ClassNode> supers) {
		this.supers = supers;
	}

	public Set<String> getAllSubclasses() {
		Set<String> result = new HashSet<String>();
		if (subclasses != null) {
			for (ClassNode cn : subclasses) {
				result.add(cn.getName());
				result.addAll(cn.getAllSubclasses());
			}
		}
		return result;
	}

	public Set<ClassNode> getSubclasses() {
		return subclasses;
	}

	public void addSubclass(ClassNode subclass) {
		this.subclasses.add(subclass);
	}
}
