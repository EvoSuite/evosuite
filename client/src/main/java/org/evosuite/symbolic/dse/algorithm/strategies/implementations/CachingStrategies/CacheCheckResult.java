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

import org.evosuite.symbolic.dse.algorithm.DSEExplorationException;

import java.util.Map;

/**
 * Result object of a cache checking.
 *
 * @author Ignacio Lebrero.
 */
public class CacheCheckResult {

    public static final String ONLY_SOLUTIONS_FROM_SATISFIABLE_QUERIES_CAN_BE_RETRIEVED = "Only solutions from satisfiable queries can be retrieved.";

    private Map<String, Object> smtSolution;
    private final CacheQueryStatus cacheQueryStatus;

    public CacheCheckResult(CacheQueryStatus cacheQueryState) {
        this.cacheQueryStatus = cacheQueryState;
    }

    public CacheCheckResult(Map<String, Object> smtSolution, CacheQueryStatus cacheQueryState) {
        this.smtSolution = smtSolution;
        this.cacheQueryStatus = cacheQueryState;
    }

    public Map<String, Object> getSmtSolution() {
        if (!cacheQueryStatus.equals(CacheQueryStatus.HIT_SAT)) throw new DSEExplorationException(
                ONLY_SOLUTIONS_FROM_SATISFIABLE_QUERIES_CAN_BE_RETRIEVED);
        return smtSolution;
    }

    public boolean isSat() {
        return this.cacheQueryStatus.equals(CacheQueryStatus.HIT_SAT);
    }

    public boolean isUnSat() {
        return this.cacheQueryStatus.equals(CacheQueryStatus.HIT_UNSAT);
    }

    public boolean isMissed() {
        return this.cacheQueryStatus.equals(CacheQueryStatus.MISS);
    }
}
