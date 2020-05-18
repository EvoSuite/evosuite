package org.evosuite.symbolic.dse.algorithm.listener;

import org.evosuite.Properties;
import org.evosuite.symbolic.dse.algorithm.listener.implementations.MaxTimeStoppingCondition;
import org.evosuite.symbolic.dse.algorithm.listener.implementations.TargetCoverageReachedStoppingCondition;
import org.evosuite.symbolic.dse.algorithm.listener.implementations.ZeroFitnessStoppingCondition;

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
	public static StoppingCondition getStoppingCondition(Properties.DSEStoppingCondition stoppingCondition) {
		switch (stoppingCondition) {
		case MAXTIME:
			return new MaxTimeStoppingCondition();
        case TARGETCOVERAGE:
            return new TargetCoverageReachedStoppingCondition();
        case ZEROFITNESS:
            return new ZeroFitnessStoppingCondition();
		default:
			return new MaxTimeStoppingCondition();
		}
	}

}
