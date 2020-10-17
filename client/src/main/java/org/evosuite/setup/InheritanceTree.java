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

package org.evosuite.setup;

import java.io.File;
import java.util.*;

import org.evosuite.classpath.ResourceList;
import org.evosuite.utils.LoggingUtils;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class InheritanceTree {

    private static final Logger logger = LoggerFactory.getLogger(InheritanceTree.class);

	private final Map<String, Set<String>> subclassCache = new LinkedHashMap<>();

	private  Set<String> interfacesSet = new LinkedHashSet<>();
	private  Set<String> abstractClassesSet = new LinkedHashSet<>();

	private Map<String, Set<String>> analyzedMethods;
		
	private DirectedMultigraph<String, DefaultEdge> inheritanceGraph = new DirectedMultigraph<>(
			DefaultEdge.class);

	private void initialiseMap(){
		if (analyzedMethods == null)
			analyzedMethods = new LinkedHashMap<>();
		if(interfacesSet==null)
			interfacesSet = new LinkedHashSet<>();
		if(abstractClassesSet ==null)
			abstractClassesSet = new LinkedHashSet<>();
	}
	
	public boolean isClassDefined(String className){
		initialiseMap();
		return analyzedMethods.containsKey(className);
	}
	
	public boolean isInterface(String classname) {
		return interfacesSet.contains(classname);
	}
	
	public boolean isAbstractClass(String classname) {
		return abstractClassesSet.contains(classname);
	}
	
	public void registerAbstractClass(String abstractClassName) {
		initialiseMap();
		abstractClassesSet.add(ResourceList.getClassNameFromResourcePath(abstractClassName));
	}
	
	public void registerInterface(String interfaceName) {
		initialiseMap();
		interfacesSet.add(ResourceList.getClassNameFromResourcePath(interfaceName));
	}
	
	public boolean isMethodDefined(String className, String methodNameWdescriptor) {
		initialiseMap();
		
		if(analyzedMethods.get(className)==null) return false;
		return analyzedMethods.get(className).contains(methodNameWdescriptor);
	}
	
	public boolean isMethodDefined(String className, String methodName, String descriptor) {
		initialiseMap();
		
		if(analyzedMethods.get(className)==null) return false;
		return analyzedMethods.get(className).contains(methodName+descriptor);
	}
	
	//TODO the initialization in the clinit dosen't work, no idea why - mattia
	public void addAnalyzedMethod(String classname, String methodname, String descriptor) {
		initialiseMap();
		classname = classname.replace(File.separator, ".");
		Set<String> tmp = analyzedMethods.get(classname);
		if(tmp==null)
			analyzedMethods.put(classname, tmp = new LinkedHashSet<>());
		tmp.add(methodname+descriptor);
	}
	
	

	public void addSuperclass(String className, String superName, int access) {
		String classNameWithDots = ResourceList.getClassNameFromResourcePath(className);
		String superNameWithDots = ResourceList.getClassNameFromResourcePath(superName);

		if (inheritanceGraph == null) {
			inheritanceGraph = new DirectedMultigraph<>(
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
		interfacesSet.add(interfaceNameWithDots);
	}

	public Set<String> getSubclasses(String className) {
		String classNameWithDots = ResourceList.getClassNameFromResourcePath(className);

		if (subclassCache.containsKey(classNameWithDots))
			return subclassCache.get(classNameWithDots);

		if (!inheritanceGraph.containsVertex(classNameWithDots)) {
            LoggingUtils.logWarnAtMostOnce(logger, "Class not in inheritance graph: " + classNameWithDots);
			return new LinkedHashSet<>();
		}

		// TreeSet so that classes are sorted by name and thus deterministic across platforms
		Set<String> result = new TreeSet<>();
		BreadthFirstIterator<String, DefaultEdge> bfi = new BreadthFirstIterator<>(
				inheritanceGraph, classNameWithDots);
		while (bfi.hasNext()) {
			result.add(bfi.next());
		}
		subclassCache.put(classNameWithDots, result);
		return result;
	}

	public Set<String> getSuperclasses(String className) {
		String classNameWithDots = ResourceList.getClassNameFromResourcePath(className);
		if (!inheritanceGraph.containsVertex(classNameWithDots)) {
			LoggingUtils.logWarnAtMostOnce(logger, "Class not in inheritance graph: " + classNameWithDots);
			return new LinkedHashSet<>();
		}
		EdgeReversedGraph<String, DefaultEdge> reverseGraph = new EdgeReversedGraph<>(
				inheritanceGraph);

		// TreeSet so that classes are sorted by name and thus deterministic across platforms
		Set<String> result = new TreeSet<>();
		BreadthFirstIterator<String, DefaultEdge> bfi = new BreadthFirstIterator<>(
				reverseGraph, classNameWithDots);
		while (bfi.hasNext()) {
			result.add(bfi.next());
		}
		return result;
	}
	
	public List<String> getOrderedSuperclasses(String className) {
		String classNameWithDots = ResourceList.getClassNameFromResourcePath(className);
		if (!inheritanceGraph.containsVertex(classNameWithDots)) {
			LoggingUtils.logWarnAtMostOnce(logger, "Class not in inheritance graph: " + classNameWithDots);
			return new LinkedList<>();
		}
		EdgeReversedGraph<String, DefaultEdge> reverseGraph = new EdgeReversedGraph<>(
				inheritanceGraph);
		List<String> orderedList = new LinkedList<>();
		BreadthFirstIterator<String, DefaultEdge> bfi = new BreadthFirstIterator<>(
				reverseGraph, classNameWithDots);
		while (bfi.hasNext()) {
			orderedList.add(bfi.next());
		}
		return orderedList;
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
