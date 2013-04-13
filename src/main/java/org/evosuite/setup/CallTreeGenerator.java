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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.utils.LoggingUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
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
			handle(callTree, targetClass, 0);
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
					handleMethodNode(callTree, superClass, mn, 0);
				}
			}
		}
		handleSuperClasses(callTree, superClass);

	}

	@SuppressWarnings("unchecked")
	public static void handle(CallTree callTree, ClassNode targetClass, int depth) {
		List<MethodNode> methods = targetClass.methods;
		for (MethodNode mn : methods) {
			logger.debug("Method: " + mn.name);
			handleMethodNode(callTree, targetClass, mn, depth);
		}
	}

	@SuppressWarnings("unchecked")
	public static void handle(CallTree callTree, ClassNode targetClass,
	        String methodName, int depth) {
		List<MethodNode> methods = targetClass.methods;
		for (MethodNode mn : methods) {
			if (methodName.equals(mn.name + mn.desc))
				handleMethodNode(callTree, targetClass, mn, depth);
		}
	}

	public static void handle(CallTree callTree, String className, String methodName,
	        int depth) {
		ClassNode cn = DependencyAnalysis.getClassNode(className);
		if (cn == null)
			return;

		handle(callTree, cn, methodName, depth);
	}

	public static Set<Type> castClasses = new HashSet<Type>();

	public static Map<Type, Integer> castClassMap = new HashMap<Type, Integer>();

	/**
	 * Add all possible calls for a given method
	 * 
	 * @param callGraph
	 * @param mn
	 */
	@SuppressWarnings("unchecked")
	public static void handleMethodNode(CallTree callTree, ClassNode cn, MethodNode mn,
	        int depth) {
		handlePublicMethodNode(callTree, cn, mn);

		if (mn.signature != null) {
			logger.debug("Visiting signature: " + mn.signature);
			CollectParameterTypesVisitor visitor = new CollectParameterTypesVisitor(
			        cn.name);
			new SignatureReader(mn.signature).accept(visitor);
			castClasses.addAll(visitor.getClasses());
			for (Type castType : visitor.getClasses()) {
				if (!castClassMap.containsKey(castType)) {
					logger.debug("Adding new cast class from signature visitor: "
					        + castType);
					castClassMap.put(castType, depth + 1);
				}
			}
		}

		InsnList instructions = mn.instructions;
		Iterator<AbstractInsnNode> iterator = instructions.iterator();

		// TODO: This really shouldn't be here but in its own class
		while (iterator.hasNext()) {
			AbstractInsnNode insn = iterator.next();
			if (insn instanceof MethodInsnNode) {
				handleMethodInsnNode(callTree, cn, mn, (MethodInsnNode) insn, depth + 1);
			} else if (insn.getOpcode() == Opcodes.CHECKCAST) {
				TypeInsnNode typeNode = (TypeInsnNode) insn;
				Type castType = Type.getObjectType(typeNode.desc);
				while (castType.getSort() == Type.ARRAY) {
					castType = castType.getElementType();
				}
				logger.debug("Adding new cast class from cast: " + castType);
				castClasses.add(castType);
				if (!castClassMap.containsKey(castType))
					castClassMap.put(castType, depth+1);
			} else if (insn.getOpcode() == Opcodes.INSTANCEOF) {
				TypeInsnNode typeNode = (TypeInsnNode) insn;
				Type castType = Type.getObjectType(typeNode.desc);
				while (castType.getSort() == Type.ARRAY) {
					castType = castType.getElementType();
				}
				logger.debug("Adding new cast class from instanceof: " + castType);
				if (!castClassMap.containsKey(castType))
					castClassMap.put(castType, depth+1);
				castClasses.add(castType);
			} else if (insn.getOpcode() == Opcodes.LDC) {
				LdcInsnNode ldcNode = (LdcInsnNode) insn;
				if (ldcNode.cst instanceof Type) {
					Type type = (Type) ldcNode.cst;
					while (type.getSort() == Type.ARRAY) {
						type = type.getElementType();
					}
					if (!castClassMap.containsKey(type))
						castClassMap.put(type, depth+1);
					castClasses.add(type);
				}

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
	        MethodNode mn, MethodInsnNode methodCall, int depth) {

		// Only build calltree for instrumentable classes
		if (InstrumentingClassLoader.checkIfCanInstrument(methodCall.owner.replaceAll("/",
		                                                                              "."))) {
			logger.debug("Handling method: " + methodCall.name);
			if (!callTree.hasCall(cn.name, mn.name + mn.desc, methodCall.owner,
			                      methodCall.name + methodCall.desc)) {

				// Add call from mn to methodCall to callgraph
				callTree.addCall(cn.name, mn.name + mn.desc, methodCall.owner,
				                 methodCall.name + methodCall.desc);

				handle(callTree, methodCall.owner, methodCall.name + methodCall.desc,
				       depth);
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
