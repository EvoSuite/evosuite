package org.evosuite.symbolic.search;

import java.util.Collection;
import java.util.Map;

import org.evosuite.symbolic.Solver;
import org.evosuite.symbolic.expr.Constraint;

public final class CachedConstraintSolver implements Solver {

	private ConstraintSolver solver = new ConstraintSolver();

	public CachedConstraintSolver() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map<String, Object> solve(Collection<Constraint<?>> constraints)
			throws ConstraintSolverTimeoutException {
		ConstraintCache cache = ConstraintCache.getInstance();

		if (cache.hasCachedResult(constraints)) {
			Map<String, Object> cached_solution = cache.getCachedResult();
			if (cached_solution != null) {
				return cached_solution;
			} else {
				return null;
			}
		}

		Map<String, Object> solution = this.solver.solve(constraints);

		if (solution == null) {
			cache.addUNSAT(constraints);
		} else {
			cache.addSAT(constraints, solution);
		}

		return solution;

	}

}
