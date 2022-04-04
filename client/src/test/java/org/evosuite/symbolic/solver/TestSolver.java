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

import org.evosuite.Properties;
import org.evosuite.symbolic.expr.Constraint;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public abstract class TestSolver {

    private static final String DEFAULT_Z3_PATH = Properties.Z3_PATH;

    private static final String DEFAULT_CVC4_PATH = Properties.CVC4_PATH;

    @BeforeClass
    public static void configureSolverPath() {
        String z3_path = System.getenv("z3_path");
        if (z3_path != null) {
            Properties.Z3_PATH = z3_path;
        }
        String cvc4_path = System.getenv("cvc4_path");
        if (cvc4_path != null) {
            Properties.CVC4_PATH = cvc4_path;
        }
    }

    @AfterClass
    public static void restorePaths() {
        Properties.Z3_PATH = DEFAULT_Z3_PATH;
        Properties.CVC4_PATH = DEFAULT_CVC4_PATH;
    }

    @Before
    public void checkSolverPaths() {
        Assume.assumeTrue(Properties.Z3_PATH != null);
        Assume.assumeTrue(Properties.CVC4_PATH != null);
    }

    public abstract Solver getSolver();

    public static Map<String, Object> solve(Solver solver, Collection<Constraint<?>> constraints)
            throws SolverTimeoutException {
        SolverResult solverResult;
        try {
            solverResult = solver.solve(constraints);
            if (solverResult.isUNSAT() || solverResult.isUnknown()) {
                return null;
            } else {
                Map<String, Object> model = solverResult.getModel();
                return model;
            }
        } catch (SolverEmptyQueryException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (SolverParseException e) {
            return null;
        } catch (SolverErrorException e) {
            return null;
        }
    }
}
