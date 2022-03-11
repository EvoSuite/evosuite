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

import org.evosuite.RandomizedTC;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.constraint.StringConstraint;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.evosuite.symbolic.solver.TestSolver.solve;
import static org.junit.Assert.*;

public class TestStringEqualsIgnoreCase extends RandomizedTC {

    @Test
    public void testStringEqualsIgnoreCase() throws SecurityException, NoSuchMethodException, SolverTimeoutException {

        IntegerConstant zero = new IntegerConstant(0);
        StringVariable stringVar0 = new StringVariable("var0", "");
        StringConstant strConst = new StringConstant("bar");

        StringBinaryComparison cmp1 = new StringBinaryComparison(stringVar0, Operator.EQUALS, strConst, 0L);
        StringConstraint constr1 = new StringConstraint(cmp1, Comparator.EQ, zero);

        StringBinaryComparison cmp2 = new StringBinaryComparison(stringVar0, Operator.EQUALSIGNORECASE, strConst, 1L);
        StringConstraint constr2 = new StringConstraint(cmp2, Comparator.NE, zero);

        Collection<Constraint<?>> constraints = Arrays.<Constraint<?>>asList(constr1, constr2);

        EvoSuiteSolver solver = new EvoSuiteSolver();

        Map<String, Object> solution = solve(solver, constraints);
        assertNotNull(solution);
        String var0 = (String) solution.get("var0");

        assertNotNull(var0);
        assertFalse(var0.equals("bar"));
        assertTrue(var0.equalsIgnoreCase("bar"));
    }

}
