package de.unisb.cs.st.evosuite.coverage.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.cfg.ControlDependency;
import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageFactory;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageTestFitness;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

public class StatementCoverageTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 4609519536866911970L;

	BytecodeInstruction goalInstruction;
	List<BranchCoverageTestFitness> branchFitnesses = new ArrayList<BranchCoverageTestFitness>();

	public StatementCoverageTestFitness(BytecodeInstruction goalInstruction) {
		if (goalInstruction == null)
			throw new IllegalArgumentException("null given");

		this.goalInstruction = goalInstruction;

		Set<ControlDependency> cds = goalInstruction.getControlDependencies();

		for (ControlDependency cd : cds) {
			BranchCoverageTestFitness fitness = BranchCoverageFactory
					.createBranchCoverageTestFitness(cd);

			branchFitnesses.add(fitness);
		}

		if (cds.isEmpty()) { // dependent on root-branch
			if (!goalInstruction.isRootBranchDependent())
				throw new IllegalStateException(
						"expect control dependencies to be empty only for root dependent instructions: "
								+ toString());

			branchFitnesses.add(BranchCoverageFactory
					.createRootBranchTestFitness(goalInstruction));
		}
		
	}

	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {

		if (branchFitnesses.isEmpty())
			throw new IllegalStateException(
					"expect to know at least one fitness for goalInstruction");

		double r = Double.MAX_VALUE;

		for (BranchCoverageTestFitness branchFitness : branchFitnesses) {
			double newFitness = branchFitness.getFitness(individual, result);
			if (newFitness == 0.0) {
				return 0.0;
			}
			if (newFitness < r)
				r = newFitness;
		}
		
		return r;
	}

	@Override
	public String toString() {
		return "Statement Goal: "+goalInstruction.getMethodName() + " "
				+ goalInstruction.toString();
	}

	public String explain() {
		StringBuilder r = new StringBuilder();
		
		r.append("StatementCoverageTestFitness for ");
		r.append(goalInstruction.toString());
		r.append(" in "+goalInstruction.getMethodName());
		
		r.append("\n");
		r.append("CDS:\n");
		for(BranchCoverageTestFitness branchFitness : branchFitnesses) {
			r.append("\t"+branchFitness.toString());
		}
		return r.toString();
	}
}
