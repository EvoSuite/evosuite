/**
 * Copyright (C) 2011,2012,2013,2014 Gordon Fraser, Andrea Arcuri, José Campos
 * and EvoSuite contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.coverage.ambiguity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.ControlDependency;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;

/**
 * <p>
 * AmbiguityCoverageTestFitness class.
 * </p>
 * 
 * @author José Campos
 */
public class AmbiguityCoverageTestFitness extends TestFitnessFunction {

	private static final long serialVersionUID = 6439138527631268608L;

	/**
	 * 
	 */
	protected BytecodeInstruction goalInstruction;

	/**
	 * 
	 */
	protected List<BranchCoverageTestFitness> branchFitnesses = new ArrayList<BranchCoverageTestFitness>();

	/**
	 * 
	 * @param goalInstruction
	 */
	public AmbiguityCoverageTestFitness(BytecodeInstruction goalInstruction) {
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

	/**
	 * 
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double fitness = Double.MAX_VALUE;

		for (BranchCoverageTestFitness branchFitness : branchFitnesses) {
			if (branchFitness.isCovered(result)) {
				fitness = 0.0;
				break ;
			}
		}

		updateIndividual(individual, fitness);
		return fitness;
	}

	/**
	 * 
	 */
	@Override
	public boolean isMaximizationFunction() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return goalInstruction.getMethodName() + " " + goalInstruction.toString();
	}

	/* (non-Javadoc)
	 * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
	 */
	@Override
	public int compareTo(TestFitnessFunction other) {
		if (other instanceof AmbiguityCoverageTestFitness) {
			return goalInstruction.compareTo(((AmbiguityCoverageTestFitness) other).goalInstruction);
		}
		return 0;
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
}
