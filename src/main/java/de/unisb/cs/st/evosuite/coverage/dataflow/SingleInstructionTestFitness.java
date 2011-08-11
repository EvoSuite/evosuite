package de.unisb.cs.st.evosuite.coverage.dataflow;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageTestFitness;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

public class SingleInstructionTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 4609519536866911970L;
	
	
	BytecodeInstruction goalInstruction;
	List<BranchCoverageTestFitness> branchFitnesses = new ArrayList<BranchCoverageTestFitness>();
	
	
	public SingleInstructionTestFitness(BytecodeInstruction goalInstruction) {
		if(goalInstruction == null)
			throw new IllegalArgumentException("null given");
		
//		Set<Branch> cds = goalInstruction.getAllControlDependentBranches();
	}
	
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		
		
		
		return 0;
	}

}
