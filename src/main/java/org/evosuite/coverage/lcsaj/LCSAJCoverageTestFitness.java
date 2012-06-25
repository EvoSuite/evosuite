/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
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
package org.evosuite.coverage.lcsaj;

import org.evosuite.Properties;
import org.evosuite.Properties.Strategy;
import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.MethodCall;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;


/**
 * Evaluate fitness of a single test case with respect to a single LCSAJ
 * 
 * @author
 * 
 */
public class LCSAJCoverageTestFitness extends TestFitnessFunction {
	private static final long serialVersionUID = 1L;

	LCSAJ lcsaj;

	private final int lcsaj_finalBranchID;

	private double approach;

	public LCSAJCoverageTestFitness(String className, String methodName, LCSAJ lcsaj) {
		this.lcsaj = lcsaj;
		lcsaj_finalBranchID = lcsaj.getBranchID(lcsaj.length() - 1);
	}

	public LCSAJCoverageTestFitness(LCSAJ lcsaj) {
		this.lcsaj = lcsaj;
		lcsaj_finalBranchID = lcsaj.getBranchID(lcsaj.length() - 1);
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
		approach = lcsaj.length();
		double savedFitness = approach;
		logger.debug("Evaluating fitness for " + lcsaj);

		// for all method calls:
		for (MethodCall call : result.getTrace().getMethodCalls()) {
			double currentFitness = approach;

			// if method call is the method of the LCSAJ
			if (call.className.equals(lcsaj.getClassName())
			        && call.methodName.equals(lcsaj.getMethodName())) {

				int lcsaj_position = 0;
				boolean found = false;

				logger.debug("New call: " + call.className + "." + call.methodName + " "
				        + call.branchTrace.size());
				// For each branch that was passed in this call
				for (int i = 0; i < call.branchTrace.size(); i++) {
					int actualBranch = call.branchTrace.get(i);
					int lcsaj_branchID = lcsaj.getBranchID(lcsaj_position);

					double false_distance = call.falseDistanceTrace.get(i);
					double true_distance = call.trueDistanceTrace.get(i);
					logger.debug("Current branch: " + call.branchTrace.get(i) + ": "
					        + true_distance + "/" + false_distance + ", need "
					        + lcsaj.getBranchID(lcsaj_position));

					if (actualBranch == lcsaj_branchID) {
						if (lcsaj_position == 0)
							found = true;
						currentFitness -= 1.0;
						if (actualBranch == lcsaj_finalBranchID) {
							currentFitness += normalize(true_distance);

							if (currentFitness < savedFitness) {
								savedFitness = currentFitness;
							}
							if (lcsaj_position > lcsaj.getdPositionReached())
								lcsaj.setPositionReached(lcsaj_position);
							lcsaj_position = 0;
							currentFitness = approach;
							continue;
						} else if (false_distance > 0) {
							logger.debug("Took a wrong turn with false distance "
							        + false_distance);
							currentFitness += normalize(false_distance);

							if (currentFitness < savedFitness)
								savedFitness = currentFitness;
							if (lcsaj_position > lcsaj.getdPositionReached())
								lcsaj.setPositionReached(lcsaj_position);

							lcsaj_position = 0;
							currentFitness = approach;
							continue;
						}

						lcsaj_position++;
						logger.debug("LCSAJ position: " + lcsaj_position);

					} else {
						if (LCSAJPool.isLCSAJBranch(BranchPool.getBranch(actualBranch))) {
							//						logger.debug("Skipping pseudo branch");
							continue;
						}
						if (lcsaj_position > lcsaj.getdPositionReached())
							lcsaj.setPositionReached(lcsaj_position);
						lcsaj_position = 0;
						currentFitness = approach;
					}
					//}
				}
				if (Properties.STRATEGY != Strategy.EVOSUITE) {
					if (!found) {
						logger.debug("Looking for approach to initial branch: "
						        + lcsaj.getStartBranch() + " with ID "
						        + lcsaj.getStartBranch().getActualBranchId());
						BranchCoverageGoal goal = new BranchCoverageGoal(
						        lcsaj.getStartBranch(), true, lcsaj.getClassName(),
						        lcsaj.getMethodName());
						BranchCoverageTestFitness dependentFitness = new BranchCoverageTestFitness(
						        goal);
						assert (currentFitness == approach);
						currentFitness += dependentFitness.getFitness(individual, result);
						if (currentFitness < savedFitness)
							savedFitness = currentFitness;
						//logger.debug("Initial branch has distance: "
						//        + dependentFitness.getFitness(individual, result));
						//logger.debug("Dependencies of initial branch: ");
						//for (Branch branch : lcsaj.getStartBranch().getAllControlDependentBranches()) {
						//	logger.debug(branch);
						//	}

						logger.debug("Resulting fitness: " + savedFitness);
					} else {
						logger.debug("Call does not match: " + call.className + "."
						        + call.methodName + " " + call.branchTrace.size());
					}
				}
			}
		}
		updateIndividual(individual, savedFitness);
		return savedFitness;
	}

	@Override
	public String toString() {
		return lcsaj.toString();
	}

	public LCSAJ getLcsaj() {
		return this.lcsaj;
	}
}
