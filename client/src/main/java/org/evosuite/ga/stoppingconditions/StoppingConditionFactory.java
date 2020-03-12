package org.evosuite.ga.stoppingconditions;

import org.evosuite.Properties;

/**
 * TODO: Move this class to a dependency injection schema.
 *
 * @author Ignacio Lebrero
 */
public class StoppingConditionFactory {

   	/**
	 * Convert property to actual stopping condition
	 * @return
	 */
	public static StoppingCondition getStoppingCondition(Properties.StoppingCondition stoppingCondition) {
		switch (stoppingCondition) {
		case MAXGENERATIONS:
			return new MaxGenerationStoppingCondition();
		case MAXFITNESSEVALUATIONS:
			return new MaxFitnessEvaluationsStoppingCondition();
		case MAXTIME:
			return new MaxTimeStoppingCondition();
		case MAXTESTS:
			return new MaxTestsStoppingCondition();
		case MAXSTATEMENTS:
			return new MaxStatementsStoppingCondition();
		default:
			return new MaxGenerationStoppingCondition();
		}
	}



}
