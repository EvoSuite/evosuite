/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic;

import java.util.Collection;
import java.util.Map;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.search.ConstraintSolverTimeoutException;

/**
 * Interface for SMT solvers
 * 
 * @author Gordon Fraser
 */
public interface Solver {
	// Set<SymbolicParameter> solveConjunction(ConjunctiveCombination
	// conjunction);

	/**
	 * Get concrete values for the parameters used in the path conditions.
	 * 
	 * @return A {@link Map} where the name of the parameter is the key and the
	 *         concrete value that the solver used is the object.
	 * @param constraints
	 *            a {@link java.util.Collection} object.
	 */
	public Map<String, Object> solve(Collection<Constraint<?>> constraints)
			throws ConstraintSolverTimeoutException;
}
