package org.evosuite.setup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticMethodCollector {

	private static Logger logger = LoggerFactory
			.getLogger(StaticMethodCollector.class);

	public void collectStaticMethods(String classNameWithDots,
			Set<String> fieldNames) {
		ClassNode targetClass = DependencyAnalysis
				.getClassNode(classNameWithDots);
		List<MethodNode> methods = targetClass.methods;

		List<MethodNode> modifiesStaticFieldMethod = new ArrayList<MethodNode>();
		for (MethodNode mn : methods) {
			logger.debug("Method: " + mn.name);

			InsnList instructions = mn.instructions;
			Iterator<AbstractInsnNode> iterator = instructions.iterator();
			while (iterator.hasNext()) {
				AbstractInsnNode insn = iterator.next();

				if (insn instanceof FieldInsnNode) {
					FieldInsnNode fieldInsn = (FieldInsnNode) insn;
					if (fieldInsn.getOpcode() == Opcodes.PUTSTATIC) {

						String targetClassname = fieldInsn.owner.replace("/",
								".");
						String targetMethodname = fieldInsn.name;

						if (classNameWithDots.equals(targetClassname)
								&& fieldNames.contains(targetMethodname)) {
							modifiesStaticFieldMethod.add(mn);
						}

					}
				}

			}

		}
	}

}
