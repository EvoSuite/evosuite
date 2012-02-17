/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author Gordon Fraser
 * 
 */
public class MethodNodeTransformer {

	public void transform(MethodNode mn) {

		Set<AbstractInsnNode> originalNodes = new HashSet<AbstractInsnNode>();
		AbstractInsnNode node = mn.instructions.getFirst();
		while (node != mn.instructions.getLast()) {
			originalNodes.add(node);
			node = node.getNext();
		}

		node = mn.instructions.getFirst();
		while (node != mn.instructions.getLast()) {
			if (!originalNodes.contains(node)) {
				// Only transform nodes present in original method
			} else if (node instanceof MethodInsnNode) {
				node = transformMethodInsnNode(mn, (MethodInsnNode) node);
			} else if (node instanceof VarInsnNode) {
				node = transformVarInsnNode(mn, (VarInsnNode) node);
			} else if (node instanceof FieldInsnNode) {
				node = transformFieldInsnNode(mn, (FieldInsnNode) node);
			} else if (node instanceof InsnNode) {
				node = transformInsnNode(mn, (InsnNode) node);
			} else if (node instanceof TypeInsnNode) {
				node = transformTypeInsnNode(mn, (TypeInsnNode) node);
			} else if (node instanceof JumpInsnNode) {
				node = transformJumpInsnNode(mn, (JumpInsnNode) node);
			} else if (node instanceof LabelNode) {
				node = transformLabelNode(mn, (LabelNode) node);
			} else if (node instanceof IntInsnNode) {
				node = transformIntInsnNode(mn, (IntInsnNode) node);
			} else if (node instanceof MultiANewArrayInsnNode) {
				node = transformMultiANewArrayInsnNode(mn, (MultiANewArrayInsnNode) node);
			}

			node = node.getNext();
		}
	}

	protected AbstractInsnNode transformMethodInsnNode(MethodNode mn,
	        MethodInsnNode methodNode) {
		return methodNode;
	}

	protected AbstractInsnNode transformVarInsnNode(MethodNode mn, VarInsnNode varNode) {
		return varNode;
	}

	protected AbstractInsnNode transformFieldInsnNode(MethodNode mn,
	        FieldInsnNode fieldNode) {
		return fieldNode;
	}

	protected AbstractInsnNode transformInsnNode(MethodNode mn, InsnNode insnNode) {
		return insnNode;
	}

	protected AbstractInsnNode transformTypeInsnNode(MethodNode mn, TypeInsnNode typeNode) {
		return typeNode;
	}

	protected AbstractInsnNode transformJumpInsnNode(MethodNode mn, JumpInsnNode jumpNode) {
		return jumpNode;
	}

	protected AbstractInsnNode transformLabelNode(MethodNode mn, LabelNode labelNode) {
		return labelNode;
	}

	protected AbstractInsnNode transformIntInsnNode(MethodNode mn, IntInsnNode intInsnNode) {
		return intInsnNode;
	}

	protected AbstractInsnNode transformMultiANewArrayInsnNode(MethodNode mn,
	        MultiANewArrayInsnNode arrayInsnNode) {
		return arrayInsnNode;
	}
}
