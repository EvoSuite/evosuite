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
package org.evosuite.symbolic.solver;

import org.evosuite.symbolic.expr.Constraint;

import java.util.Collection;
import java.util.HashMap;

public final class SolverCache {

    private static final SolverCache instance = new SolverCache();
    private static final String CONTRAINT_NOT_CACHED_EXCEPTION_MESSAGE = "The constraint is not cached!";
    private static final String SOLVER_RESULT_CANNOT_BE_NULL_EXCEPTION_MESSAGE = "Unable to save solver result as its null.";

    private int number_of_hits = 0;
    private int number_of_accesses = 0;
    private int cached_sat_result_count = 0;
    private int cached_unsat_result_count = 0;
    private boolean valid_cached_solution = false;

    private final HashMap<Collection<Constraint<?>>, SolverResult> cached_solver_results = new HashMap<>();
    private SolverResult cached_solution = null;

    public int getNumberOfUNSATs() {
        return cached_unsat_result_count;
    }

    public int getNumberOfSATs() {
        return cached_sat_result_count;
    }

    private SolverCache() {
        /* empty constructor */
    }

    public static SolverCache getInstance() {
        return instance;
    }

    private void addUNSAT(Collection<Constraint<?>> unsat_constraints, SolverResult unsatResult) {
        cached_solver_results.put(unsat_constraints, unsatResult);
        cached_unsat_result_count++;
    }

    private void addSAT(Collection<Constraint<?>> sat_constraints, SolverResult satResult) {
        cached_solver_results.put(sat_constraints, satResult);
        cached_sat_result_count++;
    }

    public boolean hasCachedResult(Collection<Constraint<?>> constraints) {
        number_of_accesses++;

        if (this.cached_solver_results.containsKey(constraints)) {
            valid_cached_solution = true;
            cached_solution = this.cached_solver_results.get(constraints);
            number_of_hits++;
            return true;
        } else {
            valid_cached_solution = false;
            return false;
        }
    }

    public double getHitRate() {
        return (double) this.number_of_hits / (double) this.number_of_accesses;
    }

    /**
     * If not in cache returns IllegalArgumentException()
     *
     * @return
     */
    public SolverResult getCachedResult() {

        if (valid_cached_solution == false) {
            throw new IllegalArgumentException(CONTRAINT_NOT_CACHED_EXCEPTION_MESSAGE);
        }

        valid_cached_solution = false;
        return this.cached_solution;
    }

    /**
     * Saves result to cache
     *
     * @param constraints
     * @param solverResult
     */
    public void saveSolverResult(Collection<Constraint<?>> constraints, SolverResult solverResult) {
        if (solverResult == null) {
            throw new IllegalArgumentException(SOLVER_RESULT_CANNOT_BE_NULL_EXCEPTION_MESSAGE);
        }

        if (solverResult.isUNSAT()) {
            addUNSAT(constraints, solverResult);
        } else {
            addSAT(constraints, solverResult);
        }
    }

}
