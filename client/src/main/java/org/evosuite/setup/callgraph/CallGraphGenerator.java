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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.InheritanceTree;
import org.evosuite.utils.LoggingUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class CallGraphGenerator {

	private static Logger logger = LoggerFactory
			.getLogger(CallGraphGenerator.class);

	public static CallGraphImpl analyze(String className) {

		ClassNode targetClass = DependencyAnalysis.getClassNode(className);

		CallGraphImpl callgraph = new CallGraphImpl(className);
		if (targetClass != null)
			handle(callgraph, targetClass, 0);
		if (Properties.INSTRUMENT_PARENT) {
			handleSuperClasses(callgraph, targetClass);
		}
		return callgraph;
	}

	private static boolean isOverridden(String methodName) {
		return true;
	}

	/**
	 * If we want to have the calltree also for the superclasses, we need to
	 * determine which methods are callable
	 * 
	 * @param callTree
	 * @param targetClass
	 */
	@SuppressWarnings("unchecked")
	private static void handleSuperClasses(CallGraphImpl callGraph,
			ClassNode targetClass) {
		String superClassName = targetClass.superName;
		if (superClassName == null || superClassName.isEmpty())
			return;

		if (superClassName.equals("java/lang/Object"))
			return;

		logger.debug("Creating calltree for superclass: " + superClassName);
		ClassNode superClass = DependencyAnalysis.getClassNode(superClassName);
		List<MethodNode> methods = superClass.methods;
		for (MethodNode mn : methods) {
			logger.debug("Method: " + mn.name);

			// Do not check super-constructors
			if (mn.name.equals("<init>"))
				continue;
			if (mn.name.equals("<clinit>"))
				continue;

			// Skip abstract etc
			if ((mn.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT)
				continue;

			// Do not handle classes if they are overridden by the subclass
			if ((mn.access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
				if (!isOverridden(mn.name + mn.desc)) {
					handleMethodNode(callGraph, superClass, mn, 0);
				}
			}
		}
		handleSuperClasses(callGraph, superClass);

	}

	@SuppressWarnings("unchecked")
	private static void handle(CallGraphImpl callGraph, ClassNode targetClass,
			int depth) {
		List<MethodNode> methods = targetClass.methods;
		for (MethodNode mn : methods) {
			logger.debug("Method: " + mn.name);
			handleMethodNode(callGraph, targetClass, mn, depth);
		}
	}

	@SuppressWarnings("unchecked")
	private static void handle(CallGraphImpl callGraph, ClassNode targetClass,
			String methodName, int depth) {
		List<MethodNode> methods = targetClass.methods;
		for (MethodNode mn : methods) {
			if (methodName.equals(mn.name + mn.desc))
				handleMethodNode(callGraph, targetClass, mn, depth);
		}
	}

	private static void handle(CallGraphImpl callGraph, String className,
			String methodName, int depth) {
		ClassNode cn = DependencyAnalysis.getClassNode(className);
		if (cn == null)
			return;

		handle(callGraph, cn, methodName, depth);
	}

	/**
	 * Add all possible calls for a given method
	 * 
	 * @param callGraph
	 * @param mn
	 */
	@SuppressWarnings("unchecked")
	private static void handleMethodNode(CallGraphImpl callGraph, ClassNode cn,
			MethodNode mn, int depth) {
		handlePublicMethodNode(callGraph, cn, mn);

		InsnList instructions = mn.instructions;
		Iterator<AbstractInsnNode> iterator = instructions.iterator();

		// TODO: This really shouldn't be here but in its own class
		while (iterator.hasNext()) {
			AbstractInsnNode insn = iterator.next();
			if (insn instanceof MethodInsnNode) {
				handleMethodInsnNode(callGraph, cn, mn, (MethodInsnNode) insn,
						depth + 1);
			}
		}
	}

	private static void handlePublicMethodNode(CallGraphImpl callGraph,
			ClassNode cn, MethodNode mn) {
		if ((mn.access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
			callGraph.addPublicMethod(cn.name, mn.name + mn.desc);
		}
	}

	/**
	 * Descend into a call
	 * 
	 * @param callGraph
	 * @param mn
	 * @param methodCall
	 */
	private static void handleMethodInsnNode(CallGraphImpl callGraph,
			ClassNode cn, MethodNode mn, MethodInsnNode methodCall, int depth) {

		// Only build calltree for instrumentable classes
		if (BytecodeInstrumentation.checkIfCanInstrument(methodCall.owner
				.replaceAll("/", "."))) {
			logger.debug("Handling method: " + methodCall.name);
			if (!callGraph.hasCall(cn.name, mn.name + mn.desc,
					methodCall.owner, methodCall.name + methodCall.desc)) {

				// Add call from mn to methodCall to callgraph
				callGraph.addCall(cn.name, mn.name + mn.desc, methodCall.owner,
						methodCall.name + methodCall.desc);

				handle(callGraph, methodCall.owner, methodCall.name
						+ methodCall.desc, depth);
			}
		}
	}

	/**
	 * Update connections in the call tree according to the inheritance: For
	 * each connection, if the method is overridden in a subclass add a
	 * connection to the method in the subclass
	 * 
	 * @param callTree
	 * @param inheritanceTree
	 */
	static void update(CallGraphImpl callGraph, InheritanceTree inheritanceTree) {
		logger.info("Updating call tree ");

		Set<CallGraphEntry> subclassCalls = new LinkedHashSet<CallGraphEntry>();
		for (CallGraphEntry call : callGraph.getViewOfCurrentCalls()) {

			// target class, ma tanto è tutto al contrario
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
				// LoggingUtils.getEvoLogger().warn("Inheritance tree does not contain {}, please check classpath",
				// targetClass);
				continue;
			}

			// tutte le chiamate dalle altre classi a questo metodo
			for (CallGraphEntry c : callGraph.getCallsFrom(call)) {
				for (String subclass : inheritanceTree
						.getSubclasses(targetClass)) {
					if (inheritanceTree.isMethodDefined(subclass, targetMethod)) {
						callGraph.addCall(subclass, targetMethod,
								c.getClassName(), c.getMethodName());
					}
				}

			}
		}
	}

}
