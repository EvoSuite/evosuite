package org.evosuite.symbolic.DSE.algorithm.strategies;

import org.evosuite.symbolic.PathCondition;
import org.evosuite.symbolic.expr.Constraint;

import java.util.List;

public interface PathSelectionStrategy {
    List<Constraint<?>> getNextPathConstraints(PathCondition currentPathCondition);
}
