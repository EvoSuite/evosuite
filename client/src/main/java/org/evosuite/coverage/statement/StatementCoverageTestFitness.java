/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.statement;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionResult;

public class StatementCoverageTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 4609519536866911970L;

	protected transient BytecodeInstruction goalInstruction;
	protected List<BranchCoverageTestFitness> branchFitnesses = new ArrayList<BranchCoverageTestFitness>();

	BranchCoverageTestFitness lastCoveringFitness = null;

	/**
	 * <p>
	 * Constructor for StatementCoverageTestFitness.
	 * </p>
	 * 
	 * @param goalInstruction
	 *            a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
	 */
	public StatementCoverageTestFitness(BytecodeInstruction goalInstruction) {
		if (goalInstruction == null)
			throw new IllegalArgumentException("null given");

		this.goalInstruction = goalInstruction;

		Set<ControlDependency> cds = goalInstruction.getControlDependencies();

		for (ControlDependency cd : cds) {
			BranchCoverageTestFitness fitness = BranchCoverageFactory.createBranchCoverageTestFitness(cd);

			branchFitnesses.add(fitness);
		}

		if (goalInstruction.isRootBranchDependent())
			branchFitnesses.add(BranchCoverageFactory.createRootBranchTestFitness(goalInstruction));

		if (cds.isEmpty() && !goalInstruction.isRootBranchDependent())
			throw new IllegalStateException(
			        "expect control dependencies to be empty only for root dependent instructions: "
			                + toString());

		if (branchFitnesses.isEmpty())
			throw new IllegalStateException(
			        "an instruction is at least on the root branch of it's method");
	}

	/** {@inheritDoc} */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {

		if (branchFitnesses.isEmpty())
			throw new IllegalStateException(
			        "expect to know at least one fitness for goalInstruction");

		double r = Double.MAX_VALUE;

		for (BranchCoverageTestFitness branchFitness : branchFitnesses) {
			double newFitness = branchFitness.getFitness(individual, result);
			if (newFitness == 0.0) {
				lastCoveringFitness = branchFitness;
				return 0.0;
			}
			if (newFitness < r)
				r = newFitness;
		}

		lastCoveringFitness = null;

		return r;
	}

	/**
	 * <p>
	 * Getter for the field <code>lastCoveringFitness</code>.
	 * </p>
	 * 
	 * @return a {@link org.evosuite.coverage.branch.BranchCoverageTestFitness}
	 *         object.
	 */
	public BranchCoverageTestFitness getLastCoveringFitness() {
		return lastCoveringFitness;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Statement Goal: " + goalInstruction.getMethodName() + " "
		        + goalInstruction.toString();
	}

	/**
	 * <p>
	 * explain
	 * </p>
	 * 
	 * @return a {@link java.lang.String} object.
	 */
	public String explain() {
		StringBuilder r = new StringBuilder();

		r.append("StatementCoverageTestFitness for ");
		r.append(goalInstruction.toString());
		r.append(" in " + goalInstruction.getMethodName());

		r.append("\n");
		r.append("CDS:\n");
		for (BranchCoverageTestFitness branchFitness : branchFitnesses) {
			r.append("\t" + branchFitness.toString());
		}
		return r.toString();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
	 */
	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof StatementCoverageTestFitness) {
			return goalInstruction.compareTo(((StatementCoverageTestFitness) other).goalInstruction);
		}
		return compareClassName(other);
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + goalInstruction.getInstructionId();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatementCoverageTestFitness other = (StatementCoverageTestFitness) obj;
		if (goalInstruction.getInstructionId() != other.goalInstruction.getInstructionId()) {
				return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
	 */
	@Override
	public String getTargetClass() {
		return goalInstruction.getClassName();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
	 */
	@Override
	public String getTargetMethod() {
		return goalInstruction.getMethodName();
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		oos.writeObject(goalInstruction.getClassName());
		oos.writeObject(goalInstruction.getMethodName());
		oos.writeInt(goalInstruction.getInstructionId());
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
    	IOException {
		ois.defaultReadObject();
		String className  = (String)ois.readObject();
		String methodName = (String)ois.readObject();
		int instructionId = ois.readInt();
		BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getInstruction(className, methodName, instructionId);
	}
}
