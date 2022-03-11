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
import org.evosuite.symbolic.expr.bv.StringUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.*;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestIsInteger extends RandomizedTC {

    @Test
    public void testIsInteger() throws SolverEmptyQueryException {

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(
                new StringUnaryToIntegerExpression(new StringVariable("var0",
                        "hello"), Operator.IS_INTEGER, 0L), Comparator.NE,
                new IntegerConstant(0)));

        EvoSuiteSolver solver = new EvoSuiteSolver();
        try {
            SolverResult result = solver.solve(constraints);
            assertTrue(result.isSAT());
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }
}
