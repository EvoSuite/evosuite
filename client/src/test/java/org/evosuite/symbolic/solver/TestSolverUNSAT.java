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
package org.evosuite.symbolic.solver;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import static org.junit.Assert.assertTrue;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.vm.ConstraintFactory;

public class TestSolverUNSAT {

	public static void testUNSAT(Solver solver) throws SolverTimeoutException, IOException, SolverParseException,
			SolverEmptyQueryException, SolverErrorException {
		Collection<Constraint<?>> constraints = new LinkedList<Constraint<?>>();
		IntegerVariable x = new IntegerVariable("x", 1L, Long.MIN_VALUE, Long.MAX_VALUE);
		IntegerConstraint unsat_constraint = ConstraintFactory.neq(x, x);
		constraints.add(unsat_constraint);
		SolverResult result = solver.solve(constraints);
		assertTrue(result.isUNSAT());
	}
}
