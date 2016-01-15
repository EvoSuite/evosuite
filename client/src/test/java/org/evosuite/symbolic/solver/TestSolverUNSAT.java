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
