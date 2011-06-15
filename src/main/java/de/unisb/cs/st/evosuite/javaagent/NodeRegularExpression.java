/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

/**
 * @author Gordon Fraser
 * 
 */
public class NodeRegularExpression {

	protected static final int ICONST_0 = 3;

	protected static final int ICONST_1 = 4;

	protected static final int GOTO = 167;

	protected static final int ACONST_NULL = 1;

	protected static final int ACONST_M1 = 2;

	protected static final int ILOAD = 21;

	protected static final int ISTORE = 54;

	protected static final int IADD = 96;

	protected static final int ISUB = 100;

	protected static final int IMUL = 104;

	protected static final int IDIV = 108;

	protected static final int IREM = 112;

	protected static final int INEG = 116;

	protected static final int IFEQ = 153;

	protected static final int IFNE = 154;

	protected static final int IFLT = 155;

	protected static final int IFGE = 156;

	protected static final int IFGT = 157;

	protected static final int IFLE = 158;

	protected static final int IF_ICMPEQ = 159;

	protected static final int IF_ICMPNE = 160;

	protected static final int IF_ICMPLT = 161;

	protected static final int IF_ICMPGE = 162;

	protected static final int IF_ICMPGT = 163;

	protected static final int IF_ICMPLE = 164;

	protected static final int IF_ACMPEQ = 165;

	protected static final int IF_ACMPNE = 166;

	protected static final int IRETURN = 172;

	protected static final int GETSTATIC = 178;

	protected static final int PUTSTATIC = 179;

	protected static final int GETFIELD = 180;

	protected static final int PUTFIELD = 181;

	protected static final int INVOKEVIRTUAL = 182;

	protected static final int INVOKESPECIAL = 183;

	protected static final int INVOKESTATIC = 184;

	protected static final int INVOKEINTERFACE = 185;

	protected static final int NEWARRAY = 188;

	protected static final int INSTANCEOF = 193;

	protected static final int IFNULL = 198;

	protected static final int IFNONNULL = 199;

	protected static final int[] ALOAD = new int[] { 25, 42, 43, 44, 45 };

	protected static final int[] IF = new int[] { 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164 };

	protected static final int[] BOOL = new int[] { ICONST_0, ICONST_1 };

	// public static NodeRegularExpression IFELSE = new
	// NodeRegularExpression(new int[] {
	// 160, 4, 167, 3 });

	public static NodeRegularExpression IFELSE = new NodeRegularExpression(new int[][] { IF, BOOL, { GOTO }, BOOL });

	public static NodeRegularExpression STOREFLAG = new NodeRegularExpression(new int[][] { IF, BOOL, { ISTORE } });

	public static NodeRegularExpression STOREFLAG2 = new NodeRegularExpression(new int[][] { IF, BOOL, { PUTSTATIC } });

	public static NodeRegularExpression STOREFLAG3 = new NodeRegularExpression(new int[][] { IF, ALOAD, BOOL,
			{ PUTFIELD } });

	public static NodeRegularExpression STOREFLAG4 = new NodeRegularExpression(new int[][] { IF, BOOL, { IRETURN } });

	public final int[][] pattern;

	public NodeRegularExpression(int[] opcodes) {
		this.pattern = new int[opcodes.length][];
		for (int i = 0; i < opcodes.length; i++) {
			this.pattern[i] = new int[] { opcodes[i] };
		}
	}

	public NodeRegularExpression(int[][] opcodes) {
		this.pattern = opcodes;
	}

	public AbstractInsnNode getNextMatch(AbstractInsnNode start, InsnList instructions) {
		int match = 0;

		AbstractInsnNode node = start;
		AbstractInsnNode startNode = start;
		while (node != instructions.getLast()) {
			if ((node.getType() == AbstractInsnNode.FRAME) || (node.getType() == AbstractInsnNode.LABEL)
					|| (node.getType() == AbstractInsnNode.LINE)) {
				node = node.getNext();
				continue;
			} else {
				boolean found = false;
				for (int opcode : pattern[match]) {
					if (node.getOpcode() == opcode) {
						if (match == 0) {
							startNode = node;
						}
						match++;
						found = true;
						break;
					}
				}
				if (!found) {
					match = 0;
				}
			}
			if (match == pattern.length) {
				return startNode;
			}

			node = node.getNext();
		}

		return null;

	}

	public boolean matches(InsnList instructions) {
		int match = 0;

		AbstractInsnNode node = instructions.getFirst();
		while (node != instructions.getLast()) {
			if ((node.getType() == AbstractInsnNode.FRAME) || (node.getType() == AbstractInsnNode.LABEL)
					|| (node.getType() == AbstractInsnNode.LINE)) {
				node = node.getNext();
				continue;
			} else {
				boolean found = false;
				for (int opcode : pattern[match]) {
					if (node.getOpcode() == opcode) {
						match++;
						found = true;
						break;
					}
				}
				if (!found) {
					match = 0;
				}
			}
			if (match == pattern.length) {
				return true;
			}

			node = node.getNext();
		}

		return false;
	}

}
