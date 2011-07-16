/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageGoal;

/**
 * @author Gordon Fraser
 * 
 */
public class Mutation {

	private final int id;

	private final String className;

	private final String methodName;

	private final String mutationName;

	private final BytecodeInstruction original;

	private final InsnList mutation;

	private final InsnList infection;

	private int lineNo;

	public Mutation(String className, String methodName, String mutationName, int id,
	        BytecodeInstruction original, AbstractInsnNode mutation, InsnList distance) {
		this.className = className;
		this.methodName = methodName;
		this.mutationName = mutationName;
		this.id = id;
		this.original = original;
		this.mutation = new InsnList();
		this.mutation.add(mutation);
		this.infection = distance;
		this.lineNo = original.getLineNumber();
	}

	public Mutation(String className, String methodName, String mutationName, int id,
	        BytecodeInstruction original, InsnList mutation, InsnList distance) {
		this.className = className;
		this.methodName = methodName;
		this.mutationName = mutationName;
		this.id = id;
		this.original = original;
		this.mutation = mutation;
		this.infection = distance;
	}

	public int getId() {
		return id;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public int getOperandSize() {
		return 0;
	}

	public Set<BranchCoverageGoal> getControlDependencies() {
		Set<BranchCoverageGoal> goals = new HashSet<BranchCoverageGoal>();
		for (Branch branch : original.getControlDependentBranches()) {
			BranchCoverageGoal goal = new BranchCoverageGoal(branch,
			        original.getBranchExpressionValue(branch), className, methodName);
			goals.add(goal);
		}
		return goals;
	}

	public AbstractInsnNode getOriginalNode() {
		return original.getASMNode();
	}

	public InsnList getMutation() {
		return mutation;
	}

	public InsnList getInfectionDistance() {
		return infection;
	}

	public static InsnList getDefaultInfectionDistance() {
		InsnList defaultDistance = new InsnList();
		defaultDistance.add(new LdcInsnNode(0.0));
		return defaultDistance;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return className + "." + methodName + ":" + lineNo + " - " + mutationName;
	}
}
