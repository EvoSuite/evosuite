/**
 * Copyright (C) 2010-2020 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import org.evosuite.symbolic.expr.Constraint;

import java.io.IOException;
import java.util.List;

/**
 * Logic for calling the SMT solver
 * TODO: in the future it could a good idea to avoid using static objects and move
 * 		 to a dependency injection schema.
 *
 * @author ilebrero
 *
 */
public abstract class SmtUtils {

    /**
	 * solves a given query (i.e. list of constraints).
	 *
	 * @param query
	 * @return
	 */
	public static SolverResult solveSMTQuery(List<Constraint<?>> query) {
		Solver solver = SolverFactory.getInstance().buildNewSolver();
		SolverResult solverResult = null;

		try {
			solverResult = solver.solve(query);
		} catch (SolverTimeoutException | SolverParseException | SolverEmptyQueryException | SolverErrorException | IOException e) {
//			TODO: see how we are going to handle this later on
			solverResult = null;
		}

		return solverResult;
	}

}
