/**
 * 
 */
package de.unisb.cs.st.evosuite.branch;

import de.unisb.cs.st.evosuite.branch.BranchCoverageGoal.Distance;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.ga.Chromosome;

/**
 * @author Gordon Fraser
 *
 */
public class BranchCoverageFitnessFunction extends TestFitnessFunction {

	/** Target branch */
	private BranchCoverageGoal goal;
	
	/**
	 * Constructor - fitness is specific to a branch
	 */
	public BranchCoverageFitnessFunction(BranchCoverageGoal goal) {
		this.goal = goal;
	}

	
	/**
	 * Calculate approach level + branch distance
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		Distance distance = goal.getDistance(result); 
		double fitness = 0.0; 
	
		//double approach = goal.getApproachLevel(result);
		//double branch   = goal.getBranchDistance(result);
		
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

}
