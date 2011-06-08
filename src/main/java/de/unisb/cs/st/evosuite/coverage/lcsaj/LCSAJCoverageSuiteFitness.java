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

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * Evaluate fitness of a test suite with respect to all LCSAJs of a class
 * 
 * @author Merlin Lang
 * 
 */
public class LCSAJCoverageSuiteFitness extends TestSuiteFitnessFunction {
	
	public HashMap<Integer,Integer> expectedBranchExecutions = new HashMap<Integer,Integer>();

	public HashSet<LCSAJCoverageTestFitness> LCSAJFitnessFunctions = new HashSet<LCSAJCoverageTestFitness>();

	public HashMap<LCSAJ, Double> LCSAJFitnesses = new HashMap<LCSAJ, Double>();

	public double best_fitness = Double.MAX_VALUE;

	public LCSAJCoverageSuiteFitness() {
		
		for (String className : LCSAJPool.lcsaj_map.keySet()){
			for (String methodName : LCSAJPool.lcsaj_map.get(className)
					.keySet())
				for (LCSAJ lcsaj : LCSAJPool.lcsaj_map.get(className)
						.get(methodName)){
					for (Integer branchID : lcsaj.getBranchIDs()){
						if (!expectedBranchExecutions.containsKey(branchID))
							expectedBranchExecutions.put(branchID, 0);
						else
							expectedBranchExecutions.put(branchID, expectedBranchExecutions.get(branchID)+1);
					}
					LCSAJFitnesses.put(lcsaj,Double.MAX_VALUE);
				}
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
		
		Map<String, Integer> call_count = new HashMap<String, Integer>();
		HashMap<Integer,Integer> branchExecutions = new HashMap<Integer,Integer>();
		
		double fitness = 0.0;

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
				if (!branchExecutions.containsKey(entry.getKey()))
					branchExecutions.put(Integer.getInteger(entry.getKey()), entry.getValue());
				else {
					branchExecutions.put(
							Integer.getInteger(entry.getKey()),
							branchExecutions.get(entry.getKey())
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
							double oldFitness;
							for (LCSAJCoverageTestFitness testFitness : LCSAJFitnessFunctions) {
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
			fitness += normalize(LCSAJFitnesses.get(l));
		}

		for (Integer executedID : expectedBranchExecutions.keySet()){
			if (!branchExecutions.containsKey(executedID))
				fitness += expectedBranchExecutions.get(executedID);
			else
				fitness += Math.abs(expectedBranchExecutions.get(executedID)-branchExecutions.get(executedID));
		}

		if (fitness < best_fitness)
			best_fitness = fitness;
		
		double coverage = 0.0;
		
		for (LCSAJ l : LCSAJFitnesses.keySet() ){
			if (LCSAJFitnesses.get(l) == 0)
				coverage += 1;
		}

		suite.setCoverage(coverage / LCSAJFitnesses.size());
		
		return fitness;
	}

}
