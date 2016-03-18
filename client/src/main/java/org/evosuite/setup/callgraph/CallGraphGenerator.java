/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
/**
 * 
 */
package org.evosuite.setup.callgraph;

import java.util.*;

import org.evosuite.Properties;
import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.evosuite.instrumentation.ExceptionTransformationClassAdapter;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.InheritanceTree;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate the call graph, the class is a modification of the CallTreeGenerator
 * class.
 * 
 * @author mattia, Gordon Fraser
 * 
 */
public class CallGraphGenerator {
	
	private static Logger logger = LoggerFactory.getLogger(CallGraphGenerator.class);

	public static CallGraph analyze(String className) {
		ClassNode targetClass = DependencyAnalysis.getClassNode(className);
		CallGraph callgraph = new CallGraph(className);
		if (targetClass != null)
			handle(callgraph, targetClass, 0);
		if (Properties.INSTRUMENT_PARENT) {
			handleSuperClasses(callgraph, targetClass);
		}
		return callgraph;
	}

	public static CallGraph analyzeOtherClasses(CallGraph callgraph, String className) {
		ClassNode targetClass = DependencyAnalysis.getClassNode(className);

		if (targetClass != null)
			handle(callgraph, targetClass, 0);
		return callgraph;
	} 

	private static boolean isOverridden(String methodName) {
		return true;
	}

	/**
	 * If we want to have the calltree also for the superclasses, we need to
	 * determine which methods are callable
	 * 
	 * @param callGraph
	 * @param targetClass
	 */
	@SuppressWarnings("unchecked")
	private static void handleSuperClasses(CallGraph callGraph, ClassNode targetClass) {
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
	private static void handle(CallGraph callGraph, ClassNode targetClass, int depth) {
		List<MethodNode> methods = targetClass.methods;
		for (MethodNode mn : methods) {
			logger.debug("Method: " + mn.name);
			handleMethodNode(callGraph, targetClass, mn, depth);
		}
	}

	@SuppressWarnings("unchecked")
	private static void handle(CallGraph callGraph, ClassNode targetClass, String methodName,
			int depth) {
		List<MethodNode> methods = targetClass.methods;
		for (MethodNode mn : methods) {
			if (methodName.equals(mn.name + mn.desc))
				handleMethodNode(callGraph, targetClass, mn, depth);
		}
	}

	private static void handle(CallGraph callGraph, String className, String methodName, int depth) {
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
	private static void handleMethodNode(CallGraph callGraph, ClassNode cn, MethodNode mn, int depth) {
		handlePublicMethodNode(callGraph, cn, mn);

		// TODO: Integrate this properly - it is currently an unexpected side-effect
		if(!ExceptionTransformationClassAdapter.methodExceptionMap.containsKey(cn.name))
			ExceptionTransformationClassAdapter.methodExceptionMap.put(cn.name, new LinkedHashMap<>());

		String methodNameDesc = mn.name + mn.desc;
		Set<Type> exceptionTypes = new LinkedHashSet<>();
		if(mn.exceptions != null) {
			for (String exceptionName : ((List<String>)mn.exceptions)) {
				exceptionTypes.add(Type.getType(exceptionName));
			}
		}
		ExceptionTransformationClassAdapter.methodExceptionMap.get(cn.name).put(methodNameDesc, exceptionTypes);

		InsnList instructions = mn.instructions;
		Iterator<AbstractInsnNode> iterator = instructions.iterator();

		// TODO: This really shouldn't be here but in its own class
		while (iterator.hasNext()) {
			AbstractInsnNode insn = iterator.next();
			if (insn instanceof MethodInsnNode) {
				handleMethodInsnNode(callGraph, cn, mn, (MethodInsnNode) insn, depth + 1);
			}
		}
	}

	private static void handlePublicMethodNode(CallGraph callGraph, ClassNode cn, MethodNode mn) {
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
	private static void handleMethodInsnNode(CallGraph callGraph, ClassNode cn, MethodNode mn,
			MethodInsnNode methodCall, int depth) {

		// Only build calltree for instrumentable classes
		if (BytecodeInstrumentation.checkIfCanInstrument(methodCall.owner.replaceAll("/", "."))) {
			logger.debug("Handling method: " + methodCall.name);
			if (!callGraph.hasCall(cn.name, mn.name + mn.desc, methodCall.owner, methodCall.name
					+ methodCall.desc)) {

				// Add call from mn to methodCall to callgraph
				if (callGraph.addCall(cn.name, mn.name + mn.desc, methodCall.owner, methodCall.name
						+ methodCall.desc)) {

					handle(callGraph, methodCall.owner, methodCall.name + methodCall.desc, depth);
				}
			}
		}
	}

	public static void update(CallGraph callGraph, InheritanceTree inheritanceTree) {
		logger.info("Updating call tree ");

		for (CallGraphEntry call : callGraph.getViewOfCurrentMethods()) {

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
}
