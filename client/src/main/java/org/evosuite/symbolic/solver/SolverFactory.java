package org.evosuite.symbolic.solver;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.search.CachedConstraintSolver;

public class SolverFactory {

	private static final String Z3_SOLVER_CLASS_NAME = "org.evosuite.symbolic.z3.Z3Solver";
	private static final SolverFactory instance = new SolverFactory();

	public static SolverFactory getInstance() {
		return instance;
	}

	public Solver buildNewSolver() {
		switch (Properties.DSE_SOLVER) {
		case Z3_SOLVER:
			return createZ3Solver();
		case SEARCH_BASED_SOLVER:
		default:
			return new CachedConstraintSolver();
		}
	}

	private static Solver createZ3Solver() {
		try {
			Class<?> clazz = Class
					.forName(Z3_SOLVER_CLASS_NAME);
			Solver z3Solver = (Solver) clazz.newInstance();
			return z3Solver;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Z3 Solver was not found", e);
		} catch (InstantiationException e) {
			throw new RuntimeException("Z3 Solver could not be created", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(
					"Illegal Access when creating new Z3 Solver", e);
		}
	}

}
