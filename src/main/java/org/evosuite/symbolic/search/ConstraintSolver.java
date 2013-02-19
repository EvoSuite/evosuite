package org.evosuite.symbolic.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.symbolic.Solver;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Solves a collection of constraints using the Alternating Variable method.
 * 
 * @author galeotti
 * 
 */
public final class ConstraintSolver implements Solver {

	static Logger log = LoggerFactory.getLogger(ConstraintSolver.class);

	/**
	 * This method searches for a new model satisfying all constraints. If UNSAT
	 * returns <code>null</code>.
	 */
	public Map<String, Object> solve(Collection<Constraint<?>> constraints)
			throws ConstraintSolverTimeoutException {

		long startTimeMillis = System.currentTimeMillis();

		double distance = DistanceEstimator.getDistance(constraints);
		if (distance == 0.0) {
			log.info("Initial distance already is 0.0, skipping search");
			return null;
		}

		Set<Variable<?>> variables = getVariables(constraints);
		Map<String, Object> initialValues = getConcreteValues(variables);
		for (int attempt = 0; attempt <= Properties.DSE_VARIABLE_RESETS; attempt++) {
			for (Variable<?> v : variables) {
				long currentTimeMillis = System.currentTimeMillis();
				if (Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS > 0
						&& (currentTimeMillis - startTimeMillis > Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS)) {
					throw new ConstraintSolverTimeoutException();
				}

				log.debug("Variable: " + v + ", " + variables);

				if (v instanceof IntegerVariable) {
					IntegerVariable integerVariable = (IntegerVariable) v;
					IntegerAVM avm = new IntegerAVM(integerVariable,
							constraints);
					avm.applyAVM();
				} else if (v instanceof RealVariable) {
					RealVariable realVariable = (RealVariable) v;
					RealAVM avm = new RealAVM(realVariable, constraints);
					avm.applyAVM();
				} else if (v instanceof StringVariable) {
					StringVariable strVariable = (StringVariable) v;
					StringAVM avm = new StringAVM(strVariable, constraints);
					avm.applyAVM();
				} else {
					throw new RuntimeException("Unknown variable type "
							+ v.getClass().getName());
				}
				distance = DistanceEstimator.getDistance(constraints);
				if (distance <= 0.0) {
					log.info("Distance is 0, ending search");
					break;
				}
			}
			if (distance <= 0.0) {
				log.info("Distance is 0, ending search");
				break;
			} else {
				log.info("Randomizing variables");
				randomizeValues(variables);
			}
		}

		// distance = DistanceEstimator.getDistance(constraints);
		if (distance <= 0) {
			log.debug("Distance is " + distance + ", found solution");
			Map<String, Object> new_model = getConcreteValues(variables);
			restoreConcreteValues(variables, initialValues);
			return new_model;
		} else {
			restoreConcreteValues(variables, initialValues);
			log.debug("Returning null, search was not successful");
			return null;
		}

		// if (DSEBudget.isFinished()) {
		// log.debug("Out of time");
		// //break resetLoop;
		// }

	}

	private void randomizeValues(Set<Variable<?>> variables) {
		for (Variable<?> v : variables) {
			if (v instanceof StringVariable) {
				StringVariable sv = (StringVariable) v;
				sv.setConcreteValue(Randomness
						.nextString(Properties.STRING_LENGTH));
			} else if (v instanceof IntegerVariable) {
				IntegerVariable iv = (IntegerVariable) v;
				iv.setConcreteValue((long) Randomness
						.nextInt(Properties.MAX_INT * 2) - Properties.MAX_INT);
			} else if (v instanceof RealVariable) {
				RealVariable rv = (RealVariable) v;
				rv.setConcreteValue((long) Randomness
						.nextInt(Properties.MAX_INT * 2) - Properties.MAX_INT);
			}
		}
	}

	/**
	 * Restore all concrete values of the variables using the concrete_values
	 * mapping.
	 * 
	 * @param variables
	 * @param concrete_values
	 */
	private void restoreConcreteValues(Set<Variable<?>> variables,
			Map<String, Object> concrete_values) {
		for (Variable<?> v : variables) {

			String var_name = v.getName();
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
				log.warn("unknow variable type " + v.getClass().getName());
			}
		}
	}

	/**
	 * Returns a mapping from variables to their current concrete values.
	 * 
	 * @param variables
	 * @return a mapping from variables to their current concrete values.
	 */
	private Map<String, Object> getConcreteValues(Set<Variable<?>> variables) {

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
	private Set<Variable<?>> getVariables(Collection<Constraint<?>> constraints) {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		for (Constraint<?> c : constraints) {
			variables.addAll(c.getLeftOperand().getVariables());
			variables.addAll(c.getRightOperand().getVariables());
		}
		return variables;
	}

}
