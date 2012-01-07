/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;

/**
 * @author fraser
 * 
 */
public class WeakMutationTestFitness extends MutationTestFitness {

	private static final long serialVersionUID = 7468742584904580204L;

	/**
	 * @param mutation
	 */
	public WeakMutationTestFitness(Mutation mutation) {
		super(mutation);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.coverage.mutation.MutationTestFitness#getFitness(de.unisb.cs.st.evosuite.testcase.TestChromosome, de.unisb.cs.st.evosuite.testcase.ExecutionResult)
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		double fitness = 0.0;

		double executionDistance = diameter;

		// Get control flow distance
		if (!result.getTrace().touchedMutants.contains(mutation.getId()))
			executionDistance = getExecutionDistance(result);
		else
			executionDistance = 0.0;

		double infectionDistance = 1.0;

		// If executed...
		if (executionDistance <= 0) {
			// Add infection distance
			assert (result.getTrace() != null);
			assert (result.getTrace().mutant_distances != null);
			assert (mutation != null);
			assert (result.getTrace().touchedMutants.contains(mutation.getId()));
			infectionDistance = normalize(result.getTrace().mutant_distances.get(mutation.getId()));
			logger.debug("Infection distance for mutation = " + infectionDistance);
		}

		fitness = infectionDistance + executionDistance;
		logger.debug("Individual fitness: " + " + " + infectionDistance + " + "
		        + executionDistance + " = " + fitness);

		updateIndividual(individual, fitness);
		if (fitness == 0.0) {
			individual.getTestCase().addCoveredGoal(this);
		}
		return fitness;
	}

}
