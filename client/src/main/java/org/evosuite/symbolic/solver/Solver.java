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
package org.evosuite.symbolic.solver;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.smt.SmtExpr;
import org.evosuite.symbolic.solver.smt.SmtExprEvaluator;
import org.evosuite.symbolic.solver.smt.SmtExprPrinter;
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
	 * Get concrete values for the parameters used in the path conditions.
	 * 
	 * @return A {@link Map} where the name of the parameter is the key and the
	 *         concrete value that the solver used is the object.
	 * @param constraints
	 *            a {@link java.util.Collection} object.
	 */
	public abstract Map<String, Object> solve(
			Collection<Constraint<?>> constraints)
			throws ConstraintSolverTimeoutException;

	/**
	 * Returns a mapping from variables to their current concrete values.
	 * 
	 * @param variables
	 * @return a mapping from variables to their current concrete values.
	 */
	protected static Map<String, Object> getConcreteValues(
			Set<Variable<?>> variables) {

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
	protected static Set<Variable<?>> getVariables(
			Collection<Constraint<?>> constraints) {
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
	protected static void setConcreteValues(Set<Variable<?>> variables,
			Map<String, Object> concrete_values) {
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

	protected static boolean checkSolution(List<SmtExpr> smtExpressions,
			Map<String, Object> solution) {
		
//		SmtExprEvaluator evaluator = new SmtExprEvaluator(solution);
//		for (SmtExpr smtExpr : smtExpressions) {
//			Boolean ret_val = (Boolean) smtExpr.accept(evaluator, null);
//			if (ret_val.booleanValue() == false) {
//				logger.debug("The following SMT expression was not satisfied by the given solution");
//				String smtExprStr = smtExpr.accept(new SmtExprPrinter(), null);
//				logger.debug(smtExprStr);
//				return false;
//			}
//		}
//
//		logger.debug("All SMT expressions were satisfied with the given solution");
		return true;
	}

	protected static boolean checkSolution(
			Collection<Constraint<?>> constraints, Map<String, Object> solution) {

		Set<Variable<?>> variables = getVariables(constraints);
		Map<String, Object> initialValues = getConcreteValues(variables);
		setConcreteValues(variables, solution);
		double distance = DistanceEstimator.getDistance(constraints);
		setConcreteValues(variables, initialValues);
		if (distance <= DELTA) {
			return true;
		} else {
			return false;
		}
	}

}
