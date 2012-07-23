
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
 *
 * @author Gordon Fraser
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

	/**
	 * <p>Constructor for ClassNode.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public ClassNode(String name) {
		this.name = name;
	}

	/**
	 * <p>getAllSupers</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
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

	/**
	 * <p>getAllSuperNodes</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
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

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return name;
	}

	/**
	 * <p>Getter for the field <code>supers</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<ClassNode> getSupers() {
		return supers;
	}

	/**
	 * <p>Setter for the field <code>supers</code>.</p>
	 *
	 * @param supers a {@link java.util.List} object.
	 */
	public void setSupers(List<ClassNode> supers) {
		this.supers = supers;
	}

	/**
	 * <p>getAllSubclasses</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
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

	/**
	 * <p>Getter for the field <code>subclasses</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<ClassNode> getSubclasses() {
		return subclasses;
	}

	/**
	 * <p>addSubclass</p>
	 *
	 * @param subclass a {@link org.evosuite.callgraph.ClassNode} object.
	 */
	public void addSubclass(ClassNode subclass) {
		this.subclasses.add(subclass);
	}
}
