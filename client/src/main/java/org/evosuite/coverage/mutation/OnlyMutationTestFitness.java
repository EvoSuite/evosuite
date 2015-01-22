package org.evosuite.coverage.mutation;

import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;

/**
 * 
 * @author gordon
 *
 */
public class OnlyMutationTestFitness extends MutationTestFitness {

	private static final long serialVersionUID = -6724941216935595963L;

	public OnlyMutationTestFitness(Mutation m) {
		super(m);
	}
	
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double fitness = 0.0;

		// Get control flow distance
		if (!result.getTrace().wasMutationTouched(mutation.getId())) { 
			fitness = 1.0;
		} else {
			fitness = normalize(result.getTrace().getMutationDistance(mutation.getId()));
			logger.debug("Infection distance for mutation = " + fitness);
		}

		updateIndividual(this, individual, fitness);
		if (fitness == 0.0) {
			individual.getTestCase().addCoveredGoal(this);
		}
		return fitness;	
	}


	@Override
	public String getTargetMethod() {
		return "Weak " + mutation.toString();
	}

}
