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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for SMT solvers
 * 
 * @author Gordon Fraser
 */
public abstract class Solver {

	private final boolean addMissingVariables;

	protected boolean addMissingVariables() {
		return addMissingVariables;
	}

	public Solver() {
		addMissingVariables = false;
	}

	public Solver(boolean addMissingVariables) {
		this.addMissingVariables = addMissingVariables;
	}

	static Logger logger = LoggerFactory.getLogger(Solver.class);

	/**
	 * 
	 * @param constraints
	 *            a constraint system to be solved
	 * 
	 * @return a non-null result that is SAT or UNSAT
	 * @throws SolverTimeoutException
	 *             a timeout occurred while executing the solver
	 * @throws IOException
	 *             an IOException occurred while executing the solver
	 * @throws SolverParseException
	 *             the solver's result could not be parsed into a valid
	 *             SolverResult
	 * @throws SolverEmptyQueryException
	 *             the solver
	 * @throws SolverErrorException
	 *             the solver reported an error after its execution
	 */
	public abstract SolverResult solve(Collection<Constraint<?>> constraints) throws SolverTimeoutException,
			IOException, SolverParseException, SolverEmptyQueryException, SolverErrorException;

	/**
	 * Returns a mapping from variables to their current concrete values.
	 * 
	 * @param variables
	 * @return a mapping from variables to their current concrete values.
	 */
	protected static Map<String, Object> getConcreteValues(Set<Variable<?>> variables) {

		Map<String, Object> concrete_values = new HashMap<String, Object>();
		for (Variable<?> v : variables) {
			String var_name = v.getName();
			Object concrete_value = v.getConcreteValue();
			concrete_values.put(var_name, concrete_value);
		}
		return concrete_values;
	}

	/**
	 * Creates a set with all the variables in the constraints.
	 * 
	 * @param constraints
	 *            the constraint system
	 * @return the set of variables in the constraint system
	 */
	protected static Set<Variable<?>> getVariables(Collection<Constraint<?>> constraints) {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		for (Constraint<?> c : constraints) {
			variables.addAll(c.getLeftOperand().getVariables());
			variables.addAll(c.getRightOperand().getVariables());
		}
		return variables;
	}

	/**
	 * Restore all concrete values of the variables using the concrete_values
	 * mapping.
	 * 
	 * @param variables
	 * @param concrete_values
	 */
	protected static void setConcreteValues(Set<Variable<?>> variables, Map<String, Object> concrete_values) {
		for (Variable<?> v : variables) {

			String var_name = v.getName();

			if (!concrete_values.containsKey(var_name)) {
				continue;
			}

			Object concreteValue = concrete_values.get(var_name);

			if (v instanceof StringVariable) {
				StringVariable sv = (StringVariable) v;
				String concreteString = (String) concreteValue;
				sv.setConcreteValue(concreteString);
			} else if (v instanceof IntegerVariable) {
				IntegerVariable iv = (IntegerVariable) v;
				Long concreteInteger = (Long) concreteValue;
				iv.setConcreteValue(concreteInteger);
			} else if (v instanceof RealVariable) {
				RealVariable ir = (RealVariable) v;
				Double concreteReal = (Double) concreteValue;
				ir.setConcreteValue(concreteReal);
			} else {
				logger.warn("unknow variable type " + v.getClass().getName());
			}
		}
	}

	private static final double DELTA = 1e-15;

	protected static boolean checkSAT(Collection<Constraint<?>> constraints, SolverResult satResult) {

		if (satResult == null) {
			throw new NullPointerException("satResult should be non-null");
		}

		if (!satResult.isSAT()) {
			throw new IllegalArgumentException("satResult should be SAT");
		}

		// back-up values
		Set<Variable<?>> variables = getVariables(constraints);
		Map<String, Object> initialValues = getConcreteValues(variables);
		// set new values
		Map<String, Object> newValues = satResult.getModel();
		setConcreteValues(variables, newValues);
		// check SAT with new values
		double distance = DistanceEstimator.getDistance(constraints);
		// restore values
		setConcreteValues(variables, initialValues);
		if (distance <= DELTA) {
			return true;
		} else {
			return false;
		}
	}


}
