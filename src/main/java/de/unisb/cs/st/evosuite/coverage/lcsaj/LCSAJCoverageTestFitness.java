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

	private double approach;

	public LCSAJCoverageTestFitness(String className, String methodName, LCSAJ lcsaj) {
		this.lcsaj = lcsaj;
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
			if (call.className.equals(lcsaj.getClassName())
			        && call.methodName.equals(lcsaj.getMethodName())) {

				/*
				BytecodeInstruction firstBytecodeInstruction = BytecodeInstructionPool.getInstruction(lcsaj.getClassName(),
				                                                                                      lcsaj.getMethodName(),
				                                                                                      lcsaj.getStartNodeID());
				Set<Branch> LCSAJDependentBranches = firstBytecodeInstruction.getControlDependentBranches();
				double min = 1.0;
				for (Branch b : LCSAJDependentBranches) {
					BranchCoverageGoal dependentGoal = new BranchCoverageGoal(b,
					        firstBytecodeInstruction.getBranchExpressionValue(b),
					        lcsaj.getClassName(), lcsaj.getMethodName());
					BranchCoverageTestFitness dependentFitness = new BranchCoverageTestFitness(
					        dependentGoal);
					min = Math.min(min, normalize(dependentFitness.getFitness(individual,
					                                                          result)));
				}
				if (min > 0.0) {
					currentFitness = min + approach;
				} else {
				*/
				int lcsaj_position = 0;

				// For each branch that was passed in this call
				for (int i = 0; i < call.branchTrace.size(); i++) {
					int actualBranch = call.branchTrace.get(i);
					int lcsaj_branchID = lcsaj.getBranchID(lcsaj_position);

					double false_distance = call.falseDistanceTrace.get(i);
					double true_distance = call.trueDistanceTrace.get(i);

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
					//}
				}

			}
		}

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

	@Override
	public String toString() {
		return lcsaj.toString();
	}
}
