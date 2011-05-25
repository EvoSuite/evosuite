/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.coverage.lcsaj;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * Evaluate fitness of a test suite with respect to all LCSAJs of a class
 * 
 * @author
 * 
 */
public class LCSAJCoverageSuiteFitness extends TestSuiteFitnessFunction {

	public int branchExecutions = 0;

	public int total_branches = BranchPool.getBranchCounter();

	public HashSet<LCSAJCoverageTestFitness> LCSAJFitnessFunctions = new HashSet<LCSAJCoverageTestFitness>();

	public HashMap<LCSAJ, Double> LCSAJFitnesses = new HashMap<LCSAJ, Double>();

	public double best_fitness = Double.MAX_VALUE;

	public LCSAJCoverageSuiteFitness() {

		for (String className : LCSAJPool.getLCSAJMap().keySet())
			for (String methodName : LCSAJPool.getLCSAJMap().get(className)
					.keySet())
				for (LCSAJ lcsaj : LCSAJPool.getLCSAJMap().get(className)
						.get(methodName)) {
					branchExecutions += lcsaj.length();
				}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.ga.FitnessFunction#getFitness(de.unisb.cs.st.
	 * evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(Chromosome individual) {

		TestSuiteChromosome suite = (TestSuiteChromosome) individual;
		List<ExecutionResult> results = runTestSuite(suite);

		double fitness = 0.0;

		Map<String, Integer> predicate_count = new HashMap<String, Integer>();
		Map<String, Integer> call_count = new HashMap<String, Integer>();

		for (ExecutionResult result : results) {
			for (Entry<String, Integer> entry : result.getTrace().covered_methods
					.entrySet()) {
				if (!call_count.containsKey(entry.getKey()))
					call_count.put(entry.getKey(), entry.getValue());
				else {
					call_count.put(entry.getKey(),
							call_count.get(entry.getKey()) + entry.getValue());
				}
			}

			for (Entry<String, Integer> entry : result.getTrace().covered_predicates
					.entrySet()) {
				if (!predicate_count.containsKey(entry.getKey()))
					predicate_count.put(entry.getKey(), entry.getValue());
				else {
					predicate_count.put(
							entry.getKey(),
							predicate_count.get(entry.getKey())
									+ entry.getValue());
				}
			}
			for (String className : LCSAJPool.getLCSAJMap().keySet())
				for (String methodName : LCSAJPool.getLCSAJMap().get(className)
						.keySet())
					for (LCSAJ lcsaj : LCSAJPool.getLCSAJMap().get(className)
							.get(methodName)) {

						LCSAJFitnessFunctions.add(new LCSAJCoverageTestFitness(
								lcsaj));

						for (TestChromosome t : suite.tests) {
							double oldFitness = 0.0;
							for (LCSAJCoverageTestFitness testFitness : LCSAJFitnessFunctions) {
								if (LCSAJFitnesses.containsKey(lcsaj))
									oldFitness = LCSAJFitnesses.get(lcsaj);
								double newFitness = testFitness.getFitness(t,
										result);
								if (newFitness < oldFitness)
									LCSAJFitnesses.put(lcsaj, newFitness);
							}
						}
					}
		}

		for (LCSAJ l : LCSAJFitnesses.keySet()) {
			fitness += LCSAJFitnesses.get(l);
		}

		fitness += branchExecutions * (total_branches - predicate_count.size());

		if (fitness < best_fitness)
			best_fitness = fitness;

		return fitness;
	}

}
