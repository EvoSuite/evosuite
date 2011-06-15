package de.unisb.cs.st.evosuite.coverage.lcsaj;

import java.util.ArrayList;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;

public class LCSAJ {

	// All branches passed in the LCSAJ
	private final ArrayList<AbstractInsnNode> branchInsnNodes;
	private final ArrayList<Integer> branchIDs;
	// Information needed and maintained in the LCSAJ instrumentation and
	// detection algorithm
	private final AbstractInsnNode startNode;
	private final int startNodeID;
	private AbstractInsnNode lastAccessedNode;

	private int id = -1;
	// Class and method where the LCSAJ occurs
	private final String className;
	private final String methodName;

	// Copy constructor
	public LCSAJ(LCSAJ l) {
		this.className = l.getClassName();
		this.methodName = l.getMethodName();

		this.branchInsnNodes = new ArrayList<AbstractInsnNode>();
		this.branchIDs = new ArrayList<Integer>();
		for (AbstractInsnNode n : l.getBranches()) {
			branchInsnNodes.add(n);
		}
		for (Integer i : l.getBranchIDs()) {
			branchIDs.add(i);
		}

		this.startNode = l.getStartNode();
		this.startNodeID = l.getStartNodeID();
		this.lastAccessedNode = l.getLastNodeAccessed();
	}

	public LCSAJ(String className, String methodName, AbstractInsnNode start, int startNodeID) {
		this.className = className;
		this.methodName = methodName;
		this.branchIDs = new ArrayList<Integer>();
		this.branchInsnNodes = new ArrayList<AbstractInsnNode>();
		this.startNode = start;
		this.startNodeID = startNodeID;
		this.lastAccessedNode = start;
	}

	public ArrayList<AbstractInsnNode> getBranches() {
		return this.branchInsnNodes;
	}

	public int getBranchID(int position) {
		return branchIDs.get(position);
	}

	public AbstractInsnNode getBranchInstruction(int position) {
		return branchInsnNodes.get(position);
	}

	public String getClassName() {
		return this.className;
	}

	public int getID() {
		return this.id;
	}

	public AbstractInsnNode getLastNodeAccessed() {
		return lastAccessedNode;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public AbstractInsnNode getStartNode() {
		return startNode;
	}

	public int getStartNodeID() {
		return this.startNodeID;
	}

	public int length() {
		return branchInsnNodes.size();
	}

	public void lookupInstruction(int id, AbstractInsnNode node) {
		lastAccessedNode = node;
		if (node instanceof JumpInsnNode) {
			JumpInsnNode jump = (JumpInsnNode) node;
			this.branchInsnNodes.add(jump);
			this.branchIDs.add(id);
		} else if (node instanceof InsnNode) {
			InsnNode insn = (InsnNode) node;
			if ((insn.getOpcode() == Opcodes.ATHROW) || (insn.getOpcode() == Opcodes.RETURN)
					|| (insn.getOpcode() == Opcodes.ARETURN) || (insn.getOpcode() == Opcodes.IRETURN)
					|| (insn.getOpcode() == Opcodes.DRETURN) || (insn.getOpcode() == Opcodes.LRETURN)
					|| (insn.getOpcode() == Opcodes.FRETURN)) {

				branchInsnNodes.add(insn);
				branchIDs.add(id);

			}
		}
	}

	/**
	 * Set an (not necessarily unique) new ID for a LCSAJ. While adding a LCSAJ
	 * into the Pool the number is set to its occurrence during the detection of
	 * all LCSAJ in a method
	 */
	public void setID(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		String output = "LCSAJ no.: " + this.id;
		output += " in " + this.className + "/" + this.methodName + ". Branches passed: ";
		for (Integer i : branchIDs) {
			output += i + " ";
		}
		return output;
	}

	protected ArrayList<Integer> getBranchIDs() {
		return this.branchIDs;
	}
}
