/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.solver;

import org.evosuite.Properties;
import org.evosuite.symbolic.solver.avm.EvoSuiteSolver;
import org.evosuite.symbolic.solver.cvc4.CVC4Solver;
import org.evosuite.symbolic.solver.z3.Z3Solver;
import org.evosuite.symbolic.solver.z3str2.Z3Str2Solver;

public class SolverFactory {

	private static final SolverFactory instance = new SolverFactory();

	public static SolverFactory getInstance() {
		return instance;
	}

	public Solver buildNewSolver() {
		switch (Properties.DSE_SOLVER) {
		case Z3_SOLVER:
			return new Z3Solver(true);
		case Z3_STR2_SOLVER:
			return new Z3Str2Solver(true);
		case CVC4_SOLVER: {
			CVC4Solver solver = new CVC4Solver(true);
			solver.setRewriteNonLinearConstraints(true);
			return solver;
		}
		case EVOSUITE_SOLVER:
		default:
			return new EvoSuiteSolver();
		}
	}

}
