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
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.constraint.StringConstraint;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;

public class TestConstraintSolver2 extends RandomizedTC {

    private static final String INIT_STRING = "abc_e";
    private static final String EXPECTED_STRING = "abcbb";

    private static Collection<Constraint<?>> buildConstraintSystem() {

        StringVariable var0 = new StringVariable("var0", INIT_STRING);

        StringConstant const0 = new StringConstant(EXPECTED_STRING);

        StringBinaryComparison strEqual = new StringBinaryComparison(var0, Operator.EQUALS, const0, (long) 0);

        IntegerConstant const_zero = new IntegerConstant(0);

        StringConstraint constr1 = new StringConstraint(strEqual, Comparator.NE, const_zero);

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
        System.out.println("Initial: " + INIT_STRING);

        EvoSuiteSolver solver = new EvoSuiteSolver();
        try {
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();
            assertNotNull(model);

            Object var0 = model.get("var0");
            System.out.println("Expected: " + EXPECTED_STRING);
            System.out.println("Found: " + var0);

            assertEquals(EXPECTED_STRING, var0);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }

    }

    public void test2() {
        String l1 = "hello";
        String l2 = "world";
        if (l1.equals(l2)) {
            System.out.println("xx");
        }
    }

}
