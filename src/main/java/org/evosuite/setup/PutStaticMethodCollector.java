package org.evosuite.setup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects a set of java.lang.reflect.Method of those 
 * classes in the callTree including an update to a static field
 * that is used in the GetStatic relation.
 * 
 * @author galeotti
 *
 */
public class PutStaticMethodCollector {

	public static class MethodIdentifier {
		private final String className;
		private final String methodName;
		private final String desc;

		public MethodIdentifier(String className, String methodName, String desc) {
			this.className = className;
			this.methodName = methodName;
			this.desc = desc;
		}
		
		public String toString() {
			return className + "." + methodName +  this.desc;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((className == null) ? 0 : className.hashCode());
			result = prime * result + ((desc == null) ? 0 : desc.hashCode());
			result = prime * result
					+ ((methodName == null) ? 0 : methodName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MethodIdentifier other = (MethodIdentifier) obj;
			if (className == null) {
				if (other.className != null)
					return false;
			} else if (!className.equals(other.className))
				return false;
			if (desc == null) {
				if (other.desc != null)
					return false;
			} else if (!desc.equals(other.desc))
				return false;
			if (methodName == null) {
				if (other.methodName != null)
					return false;
			} else if (!methodName.equals(other.methodName))
				return false;
			return true;
		}
	}

	private static Logger logger = LoggerFactory
			.getLogger(PutStaticMethodCollector.class);

	public Set<MethodIdentifier> collectMethods(String targetClassName) {

		this.targetClassName = targetClassName;

		Set<MethodIdentifier> methods = new HashSet<MethodIdentifier>();

		GetStaticGraph getStaticGraph = GetStaticGraphGenerator
				.generate(targetClassName);
		Map<String, Set<String>> requiredFields = getStaticGraph
				.getStaticFields();

		for (String calledClassName : requiredFields.keySet()) {
			ClassNode classNode = DependencyAnalysis
					.getClassNode(calledClassName);
			List<MethodNode> classMethods = classNode.methods;
			for (MethodNode mn : classMethods) {
				if (mn.name.equals("<clinit>"))
					continue;
				
				InsnList instructions = mn.instructions;
				Iterator<AbstractInsnNode> it = instructions.iterator();
				while (it.hasNext()) {
					AbstractInsnNode insn = it.next();
					if (insn instanceof FieldInsnNode) {
						FieldInsnNode fieldInsn = (FieldInsnNode) insn;
						if (fieldInsn.getOpcode() != Opcodes.PUTSTATIC) {
							continue;
						}
						String calleeClassName = fieldInsn.owner.replaceAll(
								"/", ".");
						String calleeFieldName = fieldInsn.name;

						if (contains(requiredFields, calleeClassName,
								calleeFieldName)) {

							MethodIdentifier methodIdentifier = new MethodIdentifier(
									calledClassName, mn.name, mn.desc);
							methods.add(methodIdentifier);

						}
					}
				}

			}

		}
		return methods;
	}

	private String targetClassName;

	private boolean contains(Map<String, Set<String>> fields, String className,
			String fieldName) {
		if (!fields.containsKey(className))
			return false;

		if (!fields.get(className).contains(fieldName))
			return false;

		return true;
	}

}
