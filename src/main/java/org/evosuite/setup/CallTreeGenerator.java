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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.javaagent.InstrumentingClassLoader;
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
public class CallTreeGenerator {

	private static Logger logger = LoggerFactory.getLogger(CallTreeGenerator.class);

	public static CallTree analyze(String className) {
		ClassNode targetClass = DependencyAnalysis.getClassNode(className);

		CallTree callTree = new CallTree(className);
		if (targetClass != null)
			handle(callTree, targetClass);
		if (Properties.INSTRUMENT_PARENT) {
			handleSuperClasses(callTree, targetClass);
		}
		return callTree;
	}

	public static boolean isOverridden(String methodName) {
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
	public static void handleSuperClasses(CallTree callTree, ClassNode targetClass) {
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
					handleMethodNode(callTree, superClass, mn);
				}
			}
		}
		handleSuperClasses(callTree, superClass);

	}

	@SuppressWarnings("unchecked")
	public static void handle(CallTree callTree, ClassNode targetClass) {
		List<MethodNode> methods = targetClass.methods;
		for (MethodNode mn : methods) {
			logger.debug("Method: " + mn.name);
			handleMethodNode(callTree, targetClass, mn);
		}
	}

	@SuppressWarnings("unchecked")
	public static void handle(CallTree callTree, ClassNode targetClass, String methodName) {
		List<MethodNode> methods = targetClass.methods;
		for (MethodNode mn : methods) {
			if (methodName.equals(mn.name + mn.desc))
				handleMethodNode(callTree, targetClass, mn);
		}
	}

	public static void handle(CallTree callTree, String className, String methodName) {
		ClassNode cn = DependencyAnalysis.getClassNode(className);
		if (cn == null)
			return;

		handle(callTree, cn, methodName);
	}

	/**
	 * Add all possible calls for a given method
	 * 
	 * @param callGraph
	 * @param mn
	 */
	@SuppressWarnings("unchecked")
	public static void handleMethodNode(CallTree callTree, ClassNode cn, MethodNode mn) {
		handlePublicMethodNode(callTree, cn, mn);

		InsnList instructions = mn.instructions;
		Iterator<AbstractInsnNode> iterator = instructions.iterator();

		while (iterator.hasNext()) {
			AbstractInsnNode insn = iterator.next();
			if (insn instanceof MethodInsnNode) {
				handleMethodInsnNode(callTree, cn, mn, (MethodInsnNode) insn);
			}
		}
	}

	private static void handlePublicMethodNode(CallTree callTree, ClassNode cn,
	        MethodNode mn) {
		if ((mn.access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
			callTree.addPublicMethod(cn.name, mn.name + mn.desc);
		}
	}

	/**
	 * Descend into a call
	 * 
	 * @param callGraph
	 * @param mn
	 * @param methodCall
	 */
	public static void handleMethodInsnNode(CallTree callTree, ClassNode cn,
	        MethodNode mn, MethodInsnNode methodCall) {

		// Only build calltree for instrumentable classes
		if (InstrumentingClassLoader.checkIfCanInstrument(methodCall.owner.replaceAll("/",
		                                                                              "."))) {
			logger.debug("Handling method: " + methodCall.name);
			if (!callTree.hasCall(cn.name, mn.name + mn.desc, methodCall.owner,
			                      methodCall.name + methodCall.desc)) {

				// Add call from mn to methodCall to callgraph
				callTree.addCall(cn.name, mn.name + mn.desc, methodCall.owner,
				                 methodCall.name + methodCall.desc);

				handle(callTree, methodCall.owner, methodCall.name + methodCall.desc);
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
	public static void update(CallTree callTree, InheritanceTree inheritanceTree) {
		logger.info("Updating call tree ");

		Set<CallTreeEntry> subclassCalls = new LinkedHashSet<CallTreeEntry>();
		for (CallTreeEntry call : callTree) {
			String targetClass = call.getTargetClass();
			String targetMethod = call.getTargetMethod();

			// Ignore constructors
			if (targetMethod.startsWith("<init>"))
				continue;

			// Ignore calls to Array (e.g. clone())
			if (targetClass.startsWith("["))
				continue;

			if (!inheritanceTree.hasClass(targetClass)) {
				LoggingUtils.getEvoLogger().warn("Inheritance tree does not contain {}, please check classpath",
				                                 targetClass);
				continue;
			}

			for (String subclass : inheritanceTree.getSubclasses(targetClass)) {
				if (inheritanceTree.isMethodDefined(subclass, targetMethod)) {
					subclassCalls.add(new CallTreeEntry(call.getSourceClass(),
					        call.getSourceMethod(), subclass, targetMethod));
				}
			}
		}
		callTree.addCalls(subclassCalls);
	}

}
