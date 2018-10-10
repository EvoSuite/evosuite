package org.evosuite.symbolic.solver.z3;

import org.evosuite.symbolic.solver.Solver;
import org.evosuite.symbolic.solver.TestSolverIsInteger;

public class TestZ3IsInteger extends TestSolverIsInteger {

	@Override
	public Solver getSolver() {
		return new Z3Solver();
	}

}
