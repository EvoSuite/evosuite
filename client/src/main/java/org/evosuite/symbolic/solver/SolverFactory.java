package org.evosuite.symbolic.solver;

import org.evosuite.symbolic.solver.search.CachedConstraintSolver;

public class SolverFactory {
	
	private static final SolverFactory instance = new SolverFactory();
	public static SolverFactory getInstance() {
		return instance;
	}
	
	public Solver buildNewSolver() {
		return new CachedConstraintSolver();
	}

}
