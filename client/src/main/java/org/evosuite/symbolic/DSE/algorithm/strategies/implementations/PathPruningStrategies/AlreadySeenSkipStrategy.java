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
package org.evosuite.symbolic.DSE.algorithm.strategies.implementations.PathPruningStrategies;

import org.evosuite.symbolic.DSE.algorithm.strategies.PathPruningStrategy;
import org.evosuite.symbolic.PathConditionUtils;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.SolverResult;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AlreadySeenSkipStrategy implements PathPruningStrategy {

    private static final Logger logger = LoggerFactory.getLogger(AlreadySeenSkipStrategy.class);

    //TODO: recheck this conditions, is there something we can do with the unsat cached paths?
    @Override
    public boolean shouldSkipCurrentPath(HashSet<Set<Constraint<?>>> alreadyGeneratedPathConditions, Set<Constraint<?>> constraintSet, Map<Set<Constraint<?>>, SolverResult> queryCache) {
        if (queryCache.containsKey(constraintSet)) {
          logger.debug("skipping solving of current query since it is in the query cache");
          return true;
        }

        if (PathConditionUtils.isConstraintSetSubSetOf(constraintSet, queryCache.keySet())) {
          logger.debug(
              "skipping solving of current query because it is satisfiable and solved by previous path condition");
          return true;
        }

        if (alreadyGeneratedPathConditions.contains(constraintSet)) {
          logger.debug("skipping solving of current query because of existing path condition");
          return true;
        }

        if (PathConditionUtils.isConstraintSetSubSetOf(constraintSet, alreadyGeneratedPathConditions)) {
          logger.debug(
              "skipping solving of current query because it is satisfiable and solved by previous path condition");
          return true;
        }

        return false;
    }
}
