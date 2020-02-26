package org.evosuite.symbolic.DSE.algorithm.strategies;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.SolverResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface PathPruningStrategy {
    boolean shouldSkipCurrentPath(HashSet<Set<Constraint<?>>> alreadyGeneratedPathConditions, Set<Constraint<?>> constraintSet, Map<Set<Constraint<?>>, SolverResult> queryCache);
}
