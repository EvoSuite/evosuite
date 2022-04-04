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
import org.evosuite.symbolic.solver.*;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestPatternSearch extends RandomizedTC {

    @Test
    public void testMatcherMatches() throws SolverEmptyQueryException {

        String input = "random_value";
        String format = "(\\d+)-(\\d\\d)-(\\d)";
        // String format = "^(\\d+)-(\\d\\d)-(\\d)$";

        StringVariable var0 = new StringVariable("var0", input);

        StringConstant symb_regex = ExpressionFactory
                .buildNewStringConstant(format);
        StringBinaryComparison strComp = new StringBinaryComparison(symb_regex,
                Operator.PATTERNMATCHES, var0, 0L);

        StringConstraint constraint = new StringConstraint(strComp,
                Comparator.NE, new IntegerConstant(0));

        List<Constraint<?>> constraints = Collections
                .<Constraint<?>>singletonList(constraint);

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult result = solver.solve(constraints);
            assertTrue(result.isSAT());

            Map<String, Object> model = result.getModel();

            String var0_value = (String) model.get("var0");

            Pattern pattern = Pattern.compile(format);
            Matcher matcher = pattern.matcher(var0_value);
            assertTrue(matcher.matches());
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException | SolverEmptyQueryException e) {
            fail();
        }

    }

}
