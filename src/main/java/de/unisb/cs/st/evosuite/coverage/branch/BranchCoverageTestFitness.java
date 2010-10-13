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


package de.unisb.cs.st.evosuite.coverage.branch;

import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageGoal.Distance;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;

/**
 * Fitness function for a single test on a single branch
 * 
 * @author Gordon Fraser
 *
 */
public class BranchCoverageTestFitness extends TestFitnessFunction {

	/** Target branch */
	private BranchCoverageGoal goal;
	
	/**
	 * Constructor - fitness is specific to a branch
	 */
	public BranchCoverageTestFitness(BranchCoverageGoal goal) {
		this.goal = goal;
	}
	
	/**
	 * Calculate approach level + branch distance
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		Distance distance = goal.getDistance(result); 
		double fitness = 0.0; 
	
		fitness = distance.approach + normalize(distance.branch);

		logger.debug("Approach level: "+distance.approach +" / branch distance: "+distance.branch+", fitness = "+fitness);
		
		updateIndividual(individual, fitness);
		return fitness;
	}

	/**
	 * Store information
	 */
	protected void updateIndividual(Chromosome individual, double fitness) {
		individual.setFitness(fitness);
	}
	
	public String toString() {
		return goal.toString();
	}

}
