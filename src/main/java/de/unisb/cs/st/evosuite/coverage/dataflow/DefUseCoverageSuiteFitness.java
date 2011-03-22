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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.unisb.cs.st.evosuite.cfg.CFGMethodAdapter;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteFitnessFunction;

/**
 * Evaluate fitness of a test suite with respect to all of its def-use pairs
 * 
 * @author 
 *
 */
public class DefUseCoverageSuiteFitness extends TestSuiteFitnessFunction {

	private static DefUseCoverageFactory goalFactory;
	
	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.FitnessFunction#getFitness(de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public double getFitness(Chromosome individual) {
		logger.trace("Calculating defuse fitness");

		long start = System.currentTimeMillis();
		
		TestSuiteChromosome suite = (TestSuiteChromosome)individual;
		long estart = System.currentTimeMillis();
		// this seems odd: why aren't the results calculated once for all suites?
		//				   seem very inefficient. TODO: talk to Gordon i guess, i seem to be missing a point here
		// ah ok nvm, a test holds it's last result if it wasn't changed so this is OK

		// TODO: gonna stop here for now, see above
		List<ExecutionResult> results = runTestSuite(suite);
		long eend = System.currentTimeMillis();
		double fitness = 0.0;

		// first simple and naive idea: 
		//  just take each DUGoal, calculate the minimal fitness over all results in the suite
		//  once a goal is covered don't check for it again
		//  in the end sum up all those fitness and it's the resulting suite-fitness
		
		// guess this is horribly inefficient but it's a start
		
		List<TestFitnessFunction> goals = getGoalFactory().getCoverageGoals();
		
		for(TestFitnessFunction goal : goals) {
			double goalFitness = 2;
			for(ExecutionResult result : results) {
//				double resultFitness = goal.getFitness(individual, result);
				// TODO research chromosomes ... can i make the given chromosome to a testchromosome?
			}
		}
		
//		
//		//logger.info("Fitness: "+fitness+", size: "+suite.size()+", length: "+suite.length());
//		updateIndividual(individual, fitness);
//
//		long end = System.currentTimeMillis();
//		if(end-start > 1000) {
//			logger.info("Executing tests took    : "+(eend-estart)+"ms");
//			logger.info("Calculating fitness took: "+(end-start)+"ms");
//		}
//		double coverage = num_covered;
//		for(String e : BranchPool.getBranchlessMethods()) {
//			if(call_count.keySet().contains(e))
//				coverage += 1.0;
//			
//		}
//		
//		suite.setCoverage(coverage / total_goals);
				
		return fitness;
	}
	
	private DefUseCoverageFactory getGoalFactory() {
		if(goalFactory==null)
			goalFactory = new DefUseCoverageFactory();
		
		return goalFactory;
	}

}
