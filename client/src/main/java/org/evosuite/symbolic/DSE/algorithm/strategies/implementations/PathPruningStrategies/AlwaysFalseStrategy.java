package org.evosuite.symbolic.DSE.algorithm.strategies.implementations.PathPruningStrategies;

import org.evosuite.symbolic.DSE.algorithm.strategies.PathPruningStrategy;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.SolverResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AlwaysFalseStrategy implements PathPruningStrategy {
    @Override
    public boolean shouldSkipCurrentPath(HashSet<Set<Constraint<?>>> alreadyGeneratedPathConditions, Set<Constraint<?>> constraintSet, Map<Set<Constraint<?>>, SolverResult> queryCache) {
        return false;
    }
}
