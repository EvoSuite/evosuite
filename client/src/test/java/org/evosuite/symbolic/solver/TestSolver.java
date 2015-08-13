package org.evosuite.symbolic.solver;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.evosuite.symbolic.expr.Constraint;

public abstract class TestSolver {

	public static Map<String, Object> solve(Solver solver, Collection<Constraint<?>> constraints)
			throws SolverTimeoutException {
		SolverResult solverResult;
		try {
			solverResult = solver.solve(constraints);
			if (solverResult.isUNSAT()) {
				return null;
			} else {
				Map<String, Object> model = solverResult.getModel();
				return model;
			}
		} catch (SolverEmptyQueryException e) {
			fail();
		} catch (IOException e) {
			fail();
		} catch (SolverParseException e) {
			fail();
		} catch (SolverErrorException e) {
			fail();
		}
		return null;
	}
}
