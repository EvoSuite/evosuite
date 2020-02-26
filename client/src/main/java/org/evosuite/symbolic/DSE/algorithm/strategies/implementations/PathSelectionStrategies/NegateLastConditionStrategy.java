package org.evosuite.symbolic.DSE.algorithm.strategies.implementations.PathSelectionStrategies;

import org.evosuite.symbolic.DSE.algorithm.strategies.PathSelectionStrategy;
import org.evosuite.symbolic.PathCondition;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.SolverUtils;

import java.util.List;

public class NegateLastConditionStrategy implements PathSelectionStrategy {
    @Override
    public List<Constraint<?>> getNextPathConstraints(PathCondition currentPathCondition) {
        return SolverUtils.buildQueryNegatingIthCondition(
                currentPathCondition,
                currentPathCondition.size() - 1
        );
    }
}
