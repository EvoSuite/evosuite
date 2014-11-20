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
package org.evosuite.setup.callgraph;

import java.util.concurrent.Callable;

import org.evosuite.setup.InheritanceTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate the call graph, the class is a modification of the CallTreeGenerator
 * class.
 * 
 * @author mattia, Gordon Fraser
 * 
 */
public class CallGraphUpdater implements Callable<Boolean> {

	private static Logger logger = LoggerFactory.getLogger(CallGraphUpdater.class);

	private final CallGraph callGraph;
	private final Iterable<CallGraphEntry> s;
	private final InheritanceTree inheritanceTree;

	public CallGraphUpdater(CallGraph callGraph, Iterable<CallGraphEntry> s,
			InheritanceTree inheritanceTree) {
		this.callGraph = callGraph;
		this.s = s;
		this.inheritanceTree = inheritanceTree;
	}

	/**
	 * Update connections in the call tree according to the inheritance: For
	 * each connection, if the method is overridden in a subclass add a
	 * connection to the method in the subclass
	 * 
	 * @param callTree
	 * @param inheritanceTree
	 */ 
	public void update(CallGraph callGraph, Iterable<CallGraphEntry> s, InheritanceTree inheritanceTree) {
		logger.info("Updating call tree ");

		for (CallGraphEntry call : s) {

			String targetClass = call.getClassName();
			String targetMethod = call.getMethodName();

			// Ignore constructors
			if (targetMethod.startsWith("<init>"))
				continue;
			// Ignore calls to Array (e.g. clone())
			if (targetClass.startsWith("["))
				continue;
			if (!inheritanceTree.hasClass(targetClass)) {
				// Private classes are not in the inheritance tree
				continue;
			}

			// update graph
			for (CallGraphEntry c : callGraph.getCallsFromMethod(call)) {
				for (String subclass : inheritanceTree.getSubclasses(targetClass)) {
					if (inheritanceTree.isMethodDefined(subclass, targetMethod)) {
						callGraph.addCall(c.getClassName(), c.getMethodName(), subclass,
								targetMethod);
					}
				}
			}
		}
	}



	@Override
	public Boolean call() throws Exception {
		update(callGraph, s, inheritanceTree);
		return true;
	}
}
