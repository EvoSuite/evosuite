/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.coverage.lcsaj;

import java.util.HashMap;

import org.objectweb.asm.tree.AbstractInsnNode;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageGoal;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageTestFitness;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * Evaluate fitness of a single test case with respect to a single LCSAJ
 * 
 * @author
 * 
 */
public class LCSAJCoverageTestFitness extends TestFitnessFunction {

	LCSAJ lcsaj;

	ControlFlowGraph cfg;

	public LCSAJCoverageTestFitness(String className, String methodName, LCSAJ lcsaj,
	        ControlFlowGraph cfg) {
		this.lcsaj = lcsaj;
		this.cfg = cfg;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.TestFitnessFunction#getFitness(de.unisb.cs.st.evosuite.testcase.TestChromosome, de.unisb.cs.st.evosuite.testcase.ExecutionResult)
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double fitness = 0.0;
		HashMap<Integer, AbstractInsnNode> instructions = lcsaj.getInstructions();
		boolean firstInsn = true;
		for (Integer i : instructions.keySet()) {
			CFGVertex c = cfg.getVertex(i);
			if (c.branchID != -1 && firstInsn) {
				BranchCoverageTestFitness b = new BranchCoverageTestFitness(
				        new BranchCoverageGoal(BranchPool.getBranch(c.branchID), false,
				                cfg, lcsaj.getClassName(), lcsaj.getMethodName()));
				fitness += b.getFitness(individual, result);
				firstInsn = false;
			}
			BranchCoverageTestFitness b = new BranchCoverageTestFitness(
			        new BranchCoverageGoal(BranchPool.getBranch(c.branchID), false, cfg,
			                lcsaj.getClassName(), lcsaj.getMethodName()));
			if (b.getFitness(individual, result) > 0.0 && !c.isJump())
				return (fitness += b.getFitness(individual, result));
			b = new BranchCoverageTestFitness(new BranchCoverageGoal(
			        BranchPool.getBranch(c.branchID), true, cfg, lcsaj.getClassName(),
			        lcsaj.getMethodName()));
			if (b.getFitness(individual, result) > 0.0 && c.isJump())
				return (fitness += b.getFitness(individual, result));
		}
		return 0.0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#updateIndividual(de.unisb.cs.st.evosuite.ga.Chromosome, double)
	 */
	@Override
	protected void updateIndividual(Chromosome individual, double fitness) {
		individual.setFitness(fitness);
	}

}
