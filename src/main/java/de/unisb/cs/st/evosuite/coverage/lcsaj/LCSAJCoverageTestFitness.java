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

import de.unisb.cs.st.evosuite.cfg.ControlFlowGraph;
import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageGoal;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageTestFitness;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.ExecutionTrace.MethodCall;
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
	
	public LCSAJCoverageTestFitness(LCSAJ lcsaj) {
		this.lcsaj = lcsaj;
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
		approach = lcsaj.getBranches().size();
		double currentFitness = approach;
		double savedFitness = approach;
		int lcsaj_finalBranchID = lcsaj.getBranchID(lcsaj.length() - 1);

		// for all method calls:
		for (MethodCall call : result.getTrace().finished_calls) {

			// if method call is the method of the LCSAJ
			if (call.class_name.equals(lcsaj.getClassName())
					&& call.method_name.equals(lcsaj.getMethodName())) {
				int lcsaj_position = 0;

				// For each branch that was passed in this call
				for (int i = 0; i < call.branch_trace.size(); i++) {
					int actualBranch = call.branch_trace.get(i);
					int lcsaj_branchID = lcsaj.getBranchID(lcsaj_position);

					double false_distance = call.false_distance_trace.get(i);
					double true_distance = call.true_distance_trace.get(i);

					if (actualBranch == lcsaj_branchID) {
						currentFitness -= 1.0;
						if (actualBranch == lcsaj_finalBranchID) {

							currentFitness += normalize(true_distance);

							if (currentFitness < savedFitness)
								savedFitness = currentFitness;

							lcsaj_position = 0;
							currentFitness = approach;
							continue;
						} else if (false_distance > 0) {

							currentFitness += normalize(false_distance);

							if (currentFitness < savedFitness)
								savedFitness = currentFitness;

							lcsaj_position = 0;
							currentFitness = approach;
							continue;
						}

						lcsaj_position++;

					} else {
						lcsaj_position = 0;
						currentFitness = approach;
					}

				}
			}
		}
//		if (cfg != null){
//			if (lcsaj.getGeneratingBranchID() != -1) {
//				Branch b = BranchPool.getBranchByBytecodeId(lcsaj.getClassName(),
//						lcsaj.getMethodName(), lcsaj.getGeneratingBranchID());
//				BranchCoverageGoal generatingBranchGoal = new BranchCoverageGoal(b,
//						true, cfg, lcsaj.getClassName(), lcsaj.getMethodName());
//				BranchCoverageTestFitness generatingBranchCoverageTestFitness = new BranchCoverageTestFitness(
//						generatingBranchGoal);
//				savedFitness += generatingBranchCoverageTestFitness.getFitness(
//						individual, result);
//			}
//		}
		updateIndividual(individual, savedFitness);
		return savedFitness;
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

	public String toString() {
		return lcsaj.toString();
	}
}
