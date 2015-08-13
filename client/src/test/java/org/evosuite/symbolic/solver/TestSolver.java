package org.evosuite.symbolic.solver;

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
			return null;
		} catch (IOException e) {
			return null;
		} catch (SolverParseException e) {
			return null;
		} catch (SolverErrorException e) {
			return null;
		}
	}
}
