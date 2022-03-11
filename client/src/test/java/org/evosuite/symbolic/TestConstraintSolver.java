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
package org.evosuite.symbolic;

import com.examples.with.different.packagename.concolic.TestInput1;
import com.examples.with.different.packagename.concolic.TestInput2;
import org.evosuite.Properties;
import org.evosuite.symbolic.dse.ConcolicExecutorImpl;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.*;
import org.evosuite.symbolic.solver.avm.EvoSuiteSolver;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TestConstraintSolver {

    private List<BranchCondition> executeTest(DefaultTestCase tc) {
        Properties.CLIENT_ON_THREAD = true;
        Properties.PRINT_TO_SYSTEM = true;
        Properties.TIMEOUT = 5000000;
        Properties.CONCOLIC_TIMEOUT = 5000000;

        System.out.println("TestCase=");
        System.out.println(tc.toCode());

        PathCondition pc = new ConcolicExecutorImpl().execute(tc);
        List<BranchCondition> branch_conditions = pc.getBranchConditions();

        return branch_conditions;
    }

    private DefaultTestCase buildTestCase1() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendIntPrimitive(-15);
        VariableReference long0 = tc.appendLongPrimitive(Long.MAX_VALUE);
        VariableReference string0 = tc.appendStringPrimitive("Togliere sta roba");

        Method method = TestInput1.class.getMethod("test", int.class, long.class, String.class);
        tc.appendMethod(null, method, int0, long0, string0);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testCase1() throws SecurityException, NoSuchMethodException, SolverEmptyQueryException {
        DefaultTestCase tc = buildTestCase1();
        // build patch condition
        List<BranchCondition> branch_conditions = executeTest(tc);
        assertEquals(2, branch_conditions.size());

        // invoke seeker
        try {
            SolverResult solverResult = executeSolver(branch_conditions);
            assertNotNull(solverResult);
            assertTrue(solverResult.isSAT());
        } catch (SolverTimeoutException e) {
            fail();
        }

    }

    private SolverResult executeSolver(List<BranchCondition> branch_conditions)
            throws SolverTimeoutException, SolverEmptyQueryException {

        final int lastBranchIndex = branch_conditions.size() - 1;
        BranchCondition last_branch = branch_conditions.get(lastBranchIndex);

        List<Constraint<?>> constraints = new LinkedList<>();

        for (int i = 0; i < lastBranchIndex; i++) {
            BranchCondition c = branch_conditions.get(i);
            constraints.addAll(c.getSupportingConstraints());
            constraints.add(c.getConstraint());
        }

        constraints.addAll(last_branch.getSupportingConstraints());
        Constraint<?> lastConstraint = last_branch.getConstraint();

        Constraint<?> targetConstraint = lastConstraint.negate();

        constraints.add(targetConstraint);

        System.out.println("Target constraints");
        printConstraints(constraints);

        EvoSuiteSolver solver = new EvoSuiteSolver();
        SolverResult solverResult;
        try {
            solverResult = solver.solve(constraints);
        } catch (IOException | SolverParseException | SolverErrorException e) {
            solverResult = null;
        }

        if (solverResult.isUNSAT())
            System.out.println("No new model was found");
        else {
            Map<String, Object> model = solverResult.getModel();
            System.out.println(model.toString());
        }

        return solverResult;
    }

    private static void printConstraints(List<Constraint<?>> constraints) {
        for (Constraint<?> constraint : constraints) {
            System.out.println(constraint);
        }

    }

    /**
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    private DefaultTestCase buildTestCase2() throws SecurityException, NoSuchMethodException {
        TestCaseBuilder tc = new TestCaseBuilder();
        VariableReference int0 = tc.appendIntPrimitive(5);
        VariableReference int1 = tc.appendIntPrimitive(16);
        VariableReference int2 = tc.appendIntPrimitive(16);
        VariableReference int3 = tc.appendIntPrimitive(22);
        VariableReference int4 = tc.appendIntPrimitive(22);

        Method method = TestInput2.class.getMethod("test", int.class, int.class, int.class, int.class, int.class);
        tc.appendMethod(null, method, int0, int1, int2, int3, int4);
        return tc.getDefaultTestCase();
    }

    @Test
    public void testCase2() throws SecurityException, NoSuchMethodException, SolverEmptyQueryException {
        DefaultTestCase tc = buildTestCase2();
        // build patch condition
        List<BranchCondition> branchConditions = executeTest(tc);
        assertEquals(85, branchConditions.size());
        assertEquals(58, getBranchConditions(branchConditions).size());

        // keep only 2 top-most branch conditions
        List<BranchCondition> sublist = new ArrayList<>();
        sublist.add(branchConditions.get(0));
        sublist.add(branchConditions.get(1));

        // invoke seeker
        try {
            SolverResult solverResult = executeSolver(sublist);
            assertNotNull(solverResult);
            assertTrue(solverResult.isSAT());
        } catch (SolverTimeoutException e) {
            fail();
        }

    }

    private List<BranchCondition> getBranchConditions(List<BranchCondition> branchConditions) {
        return branchConditions.stream().filter(node -> node instanceof IfBranchCondition || node instanceof SwitchBranchCondition).collect(Collectors.toList());
    }

}
