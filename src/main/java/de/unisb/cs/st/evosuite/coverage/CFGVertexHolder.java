package de.unisb.cs.st.evosuite.coverage;

import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.testcase.ExecutionTracer;

/**
 * Convenience-superclass for classes that hold a CFGVertex.
 * 
 * Just gives direct access to a lot of methods from the CFGVertex
 * Known subclasses are Branch and DefUse 
 * 
 * @author Andre Mis
 */
public class CFGVertexHolder {

	protected CFGVertex v;
	
	public CFGVertexHolder(CFGVertex v) {
		this.v = v;
	}
	
	// TODO: the following methods about control dependence are flawed right now:
	//			- the CFGVertex of a Branch does not have it's control dependent branchId
	//				but it's own branchId set 
	//			- this seems to be OK for ChromosomeRecycling as it stands, but
	//				especially getControlDependentBranch() will fail hard when called on a Branch
	//				the same may hold for the other ones as well. 
	//			- look at BranchCoverageGoal and Branch for more information
	
	/**
	 * Determines whether the CFGVertex is transitively control dependent
	 * on the given Branch
	 * 
	 * A CFGVertex is transitively control dependent on a given Branch
	 * if the Branch and the vertex are in the same method and the
	 * vertex is either directly control dependent on the Branch
	 * - look at isDirectlyControlDependentOn(Branch) -
	 * or the CFGVertex of the control dependent branch of this CFGVertex
	 * is transitively control dependent on the given branch. 
	 * 
	 */
	public boolean isTransitivelyControlDependentOn(Branch branch) {
		if(!getClassName().equals(branch.getClassName()))
			return false;		
		if(!getMethodName().equals(branch.getMethodName()))
			return false;
		
		CFGVertexHolder vertexHolder = this;
		do {
			if(vertexHolder.isControlDependentOn(branch))
				return true;
			vertexHolder = vertexHolder.getControlDependentBranch();
		} while (vertexHolder != null);
		
		return false;
	}
	
	/**
	 * Determines whether this CFGVertex is directly control dependent on the given Branch
	 *  meaning they share the same branchId and branchExpressionValue
	 */
	public boolean isControlDependentOn(Branch branch) {
		if(!getClassName().equals(branch.getClassName()))
			return false;		
		if(!getMethodName().equals(branch.getMethodName()))
			return false;
		
		return branch.getBranchId()==getBranchId() 
		&& branch.getBranchExpressionValue()==getBranchExpressionValue();		
	}
	
	/**
	 * Supposed to return the Branch this CFGVertex is control dependent on
	 * null if it's only dependent on the root branch 
	 */
	public Branch getControlDependentBranch() {
		// TODO fails if this.v is a branch
		// quick fix idea: take byteCode instruction directly
		//		previous to the branch (id-1)
		//		this is should have correct branchId and branchExpressionValue
		if(Branch.isActualBranch(v)) {
			CFGVertex hope = 
				CFGMethodAdapter.getCompleteCFG(getClassName(), getMethodName()).getVertex(getId()-1);
			if(hope==null)
				return null;
			return new CFGVertexHolder(hope).getControlDependentBranch();
		}
		return BranchPool.getBranch(getBranchId());
	}
	
	/**
	 * Determines the number of branches that have to be passed in order to
	 * pass this CFGVertex
	 * 
	 * Used to determine TestFitness difficulty
	 */
	public int getCDGDepth() {
		Branch current = getControlDependentBranch();
		int r = 1;
		while(current!=null) {
			r++;
			current = current.getControlDependentBranch();
		}
		return r;

	}
	
	private int getId() {
		return v.getID();
	}

	public CFGVertex getCFGVertex() {
		return v;
	}

	public String getMethodName() {
		return v.methodName;
	}
	
	public String getClassName() {
		return v.className;
	}
	
	public int getBranchId() {
		return v.branchId;
	}
	
	public boolean getBranchExpressionValue() {
		return v.branchExpressionValue;
	}
	
	public int getLineNumber() {
		return v.line_no;
	}
	
	public int getBytecodeId() {
		return v.getID();
	}
}
