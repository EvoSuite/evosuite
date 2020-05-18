/**
 * Copyright (C) 2010-2020 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.dse.algorithm.strategies;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.SolverResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Interface for path pruning strategies
 *
 * @author ignacio lebrero
 */
public interface PathPruningStrategy {
    boolean shouldSkipCurrentPath(HashSet<Set<Constraint<?>>> alreadyGeneratedPathConditions, Set<Constraint<?>> constraintSet, Map<Set<Constraint<?>>, SolverResult> queryCache);
}
