/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import static org.evosuite.symbolic.SymbolicObserverTest.printConstraints;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.evosuite.symbolic.solver.TestSolver.solve;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.evosuite.Properties;
import org.evosuite.RandomizedTC;
import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.ConcolicExecution;
import org.evosuite.symbolic.TestCaseBuilder;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.solver.SolverTimeoutException;
import org.evosuite.symbolic.solver.avm.EvoSuiteSolver;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.junit.Test;

import com.examples.with.different.packagename.concolic.StringSearch2;

public class TestStringSearch2 extends RandomizedTC {

	@Test
	public void testValidPathURN() {
		String pathURN = "urn:path:/A/B/C/doc.html#gilada";
		StringSearch2.checkPathURN(pathURN);
	}

	@Test
	public void testValidPathURN2() {
		String pathURN = "u:path:/";
		StringSearch2.checkPathURN(pathURN);
	}

	@Test
	public void testInvalidPathURN() {
		try {
			String pathURN = "urn:paxth:/A/B/C/doc.html#gilada";
			StringSearch2.checkPathURN(pathURN);
			fail();
		} catch (RuntimeException ex) {
		}
	}

	@Test
	public void testCreatePathConstraint() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase("urn:pBth:/A/B/C/doc.html#gilada");
		List<BranchCondition> branch_conditions = executeTest(tc);
		assertEquals(11, branch_conditions.size());
	}

	@Test
	public void testSolvePathConstraint() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase("urn:pBth:/A/B/C/doc.html#gilada");
		List<BranchCondition> branch_conditions = executeTest(tc);

		Collection<Constraint<?>> constraints = new ArrayList<Constraint<?>>();

		for (int i = 0; i < branch_conditions.size() - 1; i++) {
			BranchCondition b = branch_conditions.get(i);
			constraints.addAll(b.getSupportingConstraints());
			constraints.add(b.getConstraint());
		}

		BranchCondition last_branch = branch_conditions.get(branch_conditions.size() - 1);
		constraints.addAll(last_branch.getSupportingConstraints());
		constraints.add(last_branch.getConstraint().negate());

		EvoSuiteSolver solver = new EvoSuiteSolver();
		Map<String, Object> solution;
		try {
			solution = solve(solver, constraints);
			assertNotNull(solution);
			System.out.println(solution);
		} catch (SolverTimeoutException e) {
			fail();
		}

	}

	@Test
	public void testSolveIndexOfConstant() throws SecurityException, NoSuchMethodException {
		DefaultTestCase tc = buildTestCase("V*X-:o%tp");
		List<BranchCondition> branch_conditions = executeTest(tc);

		Collection<Constraint<?>> constraints = new ArrayList<Constraint<?>>();
		for (int i = 0; i < branch_conditions.size() - 2; i++) {
			BranchCondition b = branch_conditions.get(i);
			constraints.addAll(b.getSupportingConstraints());
			constraints.add(b.getConstraint());
		}
		BranchCondition last_branch = branch_conditions.get(branch_conditions.size() - 2);

		constraints.addAll(last_branch.getSupportingConstraints());
		constraints.add(last_branch.getConstraint().negate());

		EvoSuiteSolver solver = new EvoSuiteSolver();
		Map<String, Object> solution;
		try {
			solution = solve(solver, constraints);
			assertNotNull(solution);
			System.out.println(solution);
		} catch (SolverTimeoutException e) {
			fail();
		}

	}

	private DefaultTestCase buildTestCase(String stringVal) throws SecurityException, NoSuchMethodException {
		TestCaseBuilder tc = new TestCaseBuilder();
		VariableReference string0 = tc.appendStringPrimitive(stringVal);
		Method method = StringSearch2.class.getMethod("checkPathURN", String.class);
		tc.appendMethod(null, method, string0);
		return tc.getDefaultTestCase();
	}

	private List<BranchCondition> executeTest(DefaultTestCase tc) {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000;
		Properties.CONCOLIC_TIMEOUT = 5000000;

		System.out.println("TestCase=");
		System.out.println(tc.toCode());

		// ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = ConcolicExecution.executeConcolic(tc);

		printConstraints(branch_conditions);
		return branch_conditions;
	}

}
