package org.evosuite.symbolic.solver;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.search.EvoSuiteSolver;
import org.evosuite.symbolic.solver.z3.Z3Solver;
import org.evosuite.symbolic.solver.z3str.Z3StrSolver;

public class SolverFactory {

	private static final SolverFactory instance = new SolverFactory();

	public static SolverFactory getInstance() {
		return instance;
	}

	public Solver buildNewSolver() {
		switch (Properties.DSE_SOLVER) {
		case Z3_SOLVER:
			return new Z3Solver();
		case Z3_STR_SOLVER:
			return new Z3StrSolver();
		case EVOSUITE_SOLVER:
		default:
			return new EvoSuiteSolver();
		}
	}

}
