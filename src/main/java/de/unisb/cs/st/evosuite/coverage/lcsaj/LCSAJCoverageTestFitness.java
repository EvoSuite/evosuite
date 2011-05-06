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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
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

	double approach;
	double branch;

	public LCSAJCoverageTestFitness(String className, String methodName,
			LCSAJ lcsaj, ControlFlowGraph cfg) {
		this.lcsaj = lcsaj;
		this.cfg = cfg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.TestFitnessFunction#getFitness(de.unisb
	 * .cs.st.evosuite.testcase.TestChromosome,
	 * de.unisb.cs.st.evosuite.testcase.ExecutionResult)
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double fitness = 0.0;
		HashMap<Integer, AbstractInsnNode> instructions = lcsaj
				.getInstructions();
		boolean firstInsn = true;
		approach = instructions.size();
		for (Integer i : instructions.keySet()) {

			AbstractInsnNode current_instruction = instructions.get(i);
			BytecodeInstruction c = cfg.getVertex(i);

			if (c == null) {
				// Only jump nodes are in minimized CFG!
				continue;
			}

			if (c.branchId != -1 && firstInsn) {
				BranchCoverageTestFitness b = new BranchCoverageTestFitness(
						new BranchCoverageGoal(
								BranchPool.getBranch(c.branchId), false, cfg,
								lcsaj.getClassName(), lcsaj.getMethodName()));
				fitness += b.getFitness(individual, result);
				firstInsn = false;
				continue;
			}

			if (current_instruction instanceof JumpInsnNode) {

				JumpInsnNode current_jump = (JumpInsnNode) current_instruction;
	
				if (current_jump.getOpcode() != Opcodes.GOTO) {
					if (lcsaj.getInstruction(i).equals(lcsaj.getLastJump())) {
						branch = result.getTrace().true_distances.get(c.branchId);
						if (branch != 0.0) {
							fitness += normalize(branch);
							break;
						}
					}
					else {
						branch = result.getTrace().false_distances.get(c.branchId);
						if (branch != 0.0) {
							fitness += approach + normalize(branch);
							break;
						}
					}
				}
				else {
					
				}
			}
			approach--;
		}
		updateIndividual(individual, fitness);
		return fitness;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.FitnessFunction#updateIndividual(de.unisb.
	 * cs.st.evosuite.ga.Chromosome, double)
	 */
	@Override
	protected void updateIndividual(Chromosome individual, double fitness) {
		individual.setFitness(fitness);
	}

}
