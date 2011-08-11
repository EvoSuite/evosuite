package de.unisb.cs.st.evosuite.coverage.dataflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageFactory;
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
		if (goalInstruction == null)
			throw new IllegalArgumentException("null given");

		this.goalInstruction = goalInstruction;

		Set<Branch> cds = goalInstruction.getAllControlDependentBranches();

		for (Branch cd : cds) {
			BranchCoverageTestFitness fitness = BranchCoverageFactory
					.createBranchCoverageTestFitness(cd, goalInstruction
							.getBranchExpressionValue(cd));
			
			branchFitnesses.add(fitness);
		}

		if (cds.isEmpty()) { // dependent on root-branch
			if (!goalInstruction.isRootBranchDependent())
				throw new IllegalStateException(
						"expect control dependencies to be empty only for root dependent instrucitons");

			branchFitnesses.add(BranchCoverageFactory
					.createRootBranchTestFitness(goalInstruction));
		}
	}

	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {

		return 0;
	}

}
