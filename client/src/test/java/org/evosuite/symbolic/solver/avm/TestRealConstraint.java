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
package org.evosuite.symbolic.solver.avm;

import org.evosuite.Properties;
import org.evosuite.Properties.LocalSearchBudgetType;
import org.evosuite.RandomizedTC;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.constraint.RealConstraint;
import org.evosuite.symbolic.expr.fp.RealConstant;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.solver.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestRealConstraint extends RandomizedTC {

    private static final double INIT_DOUBLE = 0;
    private static final double EXPECTED_DOUBLE = Math.PI;

    private static Collection<Constraint<?>> buildConstraintSystem() {

        RealVariable var0 = new RealVariable("var0", INIT_DOUBLE, Double.MIN_VALUE, Double.MAX_VALUE);

        RealConstant constPi = new RealConstant(Math.PI);

        RealConstraint constr1 = new RealConstraint(var0, Comparator.EQ, constPi);

        return Arrays.<Constraint<?>>asList(constr1);
    }

    @Test
    public void test() throws SolverEmptyQueryException {
        Properties.LOCAL_SEARCH_BUDGET = 100; // 5000000000000L; TODO - ??
        Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.FITNESS_EVALUATIONS;

        Collection<Constraint<?>> constraints = buildConstraintSystem();

        System.out.println("Constraints:");
        for (Constraint<?> c : constraints) {
            System.out.println(c.toString());
        }

        System.out.println("");
        System.out.println("Initial: " + String.valueOf(INIT_DOUBLE));

        EvoSuiteSolver seeker = new EvoSuiteSolver();
        try {
            SolverResult result = seeker.solve(constraints);
            if (result.isUNSAT()) {
                fail("search was unsuccessfull");
            } else {
                Map<String, Object> model = result.getModel();

                Object var0 = model.get("var0");
                System.out.println("Expected: " + EXPECTED_DOUBLE);
                System.out.println("Found: " + var0);

                assertEquals(EXPECTED_DOUBLE, var0);
            }
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }

    }
}
