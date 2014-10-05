package org.evosuite.symbolic.solver;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.search.EvoSuiteSolver;

public class SolverFactory {

	private static final String Z3_SOLVER_CLASS_NAME = "org.evosuite.symbolic.z3.Z3Solver";

	private static final String Z3STR_SOLVER_CLASS_NAME = "org.evosuite.symbolic.z3str.Z3StrSolver";

	private static final SolverFactory instance = new SolverFactory();

	public static SolverFactory getInstance() {
		return instance;
	}

	public Solver buildNewSolver() {
		switch (Properties.DSE_SOLVER) {
		case Z3_SOLVER:
			return createZ3Solver();
		case Z3STR_SOLVER:
			return createZ3StrSolver();
		case EVOSUITE_SOLVER:
		default:
			return new EvoSuiteSolver();
		}
	}

	private Solver createZ3StrSolver() {
		try {
			Class<?> clazz = Class.forName(Z3STR_SOLVER_CLASS_NAME);
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

	private static Solver createZ3Solver() {
		try {
			Class<?> clazz = Class.forName(Z3_SOLVER_CLASS_NAME);
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
