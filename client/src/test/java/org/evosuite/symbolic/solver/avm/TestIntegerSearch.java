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
import org.evosuite.RandomizedTC;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.*;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.solver.*;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author fraser
 */
public class TestIntegerSearch extends RandomizedTC {

    private static final int DEFAULT_DSE_VARIABLE_RESETS = Properties.DSE_VARIABLE_RESETS;

    @After
    public void restoreDSEVariableResets() {
        Properties.DSE_VARIABLE_RESETS = DEFAULT_DSE_VARIABLE_RESETS;
    }

    @Test
    public void testEQConstant() throws SolverEmptyQueryException {
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", 0, -1000000, 1000000), Comparator.EQ,
                new IntegerConstant(235082)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();
            assertNotNull(model);
            assertNotNull(model.get("test1"));
            assertEquals(235082, ((Number) model.get("test1")).intValue());
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testNEConstant() throws SolverEmptyQueryException {
        // TODO: Currently, the model returned by the search is null if the
        // constraint is already satisfied,
        // so in this example the concrete value has to be the target initially
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", 235082, -1000000, 1000000), Comparator.NE,
                new IntegerConstant(235082)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            assertNotNull(model.get("test1"));
            assertTrue(235082 != ((Number) model.get("test1")).intValue());
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testLEConstant() throws SolverEmptyQueryException {
        // TODO: Currently, the model returned by the search is null if the
        // constraint is already satisfied,
        // so in this example the concrete value has to be the target initially
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", 235086, -1000000, 1000000), Comparator.LE,
                new IntegerConstant(235082)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();
            assertNotNull(model.get("test1"));
            assertTrue(235082 >= ((Number) model.get("test1")).intValue());
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testLTConstant() throws SolverEmptyQueryException {
        // TODO: Currently, the model returned by the search is null if the
        // constraint is already satisfied,
        // so in this example the concrete value has to be the target initially
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", 235086, -1000000, 1000000), Comparator.LT,
                new IntegerConstant(235082)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            assertNotNull(model.get("test1"));
            assertTrue(235082 > ((Number) model.get("test1")).intValue());
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testGEConstant() throws SolverEmptyQueryException {
        // TODO: Currently, the model returned by the search is null if the
        // constraint is already satisfied,
        // so in this example the concrete value has to be the target initially
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", 0, -1000000, 1000000), Comparator.GE,
                new IntegerConstant(235082)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            assertNotNull(model.get("test1"));
            assertTrue(235082 <= ((Number) model.get("test1")).intValue());
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testGTConstant() throws SolverEmptyQueryException {
        // TODO: Currently, the model returned by the search is null if the
        // constraint is already satisfied,
        // so in this example the concrete value has to be the target initially
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", 0, -1000000, 1000000), Comparator.GT,
                new IntegerConstant(235082)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            assertNotNull(model.get("test1"));
            assertTrue(235082 < ((Number) model.get("test1")).intValue());
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testEQVariable() throws SolverEmptyQueryException {
        int var1 = 0;
        int var2 = 1;
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.EQ,
                new IntegerVariable("test2", var2, -1000000, 1000000)));

        try {

            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();
            if (model.containsKey("test1"))
                var1 = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                var2 = ((Number) model.get("test2")).intValue();
            assertEquals(var1, var2);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testNEVariable() throws SolverEmptyQueryException {
        int var1 = 1;
        int var2 = 1;
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.NE,
                new IntegerVariable("test2", var2, -1000000, 1000000)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            if (model.containsKey("test1"))
                var1 = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                var2 = ((Number) model.get("test2")).intValue();
            assertTrue(var1 != var2);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testLEVariable() throws SolverEmptyQueryException {
        int var1 = 2;
        int var2 = 1;
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.LE,
                new IntegerVariable("test2", var2, -1000000, 1000000)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            if (model.containsKey("test1"))
                var1 = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                var2 = ((Number) model.get("test2")).intValue();
            assertTrue(var1 <= var2);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testLTVariable() throws SolverEmptyQueryException {
        int var1 = 2;
        int var2 = 1;
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.LT,
                new IntegerVariable("test2", var2, -1000000, 1000000)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            if (model.containsKey("test1"))
                var1 = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                var2 = ((Number) model.get("test2")).intValue();
            assertTrue(var1 < var2);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testGEVariable() throws SolverEmptyQueryException {
        int var1 = 0;
        int var2 = 1;
        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.GE,
                new IntegerVariable("test2", var2, -1000000, 1000000)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            if (model.containsKey("test1"))
                var1 = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                var2 = ((Number) model.get("test2")).intValue();
            assertTrue(var1 >= var2);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testGTVariable() throws SolverEmptyQueryException {
        int var1 = 0;
        int var2 = 1;
        Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS = Integer.MAX_VALUE;

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.GT,
                new IntegerVariable("test2", var2, -1000000, 1000000)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            if (model.containsKey("test1"))
                var1 = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                var2 = ((Number) model.get("test2")).intValue();
            assertTrue(var1 > var2);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testEQArithmetic() throws SolverEmptyQueryException {
        int var1 = 0;
        int var2 = 1;
        int var3 = 1;
        assertTrue(var1 != var2 + var3);

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.EQ,
                new IntegerBinaryExpression(new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
                        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            if (model.containsKey("test1"))
                var1 = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                var2 = ((Number) model.get("test2")).intValue();
            if (model.containsKey("test3"))
                var3 = ((Number) model.get("test3")).intValue();
            assertEquals(var1, var2 + var3);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testNEArithmetic() throws SolverEmptyQueryException {
        int var1 = 2;
        int var2 = 1;
        int var3 = 1;
        assertEquals(var1, var2 + var3);

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.NE,
                new IntegerBinaryExpression(new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
                        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            if (model.containsKey("test1"))
                var1 = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                var2 = ((Number) model.get("test2")).intValue();
            if (model.containsKey("test3"))
                var3 = ((Number) model.get("test3")).intValue();
            assertTrue(var1 != var2 + var3);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testLEArithmetic() throws SolverEmptyQueryException {
        int var1 = 3;
        int var2 = 1;
        int var3 = 1;
        assertTrue(var1 > var2 + var3);

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.LE,
                new IntegerBinaryExpression(new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
                        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            if (model.containsKey("test1"))
                var1 = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                var2 = ((Number) model.get("test2")).intValue();
            if (model.containsKey("test3"))
                var3 = ((Number) model.get("test3")).intValue();
            assertTrue(var1 <= var2 + var3);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testLTArithmetic() throws SolverEmptyQueryException {
        int var1 = 2;
        int var2 = 1;
        int var3 = 1;
        assertTrue(var1 >= var2 + var3);

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.LT,
                new IntegerBinaryExpression(new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
                        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            if (model.containsKey("test1"))
                var1 = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                var2 = ((Number) model.get("test2")).intValue();
            if (model.containsKey("test3"))
                var3 = ((Number) model.get("test3")).intValue();
            assertTrue(var1 < var2 + var3);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testGEArithmetic() throws SolverEmptyQueryException {
        int var1 = 0;
        int var2 = 1;
        int var3 = 1;
        assertTrue(var1 < var2 + var3);

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.GT,
                new IntegerBinaryExpression(new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
                        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            if (model.containsKey("test1"))
                var1 = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                var2 = ((Number) model.get("test2")).intValue();
            if (model.containsKey("test3"))
                var3 = ((Number) model.get("test3")).intValue();
            assertTrue(var1 >= var2 + var3);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testGTArithmetic() throws SolverEmptyQueryException {
        int var1 = 0;
        int var2 = 1;
        int var3 = 1;
        assertTrue(var1 <= var2 + var3);

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.GE,
                new IntegerBinaryExpression(new IntegerVariable("test2", var2, -1000000, 1000000), Operator.PLUS,
                        new IntegerVariable("test3", var3, -1000000, 1000000), 0L)));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS = Integer.MAX_VALUE;
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            if (model.containsKey("test1"))
                var1 = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                var2 = ((Number) model.get("test2")).intValue();
            if (model.containsKey("test3"))
                var3 = ((Number) model.get("test3")).intValue();
            assertTrue(var1 >= var2 + var3);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testEvosuiteExample1() throws SolverEmptyQueryException {
        int var1 = 1;
        int var2 = 1;

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.LE,
                new IntegerConstant(0)));
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.LT,
                new IntegerVariable("test2", var2, -1000000, 1000000)));
        constraints.add(new IntegerConstraint(new IntegerVariable("test1", var1, -1000000, 1000000), Comparator.GE,
                new IntegerConstant(0)));

        try {

            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            if (model.containsKey("test1"))
                var1 = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                var2 = ((Number) model.get("test2")).intValue();
            assertEquals(0, var1);
            assertTrue(var1 < var2);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    @Test
    public void testEvosuiteExample3() throws SolverEmptyQueryException {

        Properties.DSE_VARIABLE_RESETS = 3;

        // (var42__SYM(25721) * (var22__SYM(-1043) - 6860)) == 8275
        int var1 = 25721;
        int var2 = -1043;
        IntegerConstant iconst1 = new IntegerConstant(6860);
        IntegerConstant iconst2 = new IntegerConstant(8275);
        IntegerVariable ivar1 = new IntegerVariable("test1", var1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        IntegerVariable ivar2 = new IntegerVariable("test2", var2, Integer.MIN_VALUE, Integer.MAX_VALUE);
        IntegerBinaryExpression sub = new IntegerBinaryExpression(ivar2, Operator.MINUS, iconst1, -7903L);
        IntegerBinaryExpression mul = new IntegerBinaryExpression(ivar1, Operator.MUL, sub, -203273063L);

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(mul, Comparator.EQ, iconst2));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            if (model.containsKey("test1"))
                var1 = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                var2 = ((Number) model.get("test2")).intValue();

            assertEquals(8275, var1 * (var2 - 6860));
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }

    private static IntegerValue mul(IntegerValue left, IntegerValue right) {
        int left_val = left.getConcreteValue().intValue();
        int right_val = right.getConcreteValue().intValue();
        return new IntegerBinaryExpression(left, Operator.MUL, right, (long) left_val * right_val);
    }

    private static IntegerValue div(IntegerValue left, IntegerValue right) {
        int left_val = left.getConcreteValue().intValue();
        int right_val = right.getConcreteValue().intValue();
        return new IntegerBinaryExpression(left, Operator.DIV, right, (long) left_val / right_val);
    }

    private static IntegerValue sub(IntegerValue left, IntegerValue right) {
        int left_val = left.getConcreteValue().intValue();
        int right_val = right.getConcreteValue().intValue();
        return new IntegerBinaryExpression(left, Operator.MINUS, right, (long) left_val - right_val);
    }

    private static IntegerValue rem(IntegerValue left, IntegerValue right) {
        int left_val = left.getConcreteValue().intValue();
        int right_val = right.getConcreteValue().intValue();
        return new IntegerBinaryExpression(left, Operator.REM, right, (long) left_val % right_val);
    }

    @Test
    public void testEvosuiteExample4_1() throws SolverEmptyQueryException {
        IntegerVariable var24 = new IntegerVariable("var24", 21458, Integer.MIN_VALUE, Integer.MAX_VALUE);

        IntegerVariable var10 = new IntegerVariable("var10", 1172, Integer.MIN_VALUE, Integer.MAX_VALUE);

        IntegerVariable var14 = new IntegerVariable("var14", -1903, Integer.MIN_VALUE, Integer.MAX_VALUE);

        IntegerConstant c_19072 = new IntegerConstant(19072);
        IntegerConstant c_11060 = new IntegerConstant(11060);

        IntegerValue left = mul(sub(var24, div(var10, var14)), c_19072);
        IntegerValue right = c_11060;
        IntegerConstraint constr = new IntegerConstraint(left, Comparator.LT, right);

        List<Constraint<?>> constraints = Collections.<Constraint<?>>singletonList(constr);
        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            int v_24 = ((Number) model.get("var24")).intValue();
            int v_10 = ((Number) model.get("var10")).intValue();
            int v_14 = ((Number) model.get("var14")).intValue();

            assertTrue((v_24 - (v_10 / v_14) * 19072) < 11060);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }

    }

    @Test
    public void testEvosuiteExample4_2() throws SolverEmptyQueryException {

        IntegerVariable var20 = new IntegerVariable("var20", 17433, Integer.MIN_VALUE, Integer.MAX_VALUE);

        IntegerVariable var39 = new IntegerVariable("var39", -1819, Integer.MIN_VALUE, Integer.MAX_VALUE);

        IntegerVariable var40 = new IntegerVariable("var40", -1819, Integer.MIN_VALUE, Integer.MAX_VALUE);

        IntegerConstant c_11060 = new IntegerConstant(11060);
        IntegerConstant c_12089 = new IntegerConstant(12089);
        IntegerConstant c_14414 = new IntegerConstant(14414);

        IntegerValue left = sub(mul(c_12089, var40), rem(mul(var39, c_14414), var20));
        IntegerValue right = c_11060;
        IntegerConstraint constr = new IntegerConstraint(left, Comparator.GT, right);

        List<Constraint<?>> constraints = Collections.<Constraint<?>>singletonList(constr);
        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            int v_20 = ((Number) model.get("var20")).intValue();
            int v_39 = ((Number) model.get("var39")).intValue();
            int v_40 = ((Number) model.get("var40")).intValue();

            assertTrue((12089 * v_40) - ((v_39 * 14414) % v_20) > 11060);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }

    }

    @Test
    public void testEvosuiteExample5() throws SolverEmptyQueryException {
        // TestSuiteDSE.setStart();

        // Cnstr 0 : var6__SYM(84) != (y charAt 0) dist: 8.0
        // Cnstr 1 : var6__SYM(84) != 115 dist: 8.0
        // Cnstr 2 : var6__SYM(84) == 108 dist: 8.0

        int var1 = 84;
        int const1 = 115;
        int const2 = 108;
        String const3 = "y";

        IntegerConstant iconst1 = new IntegerConstant(const1);
        IntegerConstant iconst2 = new IntegerConstant(const2);
        StringConstant strConst = new StringConstant(const3);

        IntegerVariable ivar1 = new IntegerVariable("test1", var1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        StringBinaryToIntegerExpression sBExpr = new StringBinaryToIntegerExpression(strConst, Operator.CHARAT,
                new IntegerConstant(0), (long) "y".charAt(0));

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(ivar1, Comparator.NE, sBExpr));
        constraints.add(new IntegerConstraint(ivar1, Comparator.NE, iconst1));
        constraints.add(new IntegerConstraint(ivar1, Comparator.EQ, iconst2));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();
            var1 = ((Number) model.get("test1")).intValue();

            assertEquals(108, var1);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }

    }

    @Test
    public void testEvosuiteExample6() throws SolverEmptyQueryException {

        Properties.DSE_VARIABLE_RESETS = 3;

        // Cnstr 0 : var2__SYM(1890) >= 0 dist: 682.3333333333334
        // Cnstr 1 : var1__SYM(-157) <= 0 dist: 682.3333333333334
        // Cnstr 2 : var2__SYM(1890) <= var1__SYM(-157) dist: 682.3333333333334
        // y >= 0
        // x <= 0
        // y <= x

        int x = -157;
        int y = 1890;

        // TestSuiteDSE.setStart();

        // int x = 879254357;
        // int y = 1013652704;

        IntegerVariable ivar1 = new IntegerVariable("test1", x, Integer.MIN_VALUE, Integer.MAX_VALUE);
        IntegerVariable ivar2 = new IntegerVariable("test2", y, Integer.MIN_VALUE, Integer.MAX_VALUE);

        List<Constraint<?>> constraints = new ArrayList<>();
        constraints.add(new IntegerConstraint(ivar2, Comparator.GE, new IntegerConstant(0)));
        constraints.add(new IntegerConstraint(ivar1, Comparator.LE, new IntegerConstant(0)));
        constraints.add(new IntegerConstraint(ivar2, Comparator.LE, ivar1));

        try {
            EvoSuiteSolver solver = new EvoSuiteSolver();
            SolverResult solverResult = solver.solve(constraints);
            assertTrue(solverResult.isSAT());
            Map<String, Object> model = solverResult.getModel();

            if (model.containsKey("test1"))
                x = ((Number) model.get("test1")).intValue();
            if (model.containsKey("test2"))
                y = ((Number) model.get("test2")).intValue();
            assertTrue(y >= 0);
            assertTrue(x <= 0);
            assertTrue(y <= x);
        } catch (SolverTimeoutException | SolverParseException | SolverErrorException | IOException e) {
            fail();
        }
    }
}
