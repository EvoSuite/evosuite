/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.dse.algorithm.strategies.implementations.CachingStrategies;

import org.evosuite.symbolic.PathConditionUtils;
import org.evosuite.symbolic.dse.DSEStatistics;
import org.evosuite.symbolic.dse.algorithm.strategies.CachingStrategy;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.SolverResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * Resembles checks (a) and (b) of a counter-example cache strategy.
 * (b) Is performed only if exactly the same constraint set is found.
 *     TODO: Implement the solution for supersets.
 * <p>
 * Counter-example cache strategy: Maps sets of constraints to counter-examples and performs three optimizations:
 * (a)  When a subset of a constraint set has no solution, then neither does the original set. i.e. as the query x>10 ∧ x<5 has no solution, neither does the original query x>10 ∧ x<5 ∧ y=0
 * (b)  When a superset of a constraint set has a solution, that solution also satisfies the original set.  i.e. x=14 is the solution for the query x>0 ∧ x<5, thus it satisfies either x>0 or x<5 individually
 * (c)  When a subset of a constraint set has a solution, it is likely that this is also a solution for the original set
 *
 * @author Ignacio Lebrero
 */
public class CounterExampleCache implements CachingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CounterExampleCache.class);
    private static final DSEStatistics statisticsLogger = DSEStatistics.getInstance();

    @Override
    public CacheQueryResult checkCache(Set<Constraint<?>> query, Map<Set<Constraint<?>>, SolverResult> queryCache) {
        statisticsLogger.reportNewQueryCacheCall();

        // Cache hit of an exact set solution
        if (queryCache.containsKey(query)) {
            SolverResult cachedResult = queryCache.get(query);

            if (cachedResult.isSAT()) {
                statisticsLogger.reportNewQueryCacheHit();
                logger.debug("skipping solving of current query since it is in the query cache");
                return new CacheQueryResult(cachedResult.getModel(), CacheQueryStatus.HIT_SAT);

            } else if (cachedResult.isUNSAT()) {
                statisticsLogger.reportNewQueryCacheHit();
                logger.debug("skipping current query since it is in the query cache and it unsatisfiable");
                return new CacheQueryResult(CacheQueryStatus.HIT_UNSAT);
            }
        }

        // Cache hit of a sub set solution
        if (PathConditionUtils.isConstraintSetSupraSetOf(query, queryCache.keySet())) {
            Set<Constraint<?>> subSetSolution = PathConditionUtils.getConstraintSetSupraSetOf(query, queryCache.keySet());
            SolverResult cachedResult = queryCache.get(subSetSolution);

            // Case (a) for sub sets: the query is a supra set of a sat solution. Heuristics can be implemented here
            if (cachedResult.isSAT()) {
                // TODO: implement me!
                return new CacheQueryResult(CacheQueryStatus.MISS);

                // Case (b) for sub sets: the query is a supra set of an unsat solution
            } else if (cachedResult.isUNSAT()) {
                statisticsLogger.reportNewQueryCacheHit();
                logger.debug("skipping current query since it is in the query cache and it unsatisfiable");
                return new CacheQueryResult(CacheQueryStatus.HIT_UNSAT);
            }

        }

        // Cache hit of a supra set solution
        if (PathConditionUtils.isConstraintSetSubSetOf(query, queryCache.keySet())) {
            Set<Constraint<?>> supraSetSolution = PathConditionUtils.getConstraintSetSubSetOf(query, queryCache.keySet());
            SolverResult cachedResult = queryCache.get(supraSetSolution);

            // Case (c) of Counter-example cache: Heuristics can be implemented here
            if (cachedResult.isUNSAT()) {
                // TODO: implement me!
                return new CacheQueryResult(CacheQueryStatus.MISS);

                // Case (b) for for supra sets: The query is sat as there was a bigger query that was SAT
            } else if (cachedResult.isSAT()) {
                // TODO: Implement me!
                // We cannot use all the solution model from the superset as it may differ with other elements of this path.
                // Implementation idea: reuse only the necessary elements of the model.
                return new CacheQueryResult(CacheQueryStatus.MISS);
            }
        }

        return new CacheQueryResult(CacheQueryStatus.MISS);
    }
}