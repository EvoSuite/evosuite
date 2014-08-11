package org.evosuite.symbolic.solver;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.search.CachedConstraintSolver;

public class SolverFactory {

	private static final SolverFactory instance = new SolverFactory();

	public static SolverFactory getInstance() {
		return instance;
	}

	public Solver buildNewSolver() {
		switch (Properties.DSE_SOLVER) {
		case SEARCH_BASED:
		default:
			return new CachedConstraintSolver();
		}
	}

}
