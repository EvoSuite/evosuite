package org.evosuite.symbolic.search;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.evosuite.symbolic.Solver;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.str.StringVariable;
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
	 * This method searches for a new model satisfying all constraints. If no
	 * model is found, then the <code>null</code> value is returned.
	 */
	@Override
	public Map<String, Object> getModel(Collection<Constraint<?>> constraints) {
		double distance = DistanceEstimator.getDistance(constraints);
		if (distance == 0.0) {
			log.info("Initial distance already is 0.0, skipping search");
			return null;
		}

		Set<Variable<?>> variables = getVariables(constraints);
		Map<String, Object> initialValues = getConcreteValues(variables);

		for (Variable<?> v : variables) {
			log.debug("Variable: " + v + ", " + variables);

			if (v instanceof IntegerVariable) {
				IntegerVariable integerVariable = (IntegerVariable) v;
				IntegerAVM avm = new IntegerAVM(integerVariable, constraints);
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

		}

		distance = DistanceEstimator.getDistance(constraints);
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
