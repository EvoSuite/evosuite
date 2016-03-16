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
