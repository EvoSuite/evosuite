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
/**
 * 
 */
package org.evosuite.setup;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.classpath.ResourceList;
import org.evosuite.utils.LoggingUtils;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 * @author Gordon Fraser
 * 
 */
public class InheritanceTree {

	private final Map<String, Set<String>> subclassCache = new LinkedHashMap<String, Set<String>>();

	private DirectedMultigraph<String, DefaultEdge> inheritanceGraph = new DirectedMultigraph<String, DefaultEdge>(
	        DefaultEdge.class);

	public boolean isMethodDefined(String className, String methodName) {
		return false;
	}

	public void addSuperclass(String className, String superName, int access) {
		String classNameWithDots = ResourceList.getClassNameFromResourcePath(className);
		String superNameWithDots = ResourceList.getClassNameFromResourcePath(superName);

		if (inheritanceGraph == null) {
			inheritanceGraph = new DirectedMultigraph<String, DefaultEdge>(
			        DefaultEdge.class);
		}

		inheritanceGraph.addVertex(classNameWithDots);
		inheritanceGraph.addVertex(superNameWithDots);
		inheritanceGraph.addEdge(superNameWithDots, classNameWithDots);
	}

	public void addInterface(String className, String interfaceName) {
		String classNameWithDots = ResourceList.getClassNameFromResourcePath(className);
		String interfaceNameWithDots = ResourceList.getClassNameFromResourcePath(interfaceName);

		inheritanceGraph.addVertex(classNameWithDots);
		inheritanceGraph.addVertex(interfaceNameWithDots);
		inheritanceGraph.addEdge(interfaceNameWithDots, classNameWithDots);
	}

	public Set<String> getSubclasses(String className) {
		String classNameWithDots = ResourceList.getClassNameFromResourcePath(className);

		if (subclassCache.containsKey(classNameWithDots))
			return subclassCache.get(classNameWithDots);

		if (!inheritanceGraph.containsVertex(classNameWithDots)) {
			LoggingUtils.getEvoLogger().warn("Class not in inheritance graph: "
			                                         + classNameWithDots);
		}
		Set<String> result = new LinkedHashSet<String>();
		BreadthFirstIterator<String, DefaultEdge> bfi = new BreadthFirstIterator<String, DefaultEdge>(
		        inheritanceGraph, classNameWithDots);
		while (bfi.hasNext()) {
			result.add(bfi.next());
		}
		subclassCache.put(classNameWithDots, result);
		return result;
	}

	public Set<String> getSuperclasses(String className) {
		String classNameWithDots = ResourceList.getClassNameFromResourcePath(className);
		EdgeReversedGraph<String, DefaultEdge> reverseGraph = new EdgeReversedGraph<String, DefaultEdge>(
		        inheritanceGraph);
		Set<String> result = new LinkedHashSet<String>();
		BreadthFirstIterator<String, DefaultEdge> bfi = new BreadthFirstIterator<String, DefaultEdge>(
		        reverseGraph, classNameWithDots);
		while (bfi.hasNext()) {
			result.add(bfi.next());
		}
		return result;
	}

	public Set<String> getAllClasses() {
		return inheritanceGraph.vertexSet();
	}

	public void removeClass(String className) {
		inheritanceGraph.removeVertex(className);
	}

	public boolean hasClass(String className) {
		return inheritanceGraph.containsVertex(className);
	}

	public int getNumClasses() {
		return inheritanceGraph.vertexSet().size();
	}

}
