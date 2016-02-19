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
package org.evosuite.coverage.lcsaj;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.ExecutionResult;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * Evaluate fitness of a test suite with respect to all LCSAJs of a class
 * 
 * @author Merlin Lang
 */
public class LCSAJCoverageSuiteFitness extends TestSuiteFitnessFunction {
	private static final long serialVersionUID = 1L;

	public HashMap<Integer, Integer> expectedTrueExecutions = new HashMap<Integer, Integer>();

	public HashMap<Integer, Integer> expectedFalseExecutions = new HashMap<Integer, Integer>();

	public HashSet<LCSAJCoverageTestFitness> LCSAJFitnessFunctions = new HashSet<LCSAJCoverageTestFitness>();

	public double best_fitness = Double.MAX_VALUE;

	private final BranchCoverageSuiteFitness branchFitness = new BranchCoverageSuiteFitness();

	/**
	 * <p>
	 * Constructor for LCSAJCoverageSuiteFitness.
	 * </p>
	 */
	public LCSAJCoverageSuiteFitness() {
		ExecutionTracer.enableTraceCalls();
		for (String className : LCSAJPool.lcsaj_map.keySet()) {
			for (String methodName : LCSAJPool.lcsaj_map.get(className).keySet())
				for (LCSAJ lcsaj : LCSAJPool.lcsaj_map.get(className).get(methodName)) {
					LCSAJFitnessFunctions.add(new LCSAJCoverageTestFitness(lcsaj));
					for (int i = 0; i < lcsaj.length() - 1; i++) {
						Branch branch = lcsaj.getBranch(i);
						if (!expectedFalseExecutions.containsKey(branch.getActualBranchId()))
							expectedFalseExecutions.put(branch.getActualBranchId(), 1);
						else
							expectedFalseExecutions.put(branch.getActualBranchId(),
							                            expectedFalseExecutions.get(branch.getActualBranchId()) + 1);
					}
					Branch branch = lcsaj.getLastBranch();
					if (!expectedTrueExecutions.containsKey(branch.getActualBranchId()))
						expectedTrueExecutions.put(branch.getActualBranchId(), 1);
					else
						expectedTrueExecutions.put(branch.getActualBranchId(),
						                           expectedTrueExecutions.get(branch.getActualBranchId()) + 1);
				}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.evosuite.ga.FitnessFunction#getFitness(org.
	 * evosuite.ga.Chromosome)
	 */
	/** {@inheritDoc} */
	@Override
	public double getFitness(
	        AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {

		List<ExecutionResult> results = runTestSuite(suite);

		double fitness = branchFitness.getFitness(suite);
		logger.debug("Branch fitness: {}", fitness);

		//Map<String, Integer> call_count = new HashMap<String, Integer>();
		HashMap<Integer, Integer> trueExecutions = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> falseExecutions = new HashMap<Integer, Integer>();
		HashMap<LCSAJ, Double> LCSAJFitnesses = new HashMap<LCSAJ, Double>();

		logger.debug("Checking " + LCSAJFitnessFunctions.size() + " LCSAJs against "
		        + results.size() + " tests = " + LCSAJFitnessFunctions.size()
		        * results.size() + " test/LCSAJ combinations");
		for (ExecutionResult result : results) {

			for (LCSAJCoverageTestFitness testFitness : LCSAJFitnessFunctions) {
				TestChromosome chromosome = new TestChromosome();
				chromosome.setTestCase(result.test);
				chromosome.setLastExecutionResult(result);
				double newFitness = testFitness.getFitness(chromosome, result);
				if (!LCSAJFitnesses.containsKey(testFitness.lcsaj))
					LCSAJFitnesses.put(testFitness.lcsaj, newFitness);
				else {
					double oldFitness = LCSAJFitnesses.get(testFitness.lcsaj);
					if (newFitness < oldFitness)
						LCSAJFitnesses.put(testFitness.lcsaj, newFitness);
				}
			}
			for (Entry<Integer, Integer> entry : result.getTrace().getPredicateExecutionCount().entrySet()) {
				if (!trueExecutions.containsKey(entry.getKey()))
					trueExecutions.put(entry.getKey(), entry.getValue());
				else {
					trueExecutions.put(entry.getKey(), trueExecutions.get(entry.getKey())
					        + entry.getValue());
				}
				if (!falseExecutions.containsKey(entry.getKey()))
					falseExecutions.put(entry.getKey(), entry.getValue());
				else {
					falseExecutions.put(entry.getKey(),
					                    falseExecutions.get(entry.getKey())
					                            + entry.getValue());
				}
			}
		}

		for (LCSAJ l : LCSAJFitnesses.keySet()) {
			fitness += normalize(LCSAJFitnesses.get(l));
		}

		fitness += branchFitness.getFitness(suite);

		logger.debug("Combined fitness: " + fitness);
		double missingBranches = 0.0;
		for (Integer executedID : expectedTrueExecutions.keySet()) {
			if (!trueExecutions.containsKey(executedID))
				missingBranches += expectedTrueExecutions.get(executedID);
			else {
				if (trueExecutions.get(executedID) < expectedTrueExecutions.get(executedID))
					missingBranches += expectedTrueExecutions.get(executedID)
					        - trueExecutions.get(executedID);
			}
		}
		for (Integer executedID : expectedFalseExecutions.keySet()) {
			if (!falseExecutions.containsKey(executedID))
				missingBranches += expectedFalseExecutions.get(executedID);
			else {
				if (falseExecutions.get(executedID) < expectedFalseExecutions.get(executedID))
					missingBranches += expectedFalseExecutions.get(executedID)
					        - falseExecutions.get(executedID);
			}
		}

		fitness += normalize(missingBranches);

		logger.info("Combined fitness with correction: " + fitness);

		updateIndividual(this, suite, fitness);

		double coverage = 0.0;

		for (LCSAJ l : LCSAJFitnesses.keySet()) {
			if (LCSAJFitnesses.get(l) == 0)
				coverage += 1;
		}

		suite.setCoverage(this, coverage / LCSAJFitnesses.size());

		return fitness;
	}

}
