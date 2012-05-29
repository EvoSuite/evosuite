/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.symbolic;

import java.util.Collection;
import java.util.Map;

import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;

/**
 * Interface for SMT solvers
 * 
 * @author Gordon Fraser
 * 
 */
public interface Solver {
	// Set<SymbolicParameter> solveConjunction(ConjunctiveCombination
	// conjunction);

	/**
	 * Get concrete values for the parameters used in the path conditions.
	 * 
	 * @return A {@link Map} where the name of the parameter is the key and the
	 *         concrete value that the solver used is the object.
	 */
	Map<String, Object> getModel(Collection<Constraint<?>> constraints);

	/**
	 * Determines whether the given PathConditions could be solved.
	 * 
	 * @return <code>true</code> if the path conditions could be solved, and a
	 *         concrete model is obtainable via {@link #getConcreteModel()}.
	 */
	boolean solve(Collection<Constraint<?>> constraints);
}
