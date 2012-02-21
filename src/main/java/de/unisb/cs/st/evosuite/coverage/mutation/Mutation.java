/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;

import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageGoal;
import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.graphs.cfg.ControlDependency;

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

	private final int lineNo;

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
		this.lineNo = original.getLineNumber();
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

	public String getMutationName() {
		return id + ": " + mutationName + ", line " + original.getLineNumber();
	}

	public Set<BranchCoverageGoal> getControlDependencies() {
		Set<BranchCoverageGoal> goals = new HashSet<BranchCoverageGoal>();
		for (ControlDependency cd : original.getControlDependencies()) {
			BranchCoverageGoal goal = new BranchCoverageGoal(cd, className, methodName);
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
		return "Mutation " + id + ": " + className + "." + methodName + ":" + lineNo
		        + " - " + mutationName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + id;
		result = prime * result + lineNo;
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + ((mutationName == null) ? 0 : mutationName.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mutation other = (Mutation) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (id != other.id)
			return false;
		if (lineNo != other.lineNo)
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		if (mutationName == null) {
			if (other.mutationName != null)
				return false;
		} else if (!mutationName.equals(other.mutationName))
			return false;
		return true;
	}

}
