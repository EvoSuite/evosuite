/*
 * Copyright (C) 2011 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Javalanche is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * Javalanche. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.callgraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.callgraph.DistanceTransformer.ClassEntry;
import de.unisb.cs.st.evosuite.utils.Utils;

public class Hierarchy implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.getLogger(Hierarchy.class);

	private final Map<String, ClassNode> hierarchyMap;

	private final Set<String> notFound = new HashSet<String>();

	private Hierarchy(Map<String, ClassNode> hierarchyMap) {
		this.hierarchyMap = hierarchyMap;
	}

	public Set<String> getAllClasses() {
		return hierarchyMap.keySet();
	}

	public Set<String> getAllSupers(String className) {
		ClassNode classNode = hierarchyMap.get(className);
		if (classNode == null) {
			if (!notFound.contains(className)) {
				logger.warn("Did not find class node " + className);
				notFound.add(className);
			}
			return Collections.emptySet();
		}
		return classNode.getAllSupers();
	}

	public static Hierarchy fromSet(Set<ClassEntry> entries) {
		Map<String, ClassNode> result = new HashMap<String, ClassNode>();
		for (ClassEntry classEntry : entries) {
			ClassNode cn = result.get(classEntry.getName());
			if (cn == null) {
				cn = new ClassNode(classEntry.getName());
				result.put(classEntry.getName(), cn);
			}
			List<ClassNode> supers = new ArrayList<ClassNode>();
			for (String s : classEntry.getSupers()) {
				ClassNode classNode = result.get(s);
				if (classNode == null) {
					classNode = new ClassNode(s);
					result.put(s, classNode);
				}
				supers.add(classNode);
			}
			cn.setSupers(supers);
		}
		return new Hierarchy(result);
	}

	public void calculateSubclasses() {
		for (ClassNode cn : hierarchyMap.values()) {
			for (ClassNode supercn : cn.getAllSuperNodes()) {
				supercn.addSubclass(cn);
				//logger.info(cn.getName()+" is an instantiation of "+supercn.getName());
			}
		}
	}

	public Set<String> getAllSubclasses(String className) {
		ClassNode classNode = hierarchyMap.get(className);
		if (classNode == null) {
			if (!notFound.contains(className)) {
				logger.warn("Did not find class node " + className);
				notFound.add(className);
			}
			return Collections.emptySet();
		}
		return classNode.getAllSubclasses();
	}

	public static Hierarchy readFromDefaultLocation() {
		Set<ClassEntry> entries = Utils.readXML(Properties.OUTPUT_DIR + "/"
		        + Properties.HIERARCHY_DATA);
		return fromSet(entries);
	}

}
