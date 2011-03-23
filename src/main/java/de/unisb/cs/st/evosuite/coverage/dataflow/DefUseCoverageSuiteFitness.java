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


package de.unisb.cs.st.evosuite.coverage.dataflow;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * Evaluate fitness of a test suite with respect to all of its def-use pairs
 * 
 * @author 
 *
 */
public class DefUseCoverageSuiteFitness extends TestSuiteFitnessFunction {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#getFitness(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(Chromosome individual) {
		logger.trace("Calculating defuse fitness");

		TestSuiteChromosome suite = (TestSuiteChromosome)individual;
		// this seems odd: why aren't the results calculated once for all suites?
		//				   seem very inefficient. TODO: talk to Gordon i guess, i seem to be missing a point here
		// ah ok nvm, a test holds it's last result if it wasn't changed so this is OK
		List<ExecutionResult> results = runTestSuite(suite);
		double fitness = 0.0;

		// first simple and naive idea: 
		//  just take each DUGoal, calculate the minimal fitness over all results in the suite
		//  once a goal is covered don't check for it again
		//  in the end sum up all those fitness and it's the resulting suite-fitness
		
		// guess this is horribly inefficient but it's a start
		List<DefUseCoverageTestFitness> totalGoals = DefUseCoverageFactory.getDUGoals();
		List<DefUseCoverageTestFitness> coveredGoals = new ArrayList<DefUseCoverageTestFitness>(); 

		for(DefUseCoverageTestFitness goal : totalGoals) {
			double goalFitness = 2.0;
			for(ExecutionResult result : results) {
				TestChromosome tc = new TestChromosome();
				tc.test = result.test;
				double resultFitness = goal.getFitness(tc,result);
				if(resultFitness<goalFitness)
					goalFitness=resultFitness;
				if(goalFitness == 0.0) {
					result.test.addCoveredGoal(goal);
//					System.out.println(goal.toString());
//					System.out.println(result.test.toCode());
//					System.out.println(resultFitness);
					coveredGoals.add(goal);
					break;
				}
			}
			fitness += goalFitness;
		}
		
		suite.setCoverage(coveredGoals.size()/(double)totalGoals.size());
		updateIndividual(individual, fitness);
		return fitness;
	}
	
}
