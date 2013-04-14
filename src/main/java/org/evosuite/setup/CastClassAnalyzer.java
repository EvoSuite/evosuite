package org.evosuite.setup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.evosuite.Properties;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CastClassAnalyzer {

	private static final Logger logger = LoggerFactory.getLogger(CastClassAnalyzer.class);
	
	private Map<Type, Integer> castClassMap = new HashMap<Type, Integer>();

	public Map<Type,Integer> analyze(String className) {
		ClassNode targetClass = DependencyAnalysis.getClassNode(className);

		CallTree callTree = new CallTree(className);
		if (targetClass != null)
			handle(callTree, targetClass, 0);
		if (Properties.INSTRUMENT_PARENT) {
			handleSuperClasses(callTree, targetClass);
		}
		return castClassMap;
	}

	/**
	 * If we want to have the calltree also for the superclasses, we need to
	 * determine which methods are callable
	 * 
	 * @param callTree
	 * @param targetClass
	 */
	@SuppressWarnings("unchecked")
	public void handleSuperClasses(CallTree callTree, ClassNode targetClass) {
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
				handleMethodNode(callTree, superClass, mn, 0);
			}
		}
		handleSuperClasses(callTree, superClass);

	}
	
	private void handleClassSignature(ClassNode cn) {
		CollectParameterTypesVisitor visitor = new CollectParameterTypesVisitor(
				cn.name);
		if(cn.signature != null) {
			new SignatureReader(cn.signature).accept(visitor);
			for (Type castType : visitor.getClasses()) {
				if (!castClassMap.containsKey(castType)) {
					logger.debug("Adding new cast class from signature visitor: "
							+ castType);
					castClassMap.put(castType, 1);
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	public  void handle(CallTree callTree, ClassNode targetClass, int depth) {
		handleClassSignature(targetClass);

		List<MethodNode> methods = targetClass.methods;
		for (MethodNode mn : methods) {
			logger.debug("Method: " + mn.name);
			handleMethodNode(callTree, targetClass, mn, depth);
		}
	}

	@SuppressWarnings("unchecked")
	public void handle(CallTree callTree, ClassNode targetClass,
	        String methodName, int depth) {
		handleClassSignature(targetClass);
		List<MethodNode> methods = targetClass.methods;
		for (MethodNode mn : methods) {
			if (methodName.equals(mn.name + mn.desc))
				handleMethodNode(callTree, targetClass, mn, depth);
		}
	}

	public void handle(CallTree callTree, String className, String methodName,
	        int depth) {
		ClassNode cn = DependencyAnalysis.getClassNode(className);
		if (cn == null)
			return;

		handle(callTree, cn, methodName, depth);
	}

	/**
	 * Add all possible calls for a given method
	 * 
	 * @param callGraph
	 * @param mn
	 */
	@SuppressWarnings("unchecked")
	public  void handleMethodNode(CallTree callTree, ClassNode cn, MethodNode mn,
	        int depth) {

		if (mn.signature != null) {
			logger.debug("Visiting signature: " + mn.signature);
			CollectParameterTypesVisitor visitor = new CollectParameterTypesVisitor(
			        cn.name);
			new SignatureReader(mn.signature).accept(visitor);
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
			if (insn.getOpcode() == Opcodes.CHECKCAST) {
				TypeInsnNode typeNode = (TypeInsnNode) insn;
				Type castType = Type.getObjectType(typeNode.desc);
				while (castType.getSort() == Type.ARRAY) {
					castType = castType.getElementType();
				}
				logger.debug("Adding new cast class from cast: " + castType);
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
			} else if (insn.getOpcode() == Opcodes.LDC) {
				LdcInsnNode ldcNode = (LdcInsnNode) insn;
				if (ldcNode.cst instanceof Type) {
					Type type = (Type) ldcNode.cst;
					while (type.getSort() == Type.ARRAY) {
						type = type.getElementType();
					}
					if (!castClassMap.containsKey(type))
						castClassMap.put(type, depth+1);
				}

			}
		}
	}

}
